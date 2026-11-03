package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp
public class GamePad extends OpMode {
    DcMotor frontLeft, frontRight, backLeft, backRight, intake, indexer, shooter;
    CRServo servo;

    boolean intakeOn = false;
    boolean intakeReversed = false;
    boolean lastBumper = false;
    boolean lastDpad = false;

    // Shooter toggle
    boolean shooterOn = false;
    boolean lastY = false;

    // Power limiting
    static final double INTAKE_MAX = 0.8;     // cap: intake never exceeds 80% power
    static final double INDEXER_POWER = 1.0;  // indexer power while right trigger is held
    static final double SHOOTER_POWER = 1.0;  // shooter power while toggled on
    static final double RAMP_PER_SEC = 3.0;   // power change per second (0 to 1 in ~0.33 s)
    double intakeCurrent = 0;                 // power actually being sent to the intake
    double shooterCurrent = 0;                // power actually being sent to the shooter
    ElapsedTime timer = new ElapsedTime();

    // Servo (continuous rotation mode): 1.5 turns per A press, alternating direction
    static final double SERVO_POWER = 0.8;             // speed during the rotation
    static final double SERVO_ROTATION_SECONDS = 1.5;  // TUNE: time for 1.5 turns
    boolean lastA = false;
    boolean servoClockwiseNext = true;   // first press goes clockwise, then alternates
    boolean servoRunning = false;
    ElapsedTime servoTimer = new ElapsedTime();

    // Moves current toward target by at most maxStep
    double ramp(double current, double target, double maxStep) {
        if (target > current + maxStep) return current + maxStep;
        if (target < current - maxStep) return current - maxStep;
        return target;
    }

    @Override
    public void init() {
        // Names must match exactly what you set in the Robot Controller configuration
        frontLeft  = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft   = hardwareMap.get(DcMotor.class, "backLeft");
        backRight  = hardwareMap.get(DcMotor.class, "backRight");
        intake     = hardwareMap.get(DcMotor.class, "intake");
        indexer    = hardwareMap.get(DcMotor.class, "indexer");
        shooter    = hardwareMap.get(DcMotor.class, "shooter");
        servo      = hardwareMap.get(CRServo.class, "servo");

        // Motors on opposite sides face opposite directions, so reverse one side
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        intake.setDirection(DcMotorSimple.Direction.REVERSE);
        indexer.setDirection(DcMotorSimple.Direction.REVERSE);
        // (if the robot drives backward when you push forward, swap to reversing the right side instead)
        // (if the shooter spins the wrong way, add: shooter.setDirection(DcMotorSimple.Direction.REVERSE);)
        // (if the servo's first press goes counter-clockwise, add: servo.setDirection(DcMotorSimple.Direction.REVERSE);)

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        indexer.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    @Override
    public void start() {
        timer.reset();  // so the first loop doesn't see a huge time gap since init
    }

    @Override
    public void loop() {
        // Driver (gamepad1): driving
        double fwd = -gamepad1.right_stick_y / 2;
        double str =  gamepad1.right_stick_x / 2;
        double rot =  gamepad1.left_stick_x / 2;

        telemetry.addData("fwd", fwd);
        telemetry.addData("str", str);
        telemetry.addData("rot", rot);

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

        // Operator (gamepad2): toggle intake on/off on each new bumper press
        if (gamepad2.left_bumper && !lastBumper) {
            intakeOn = !intakeOn;
        }
        lastBumper = gamepad2.left_bumper;

        // Operator (gamepad2): toggle direction on each new dpad_down press
        if (gamepad2.dpad_down && !lastDpad) {
            intakeReversed = !intakeReversed;
        }
        lastDpad = gamepad2.dpad_down;

        // Operator (gamepad2): toggle shooter on/off on each new Y press
        if (gamepad2.y && !lastY) {
            shooterOn = !shooterOn;
        }
        lastY = gamepad2.y;

        // Time since the last loop, used to scale the ramps
        double dt = timer.seconds();
        timer.reset();

        // Intake: target power (capped), then ramp toward it
        double intakeTarget = 0;
        if (intakeOn) {
            intakeTarget = intakeReversed ? -INTAKE_MAX : INTAKE_MAX;
        }
        intakeCurrent = ramp(intakeCurrent, intakeTarget, RAMP_PER_SEC * dt);
        intake.setPower(intakeCurrent);

        // Indexer (operator): runs only while gamepad2's right trigger is held (no ramp)
        indexer.setPower(gamepad2.right_trigger > 0.1 ? INDEXER_POWER : 0);

        // Shooter: stays on once toggled with Y, ramped to avoid a current spike
        double shooterTarget = shooterOn ? SHOOTER_POWER : 0;
        shooterCurrent = ramp(shooterCurrent, shooterTarget, RAMP_PER_SEC * dt);
        shooter.setPower(shooterCurrent);

        // Operator (gamepad2): on each new A press, rotate the servo 1.5 turns, alternating direction
        if (gamepad2.a && !lastA && !servoRunning) {
            servo.setPower(servoClockwiseNext ? SERVO_POWER : -SERVO_POWER);
            servoClockwiseNext = !servoClockwiseNext;
            servoTimer.reset();
            servoRunning = true;
        }
        lastA = gamepad2.a;

        // Stop the servo once the time is up
        if (servoRunning && servoTimer.seconds() >= SERVO_ROTATION_SECONDS) {
            servo.setPower(0);
            servoRunning = false;
        }

        telemetry.addData("intake", intakeCurrent);
        telemetry.addData("indexer", indexer.getPower());
        telemetry.addData("shooter", shooterCurrent);
        telemetry.addData("shooter on", shooterOn);
        telemetry.addData("servo running", servoRunning);
    }
}