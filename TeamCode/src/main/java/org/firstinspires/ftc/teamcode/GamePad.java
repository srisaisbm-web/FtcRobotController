package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp
public class GamePad extends OpMode {
    DcMotor frontLeft, frontRight, backLeft, backRight, intake;

    @Override
    public void init() {
        // Names must match exactly what you set in the Robot Controller configuration
        frontLeft  = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft   = hardwareMap.get(DcMotor.class, "backLeft");
        backRight  = hardwareMap.get(DcMotor.class, "backRight");
        intake  = hardwareMap.get(DcMotor.class, "intake");

        // Motors on opposite sides face opposite directions, so reverse one side
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        // (if the robot drives backward when you push forward, swap to reversing the right side instead)

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    @Override
    public void loop() {
        double fwd = -gamepad1.right_stick_y / 2;
        double str =  gamepad1.right_stick_x / 2;
        double rot =  gamepad1.left_stick_x / 2;

        telemetry.addData("fwd",fwd);
        telemetry.addData("str",str);
        telemetry.addData("rot",rot);

        // Mecanum mixing: each wheel gets a different combination
        double fl = fwd + str + rot;
        double fr = fwd - str - rot;
        double bl = fwd - str + rot;
        double br = fwd + str - rot;

        // Motor power must stay within -1 to 1; scale everything down together if any exceeds it
        double max = Math.max(1.0, Math.max(Math.max(Math.abs(fl), Math.abs(fr)),
                Math.max(Math.abs(bl), Math.abs(br))));

        frontLeft.setPower(fl / max);
        frontRight.setPower(fr / max);
        backLeft.setPower(bl / max);
        backRight.setPower(br / max);

        if (gamepad1.a) {
            intake.setPower(1.0);
        } else if (gamepad1.b) {
            intake.setPower(-1.0);
        } else {
            intake.setPower(0);
        }
    }
}