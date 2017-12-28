package org.firstinspires.ftc.teamcode.Autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcontroller.external.samples.ConceptVuforiaNavigation;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuMarkInstanceId;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

/**
 * Created by Cristian on 11/10/17.
 */

@Autonomous(name="AutoRedClose", group ="AutoRed")

public class AutoBlueClose extends LinearOpMode {

    //ColorSensor color_sensor;

    HardwareRobespierre         robot   = new HardwareRobespierre();   // Use a Pushbot's hardware
    private ElapsedTime runtime = new ElapsedTime();

    static final double     COUNTS_PER_MOTOR_REV    = 1220;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 2;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 4.0;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = ((COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415));
    static final double     DRIVE_SPEED             = 0.39;
    static final double     TURN_SPEED              = 0.5;
    static final double     STRAFE_SPEED            = 0.25;

    public static final String TAG = "Vuforia VuMark Sample";

    OpenGLMatrix lastLocation = null;
    VuforiaLocalizer vuforia;

    @Override public void runOpMode() {
        //color_sensor = hardwareMap.colorSensor.get("color");
        //color_sensor.enableLed(true);

        //color_sensor.red();   // Red channel value
        //color_sensor.green(); // Green channel value
        //color_sensor.blue();  // Blue channel value

        //color_sensor.alpha(); // Total luminosity
        //color_sensor.argb();  // Combined color value

        //telemetry.addData("Red:", color_sensor.red());
        //telemetry.addData("Blue:", color_sensor.green());
        //telemetry.addData("Green:", color_sensor.blue());
        //telemetry.addData("Alpha:", color_sensor.alpha());
        //telemetry.addData("ARGB:", color_sensor.argb());

        robot.init(hardwareMap);

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Resetting Encoders");    //
        telemetry.update();

        robot.lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.lf.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rf.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.lb.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rb.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Send telemetry message to indicate successful Encoder reset
        telemetry.addData("Path0",  "Starting at %7d :%7d",
                robot.lf.getCurrentPosition(),
                robot.rf.getCurrentPosition());
        robot.colorSensor.enableLed(true);
        telemetry.update();


        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        parameters.vuforiaLicenseKey = "AR/dGQz/////AAAAGefSPdahvk9xi29/5Ic0ZAIMHot8Hd/hCJSa/WffbXSv6wZnEcenvy8VMW0rLYr9rfocY8sa48bqBuPc6JjQhPIKeY7DBUhC2QTE+3uyNp0IkvCg4Y7qAuiVn+HUhfeLLtZpPlsmU23SbW7Ed/7G3VUGJeW35q8gLO/pl48dCAbAHQzer40Lwa8BXprp717Pe8r30KQuT+NzSiaxF9zdrdLNXukLrgi57Y4T68UyI72a1IypYIV3Ug31ICLnQU/AN3okoNLIVEd0v2+Qm2T/AgD17aPytGWoAcVthsfDumgSBt+cvigvAH4DkzUQ+faQfaGJjeKv2c16NQ3B4NX1kJ08jsxxawOolkqOPMjfl1QW";

        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        VuforiaTrackables relicTrackables = this.vuforia.loadTrackablesFromAsset("RelicVuMark");
        VuforiaTrackable relicTemplate = relicTrackables.get(0);
        relicTemplate.setName("relicVuMarkTemplate"); // can help in debugging; otherwise not necessary

        telemetry.addData(">", "Press Play to start");
        telemetry.update();


        waitForStart();

        relicTrackables.activate();

        while (opModeIsActive()) {

            RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);
            if (vuMark != RelicRecoveryVuMark.UNKNOWN) {

                telemetry.addData("VuMark", "%s visible", vuMark);

                OpenGLMatrix pose = ((VuforiaTrackableDefaultListener)relicTemplate.getListener()).getPose();
                telemetry.addData("Pose", format(pose));

                if (pose != null) {
                    telemetry.addData("VuMark", "%s visible", vuMark);
                    if(vuMark == RelicRecoveryVuMark.LEFT){
                        encoderDrive(DRIVE_SPEED,39,39,15);
                        encoderDrive(TURN_SPEED,-18.5,18.5,10);
                        sleep(200);
                        robot.extend.setPosition(.9);
                        sleep(200);
                        encoderDrive(DRIVE_SPEED,5,5,3);
                        sleep(200);
                        robot.grabber.setPosition(.15);
                        sleep(200);
                        encoderDrive(DRIVE_SPEED, -6,-6,2);
                        sleep(200);
                        robot.grabber.setPosition(.41);
                        sleep(1000);
                        robot.extend.setPosition(.20);
                        sleep(500);
                        encoderDrive(DRIVE_SPEED, 6,6,2);
                        stop();
                    }else if(vuMark == RelicRecoveryVuMark.CENTER){
                        encoderDrive(DRIVE_SPEED,31.5,31.5,15);
                        encoderDrive(TURN_SPEED,-18.5,18.5,10);
                        sleep(200);
                        robot.extend.setPosition(.9);
                        sleep(200);
                        encoderDrive(DRIVE_SPEED,5,5,3);
                        sleep(200);
                        robot.grabber.setPosition(.15);
                        sleep(200);
                        encoderDrive(DRIVE_SPEED, -6,-6,2);
                        sleep(200);
                        robot.grabber.setPosition(.41);
                        sleep(1000);
                        robot.extend.setPosition(.20);
                        sleep(500);
                        encoderDrive(DRIVE_SPEED, 6,6,2);
                        stop();
                    }else if(vuMark == RelicRecoveryVuMark.RIGHT){
                        encoderDrive(DRIVE_SPEED,25,25,15);
                        encoderDrive(TURN_SPEED,-18.5,18.5,10);
                        sleep(200);
                        robot.extend.setPosition(.9);
                        sleep(200);
                        encoderDrive(DRIVE_SPEED,5,5,3);
                        sleep(200);
                        robot.grabber.setPosition(.15);
                        sleep(200);
                        encoderDrive(DRIVE_SPEED, -6,-6,2);
                        sleep(200);
                        robot.grabber.setPosition(.41);
                        sleep(1000);
                        robot.extend.setPosition(.20);
                        sleep(500);
                        encoderDrive(DRIVE_SPEED, 6,6,2);
                        stop();
                    }
                }
            }
            else {
                telemetry.addData("VuMark", "not visible");
            }

            telemetry.update();
        }
    }

    String format(OpenGLMatrix transformationMatrix) {
        return (transformationMatrix != null) ? transformationMatrix.formatAsTransform() : "null";
    }
    public void encoderDrive(double speed,
                             double leftInches, double rightInches,
                             double timeoutS) {
        int newLeftTarget;
        int newRightTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {
// Turn On RUN_TO_POSITION
            robot.lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // Determine new target position, and pass to motor controller
            newLeftTarget = robot.lf.getCurrentPosition() + (int)(leftInches * COUNTS_PER_INCH);
            newRightTarget = robot.rf.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH);
            robot.lf.setTargetPosition(newLeftTarget);
            robot.lb.setTargetPosition(newLeftTarget);
            robot.rf.setTargetPosition(newRightTarget);
            robot.rb.setTargetPosition(newRightTarget);




            // reset the timeout time and start motion.
            runtime.reset();
            robot.lf.setPower(Math.abs(speed));
            robot.rf.setPower(Math.abs(speed));
            robot.lb.setPower(Math.abs(speed));
            robot.rb.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (robot.lf.isBusy() && robot.rf.isBusy()
                            && robot.lb.isBusy() && robot.rb.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Path1",  "Running to %7d :%7d", newLeftTarget,  newRightTarget);
                telemetry.addData("Path2",  "Running at %7d :%7d",
                        robot.lf.getCurrentPosition(),
                        robot.rf.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            robot.lf.setPower(0);
            robot.rf.setPower(0);
            robot.lb.setPower(0);
            robot.rb.setPower(0);

            // Turn off RUN_TO_POSITION
            robot.lf.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rf.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.lb.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rb.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            sleep(250);   // optional pause after each move
        }
    }
    public void encoderStrafe(double speed,
                              double lfInches,double lbInches,
                              double rfInches , double rbInches,
                              double timeoutS) {
        int newLfTarget;
        int newRfTarget;
        int newRbTarget;
        int newLbTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {
// Turn On RUN_TO_POSITION
            robot.lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // Determine new target position, and pass to motor controller
            newLfTarget = robot.lf.getCurrentPosition() + (int)(lfInches * COUNTS_PER_INCH);
            newRfTarget = robot.rf.getCurrentPosition() + (int)(rfInches * COUNTS_PER_INCH);
            newLbTarget = robot.lb.getCurrentPosition() + (int)(lbInches * COUNTS_PER_INCH);
            newRbTarget = robot.rb.getCurrentPosition() + (int)(rbInches * COUNTS_PER_INCH);

            robot.lf.setTargetPosition(newLfTarget);
            robot.lb.setTargetPosition(newLbTarget);
            robot.rf.setTargetPosition(newRfTarget);
            robot.rb.setTargetPosition(newRbTarget);




            // reset the timeout time and start motion.
            runtime.reset();
            robot.lf.setPower(Math.abs(speed));
            robot.rf.setPower(Math.abs(speed));
            robot.lb.setPower(Math.abs(speed));
            robot.rb.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (robot.lf.isBusy() && robot.rf.isBusy()
                            && robot.lb.isBusy() && robot.rb.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Path1",  "Running to %7d :%7d", newLfTarget,  newRfTarget);
                telemetry.addData("Path2",  "Running at %7d :%7d",
                        robot.lf.getCurrentPosition(),
                        robot.rf.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            robot.lf.setPower(0);
            robot.rf.setPower(0);
            robot.lb.setPower(0);
            robot.rb.setPower(0);

            // Turn off RUN_TO_POSITION
            robot.lf.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rf.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.lb.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rb.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            sleep(250);   // optional pause after each move
        }
    }
}