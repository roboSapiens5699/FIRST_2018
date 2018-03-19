/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
// xbox
package org.usfirst.frc.team5699.robot;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
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
	Victor leftDrive1, leftDrive2, rightDrive1, rightDrive2;
	Spark intakeFlip;
	Spark conveyor;
	DoubleSolenoid solenoid;
	Compressor comp;
	
	DigitalInput microSwitchIn;
	DigitalInput microSwitchConveyor;
	boolean dropSafe = true;
	boolean inSafe = true;
	
	boolean intakeIn = false;
	boolean startPressed = false;
	boolean backPressed = false;
	boolean slowMode = false;
	double direction = 1;
	
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
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
		SmartDashboard.putData("Auto choices", m_chooser);
		
		driver = new Joystick(0);
		leftDrive1 = new Victor(0);
		leftDrive2 = new Victor(1);
		rightDrive1 = new Victor(2);
		rightDrive2 = new Victor(3);
		
		conveyor = new Spark(5);
		intakeFlip = new Spark(4);
		solenoid = new DoubleSolenoid(1,0);
		comp = new Compressor(0);
		
		microSwitchConveyor = new DigitalInput(0);
		microSwitchIn = new DigitalInput(1);
		
		/********using 2 cameras at the same time*********/
		server = CameraServer.getInstance();
		//front camera
		server.startAutomaticCapture(0);
		server.getVideo();
		server.putVideo("cam0", 320, 240);
		//back camera
		server.startAutomaticCapture(1);
		server.getVideo();
		server.putVideo("cam1", 320, 240);
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
	@Override
	public void autonomousInit() {
		m_autoSelected = m_chooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
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
			case kDefaultAuto:
			default:
				// Put default auto code here
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
	    else{
	    	return joystick.getRawAxis(0) / 4;
	    }
	}
	void drive() {
		double forewardPowerR = -getBFValue(driver) * direction + getTurnValue(driver);
		double forewardPowerL = getBFValue(driver) * direction + getTurnValue(driver);
		
		if(forewardPowerR>1){
			forewardPowerR=1;
		}else if(forewardPowerR<-1){
			forewardPowerR=-1;
		}if(forewardPowerL>1){
			forewardPowerL=1;
		}else if(forewardPowerL<-1){
			forewardPowerL=-1;
		}
		leftDrive1.set(forewardPowerL);
		leftDrive2.set(forewardPowerL);
		rightDrive1.set(forewardPowerR);
		rightDrive2.set(forewardPowerR);
	}
	/**CONTROLS INTAKE**/
	void intakeControl() {
		//taking the box
		if (driver.getRawButtonReleased(6)||this.intakeIn){
			solenoid.set(DoubleSolenoid.Value.kForward);
			this.intakeIn = true;
		}
		//dropping the box
		if (driver.getRawButtonReleased(5)||!this.intakeIn){
			solenoid.set(DoubleSolenoid.Value.kReverse);
			this.intakeIn = false;
		}
		//rotates it on top of the conveyor
		boolean flipping = false;
		if((this.driver.getRawAxis(5) > 0.2 || this.driver.getRawAxis(5) < -0.2) && dropSafe && inSafe) {
			double power;
			if(this.driver.getRawAxis(5) > 0.2)power = 0.3;
			else power = -0.3;
			this.intakeFlip.set(power);//check direction
			flipping = true;
		}
		if(!flipping && !dropSafe && !inSafe) this.intakeFlip.set(0);
		
	}
	/**CONTROLS CONVEYOR BELT**/
	void conveyorControl() {
		boolean rolling = false;
		if(driver.getRawButton(4)){
			conveyor.set(1);
			rolling = true;
		}
		if(driver.getRawButton(1)){
			conveyor.set(-1);
			rolling = true;
		}
		if(!rolling) {
			conveyor.set(0);
		}
	}
	
	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		comp.setClosedLoopControl(true);
		comp.setClosedLoopControl(false);
		comp.start();
		
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
		this.inSafe = this.microSwitchIn.get();
		this.dropSafe = this.microSwitchIn.get();
	}

	
	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
