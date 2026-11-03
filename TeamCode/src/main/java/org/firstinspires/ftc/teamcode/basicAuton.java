package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name = "Auto Forward 2ft")
public class basicAuton extends LinearOpMode {
    DcMotor frontLeft, frontRight, backLeft, backRight;

    // ---- TUNE THESE FOR YOUR ROBOT ----
    static final double TICKS_PER_REV = 537.7;
    static final double WHEEL_DIAMETER_IN = 3.78;
    static final double GEAR_RATIO = 1.0;

    static final double DISTANCE_IN = 24.0;   // 2 feet
    static final double DRIVE_POWER = 0.4;
    static final double TIMEOUT_SEC = 5.0;    // safety: stop no matter what after this long

    static final double TICKS_PER_INCH =
            (TICKS_PER_REV * GEAR_RATIO) / (Math.PI * WHEEL_DIAMETER_IN);

    @Override
    public void runOpMode() {
        frontLeft  = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft   = hardwareMap.get(DcMotor.class, "backLeft");
        backRight  = hardwareMap.get(DcMotor.class, "backRight");

        // Same directions as the TeleOp
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        int target = (int) Math.round(DISTANCE_IN * TICKS_PER_INCH);
        frontLeft.setTargetPosition(target);
        frontRight.setTargetPosition(target);
        backLeft.setTargetPosition(target);
        backRight.setTargetPosition(target);
        setMode(DcMotor.RunMode.RUN_TO_POSITION);

        telemetry.addData("Target ticks", target);
        telemetry.addLine("Ready - press Play");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            ElapsedTime timeout = new ElapsedTime();

            setPower(DRIVE_POWER);

            // Run until the wheels reach the target, or time runs out (so it can never hang)
            while (opModeIsActive()
                    && timeout.seconds() < TIMEOUT_SEC
                    && (frontLeft.isBusy() || frontRight.isBusy()
                    || backLeft.isBusy() || backRight.isBusy())) {
                telemetry.addData("FL", frontLeft.getCurrentPosition());
                telemetry.addData("FR", frontRight.getCurrentPosition());
                telemetry.addData("BL", backLeft.getCurrentPosition());
                telemetry.addData("BR", backRight.getCurrentPosition());
                telemetry.addData("Time", "%.1f s", timeout.seconds());
                telemetry.update();
            }

            // Stop everything, then leave RUN_TO_POSITION so no motor keeps trying to move
            setPower(0);
            setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    private void setPower(double p) {
        frontLeft.setPower(p);
        frontRight.setPower(p);
        backLeft.setPower(p);
        backRight.setPower(p);
    }

    private void setMode(DcMotor.RunMode mode) {
        frontLeft.setMode(mode);
        frontRight.setMode(mode);
        backLeft.setMode(mode);
        backRight.setMode(mode);
    }
}