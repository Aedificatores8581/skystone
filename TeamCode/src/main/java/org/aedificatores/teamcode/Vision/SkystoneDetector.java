package org.aedificatores.teamcode.Vision;

import android.util.Log;

import org.aedificatores.teamcode.Universal.TelemetryLogger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.io.IOException;

public class SkystoneDetector extends OpenCvPipeline {
    Mat hsvImage;
    Mat bgrImage;
    Mat threshold;
    Mat thresholdAfterROI;
    Rect roi;

    Mat colSum;
    int[] colSumArray;
    int[] colSumRunningAverage;
    int[] colSumRunningAverageDeriv;
    int derivMax = 0, derivMin = 0;

    final int RUNNING_AVG_WINDOW_SIZE = 6;
    int average = 0;
    public int dieRoll = 0;

    TelemetryLogger logger;

    public int blockPixelPosition;

    int screenWidth, screenHeight;
    private int H_MIN = 0,
            S_MIN = 185,
            V_MIN = 40,
            H_MAX = 70,
            S_MAX = 255,
            V_MAX = 255;

    public SkystoneDetector(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        hsvImage = new Mat();
        bgrImage = new Mat();
        threshold = new Mat();
        thresholdAfterROI = new Mat();

        colSum = new Mat();
        colSumArray = new int[screenWidth];
        colSumRunningAverage = new int[colSumArray.length- RUNNING_AVG_WINDOW_SIZE];
        colSumRunningAverageDeriv = new int[colSumRunningAverage.length - 1];
        roi = new Rect(0, 20, 316, 82);

        blockPixelPosition = 0;

        try {
            logger = new TelemetryLogger();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Mat processFrame(Mat input) {
        // For whatever reason, OpenCV requires you to do some weird acrobatics to convert
        // RGBA to HSV, that's what is done here
        Imgproc.cvtColor(input, bgrImage, Imgproc.COLOR_RGBA2BGR);
        Imgproc.cvtColor(bgrImage, hsvImage, Imgproc.COLOR_BGR2HSV_FULL);

        // Threshold hsv values
        Core.inRange(hsvImage,
                new Scalar(H_MIN, S_MIN, V_MIN),
                new Scalar(H_MAX, S_MAX, V_MAX),
                threshold);
        thresholdAfterROI = threshold.submat(roi);

        Imgproc.cvtColor(threshold, bgrImage, Imgproc.COLOR_GRAY2BGR);
        Imgproc.cvtColor(bgrImage, input, Imgproc.COLOR_BGR2RGBA);
        Imgproc.rectangle(input, roi, new Scalar(0,255,0), 4);

        Core.reduce(thresholdAfterROI, colSum, 0, Core.REDUCE_SUM, CvType.CV_32S);

        colSum.get(0,0,colSumArray);

        for (int i = 0; i < colSumRunningAverage.length; ++i) {
            int sum = 0;

            for (int j = 0; j < RUNNING_AVG_WINDOW_SIZE; ++j){
                if (i + j < colSumRunningAverage.length){
                    sum += colSumArray[i+j];
                } else {
                    sum += colSumArray[i];
                }
            }
            colSumRunningAverage[i] = sum / RUNNING_AVG_WINDOW_SIZE;
        }

        for (int i = 0; i < colSumRunningAverageDeriv.length; ++i) {
            colSumRunningAverageDeriv[i] = colSumRunningAverage[i+1] - colSumRunningAverage[i];
        }
        derivMin = 0;
        for (int i=0; i < colSumRunningAverageDeriv.length; ++i) {
            if (colSumRunningAverageDeriv[i] < derivMin) {
                derivMin = colSumRunningAverageDeriv[i];
                blockPixelPosition = i + screenWidth / 6;
            }
        }

        for (int i : colSumRunningAverage) {
            average += i;
        }

        average /= colSumRunningAverage.length;

        {
            if (blockPixelPosition <= screenWidth/3) {
                dieRoll = 1;
            } else if (blockPixelPosition <= 2*screenWidth/3){
                dieRoll = 2;
            } else {
                dieRoll = 3;
            }
        }
        return input;
    }

    @Override
    public void onViewportTapped() {
        for (int i = 0; i < colSumRunningAverageDeriv.length; ++i) {
            try {
                logger.writeToLogInCSV(i,colSumArray[i],colSumRunningAverage[i],colSumRunningAverageDeriv[i]);
            } catch (IOException e) {
                Log.e("SKYSTONE DETECTOR", e.getMessage());
            }
        }
    }

    public void close() {
        hsvImage.release();
        bgrImage.release();
        threshold.release();
        thresholdAfterROI.release();
        colSum.release();
    }
}