/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
// xbox
package org.usfirst.frc.team5699.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	CameraServer server;
	Joystick driver;
	Victor leftDrive, rightDrive;
	Spark intakeFlip;
	Spark conveyor;
	DoubleSolenoid solenoid;
	Compressor comp;
	private ADXRS450_Gyro gyro;
	
	boolean dropSafe = true;
	boolean inSafe = true;
	
	boolean intakeIn = false;
	boolean startPressed = false;
	boolean backPressed = false;
	boolean slowMode = false;
	double direction = 1;
	
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	private static final String kCentreRight = "Centre Right Auto";
	private static final String kCentreLeft = "Centre Left Auto";
	private static final String kLeft = "Left Auto";
	private static final String kRight = "Right Auto";
	
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		m_chooser.addObject("Centre Right Auto", kCentreRight);
		m_chooser.addObject("Centre Left Auto", kCentreLeft);
		m_chooser.addObject("Left Auto", kLeft);
		m_chooser.addObject("Right Auto", kRight);
		
		SmartDashboard.putData("Auto choices", m_chooser);
		driver = new Joystick(0);
		leftDrive = new Victor(0);
		rightDrive = new Victor(1);
		autoCount = 0;
		conveyor = new Spark(2);
		intakeFlip = new Spark(3);
		solenoid = new DoubleSolenoid(1,0);
		comp = new Compressor(0);
		gyro = new ADXRS450_Gyro();
		gyro.calibrate();
	

		
		/********using 2 cameras at the same time*********/
		server = CameraServer.getInstance();
		//front camera
		server.startAutomaticCapture(0);
		server.getVideo();
		server.putVideo("cam0", 160, 120);
		//back camera
		server.startAutomaticCapture(1);
		server.getVideo();
		server.putVideo("cam1", 160, 120);
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	int autoCount;
	@Override
	public void autonomousInit() {
		m_autoSelected = m_chooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
		//this.gyro.calibrate();
		autoCount = 0;
		this.conveyor.set(0);
		solenoid.set(DoubleSolenoid.Value.kReverse);

	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		switch (m_autoSelected) {
			case kCustomAuto:
				// Put custom auto code here
				break;
			case kCentreRight:
				if(autoCount == 0) {
					Timer.delay(10);
					this.setMotors(0.54, 0.5);
					Timer.delay(2.5);
					this.setMotors(0, 0);
					autoCount++;
					if(DriverStation.getInstance().getGameSpecificMessage().charAt(0) == 'R') {
						this.solenoid.set(DoubleSolenoid.Value.kForward);
						this.conveyor.set(0.75);
						Timer.delay(3);
						this.conveyor.set(0);
					}
				}
				break;
			case kCentreLeft:
				if(autoCount == 0) {
					
					Timer.delay(10);
					this.setMotors(0.54, 0.5);
					Timer.delay(2.5);
					this.setMotors(0, 0);
					autoCount++;
					if(DriverStation.getInstance().getGameSpecificMessage().charAt(0) == 'L') {
						this.solenoid.set(DoubleSolenoid.Value.kForward);
						this.conveyor.set(0.75);
						Timer.delay(3);
						this.conveyor.set(0);
					}
				}
				break;
			case kLeft:
				if(autoCount == 0) {
					
					Timer.delay(5);
					this.setMotors(0.54, 0.5);
					Timer.delay(2.5);
					this.setMotors(0, 0);
					Timer.delay(0.5);
					gyro.reset();
					autoCount++;
				}else if(autoCount == 1&&gyro.getAngle()>-90&&DriverStation.getInstance().getGameSpecificMessage().charAt(0) == 'L') {
					this.setMotors(-0.3,0.3);
					if(gyro.getAngle()<=-90) {
						this.setMotors(0,0);
						autoCount++;
						Timer.delay(0.5);
						gyro.reset();
					}
				}else if(autoCount == 2) {
					this.setMotors(0.34, 0.3);
					Timer.delay(2);
					this.setMotors(0, 0);
					autoCount++;
				}else if(autoCount == 3) {
					this.solenoid.set(DoubleSolenoid.Value.kForward);
					this.conveyor.set(0.75);
					Timer.delay(3);
					this.conveyor.set(0);
					autoCount++;
				}
				break;
				
			case kRight:
				if(autoCount == 0) {
					Timer.delay(5);
					this.setMotors(0.54, 0.5);
					Timer.delay(2.5);
					this.setMotors(0, 0);
					Timer.delay(0.5);
					gyro.reset();
					autoCount++;
				}else if(autoCount == 1&&gyro.getAngle()<90&&DriverStation.getInstance().getGameSpecificMessage().charAt(0) == 'R') {
					this.setMotors(0.3,-0.3);
					if(gyro.getAngle()>=90) {
						this.setMotors(0,0);
						autoCount++;
						Timer.delay(0.5);
						gyro.reset();
					}
				}else if(autoCount == 2) {
					this.setMotors(0.34, 0.3);
					Timer.delay(2);
					this.setMotors(0, 0);
					autoCount++;
				}else if(autoCount == 3) {
					this.solenoid.set(DoubleSolenoid.Value.kForward);
					this.conveyor.set(0.75);
					Timer.delay(3);
					this.conveyor.set(0);
					autoCount++;
				}
			break;
			case kDefaultAuto:
			default:
				// Put default auto code here
				
				if(autoCount == 0) {
					Timer.delay(10);
					this.setMotors(0.54, 0.5);
					Timer.delay(2.5);
					this.setMotors(0, 0);
					autoCount++;
				}
				break;
		}
	}

	
	/**DRIVESSSSS**/
	public double getBFValue(Joystick joystick) {
		if(Math.abs(joystick.getRawAxis(3)) - Math.abs(joystick.getRawAxis(2)) < 0.2 && Math.abs(joystick.getRawAxis(3)) - Math.abs(joystick.getRawAxis(2)) > -0.2){
	    	return 0;
	    }
	    else{
	    	return Math.abs(joystick.getRawAxis(3)) - Math.abs(joystick.getRawAxis(2));
	    }
	}
	public double getTurnValue(Joystick joystick) {
		if(Math.abs(joystick.getRawAxis(0)) < 0.2){
	    	return 0;
	    }
		else {
	    	return (double)(joystick.getRawAxis(0)/2);
	    }
	}
	void drive() {
		double forewardPowerR = getBFValue(driver) * direction + getTurnValue(driver);
		double forewardPowerL = -getBFValue(driver) * direction + getTurnValue(driver);
		
		if(forewardPowerR>1){
			forewardPowerR=1;
		}else if(forewardPowerR<-1){
			forewardPowerR=-1;
		}if(forewardPowerL>1){
			forewardPowerL=1;
		}else if(forewardPowerL<-1){
			forewardPowerL=-1;
		}
		
		leftDrive.set(forewardPowerL);
		rightDrive.set(forewardPowerR);
		
	}
	/**CONTROLS INTAKE**/
	boolean inPressed,inPressed1,autoFlip;
	boolean pistonDirection;
	void intakeControl() {
		//taking the box
		if(driver.getRawButton(6)&&!inPressed) {
			pistonDirection = !pistonDirection;
			inPressed = true;
		}
		if(driver.getRawButtonReleased(6)&&inPressed) {
			inPressed = false;
		}
		if(pistonDirection) {
			solenoid.set(DoubleSolenoid.Value.kForward);
		}else {
			solenoid.set(DoubleSolenoid.Value.kReverse);
		}
		
		//rotates it on top of the conveyer
		boolean flipping = false;
		if((this.driver.getRawAxis(5) > 0.2 || this.driver.getRawAxis(5) < -0.2) &&!autoFlip) {
			double power = 0;
			if(this.driver.getRawAxis(5) > -0.2) {
				power = (this.driver.getRawAxis(5)/1.5);
			}
			if(this.driver.getRawAxis(5) < 0.2 ){
				power = (this.driver.getRawAxis(5)/2); //down
			}
			this.intakeFlip.set(power);//check direction
			flipping = true;
		}
		if(!flipping) this.intakeFlip.set(0); //just in case...
		
	}
	/**CONTROLS CONVEYOR BELT**/
	void conveyorControl() {
		boolean rolling = false;
		if(driver.getRawButton(4)){
			conveyor.set(0.75);
			rolling = true;
		}
		if(driver.getRawButton(1)){
			conveyor.set(-0.75);
			rolling = true;
		}
		if(!rolling) {
			conveyor.set(0);
		}
	}
	
	/**
	 * This function is called periodically during operator control.
	 */
	boolean demoOn = false;
	@Override
	public void teleopPeriodic() {
		//comp.setClosedLoopControl(true);
		//comp.setClosedLoopControl(false);
		//comp.start();
		//System.out.println(comp.getCompressorCurrent());
		if(!demoOn) {
			drive();
			intakeControl();
			conveyorControl();
			
			//changes direction
			if(driver.getRawButton(8) && !startPressed) {
				direction *= -1;
				startPressed = true;
			}
			if(driver.getRawButtonReleased(8)) {
				startPressed = false;
			}
			//changes turning speed
			if(driver.getRawButton(7) && !backPressed) {
				slowMode = !slowMode;
				backPressed = true;
			}
			if(driver.getRawButtonReleased(7)) {
				backPressed = false;
			}
			if(driver.getRawButton(2)) {
				demoOn = true;
			}
		}else {
			demoAuto();
			demoOn = false;
		}
	}

	
	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
		
	/*** AUTONOMOUS METHODS ***/
	private void setMotors(double right, double left) {
		this.rightDrive.set(right*-1);
		this.leftDrive.set(left);
	}
	
	int randTime = (int)(Math.random() * 3) + 3;
	public void demoAuto() {
		Timer.delay(3);
		this.solenoid.set(DoubleSolenoid.Value.kReverse);
		Timer.delay(1);
		this.intakeFlip.set(0.5);
		Timer.delay(1.5);
		this.setMotors(-1, 1);
		Timer.delay(randTime);
		this.setMotors(0, 0);
		Timer.delay(0.5);
		this.solenoid.set(DoubleSolenoid.Value.kForward);
		this.conveyor.set(0.8);
		Timer.delay(2);
		this.setMotors(0, 0);
		this.conveyor.set(0);
		randTime = (int)(Math.random() * 3) + 3;
	}
}