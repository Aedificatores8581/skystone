package org.aedificatores.libspeedydoge.VF;

import org.aedificatores.libspeedydoge.Universal.Math.Pose;
import org.aedificatores.libspeedydoge.Universal.Math.Vector2;
import org.aedificatores.libspeedydoge.Universal.UniversalConstants;
import org.aedificatores.libspeedydoge.Universal.UniversalFunctions;
import org.aedificatores.libspeedydoge.VF.Objects.Robot;

/**
 * VectorFieldComponent that when interacted with, directs location away from location and towards target
 * TESTED
 */
public class PointField extends VectorFieldComponent {
    public PointField (Pose location, double strength, double falloff) {
        super(location, strength, falloff);
    }

    public Vector2 interact(Pose position) {
        Robot robot = UniversalConstants.getRobot(position);

        //zeroes the field at 0, 0, 0 and translates the location and destination to match
        Vector2 dest = getTarget().toVector().clone();
        dest.subtract(location.toVector());
        Vector2 point = new Vector2();
        point.x = position.x - location.x;
        point.y = position.y - location.y;
        point.rotate(-dest.angle());

        //creates output vector and sets its magnitude
        Vector2 output = new Vector2(point.x, point.y);
        Vector2 closestRobotPoint = robot.getClosestPoint(position.toVector());
        closestRobotPoint.subtract(robot.location.toVector());
        double strength = getStrength(output.magnitude() - closestRobotPoint.magnitude() - UniversalConstants.AVOIDANCE_THRESHOLD);


        //if the obstacle is in the way...
        if(Math.abs(UniversalFunctions.normalizeAngle180Radians(output.angle())) > Math.abs(Math.acos(output.magnitude() / dest.magnitude()))&& point.magnitude() < dest.magnitude()){
            //refedines the vector as perpendicular to its original direction
            output.setFromPolar(strength, output.angle() + Math.PI / 2);
            if(point.y > 0)
                output.setFromPolar(strength, -output.angle());
            output.x = Math.abs(output.x);

            output.rotate(dest.angle());

        }

        //if the obstacle is out of the way...
        else {

            //shoot straight for the destination
            output = new Vector2();
        }
        return output;
    }
}
