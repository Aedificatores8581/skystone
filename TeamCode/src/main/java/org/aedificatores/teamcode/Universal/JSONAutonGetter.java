package org.aedificatores.teamcode.Universal;

import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONAutonGetter {
    private static final String BASE_DIR = Environment.getExternalStorageDirectory().getPath() + "/FIRST/JSON/";
    private String path;

    private BufferedReader br;
    private String jsonBuffer;
    public JSONObject jsonObject;

    /**
     * Note: The path name entered put into the "path" parameter of this constructor
     * must just be the file name and it's extension. So if your json file is "VisionTuningTest.json",
     * then the String entered into the path parameter should be "VisionTuningTest.json", not
     * "VisionTuningTest", not "FIRST/JSON/VisionTuningTest.json", just "VisionTuningTest.json"
     * */
    public JSONAutonGetter(String path) throws IOException, JSONException {
        this.path = path;

        File f = new File(BASE_DIR + this.path);
        jsonBuffer = "";

        if(!f.exists())
            f.createNewFile();

        br = new BufferedReader(new FileReader(f));

        String line;
        while((line = br.readLine()) != null) {
            jsonBuffer = jsonBuffer.concat(line);
        }

        jsonObject = new JSONObject(jsonBuffer);

    }

    public void saveToFile() throws IOException {
        File f = new File(BASE_DIR + path);
        FileWriter writer = new FileWriter(f);
        writer.write(jsonObject.toString());
        writer.close();
    }

    public String getPath() {
        return path;
    }

    public void close() throws IOException {
        br.close();
    }
}