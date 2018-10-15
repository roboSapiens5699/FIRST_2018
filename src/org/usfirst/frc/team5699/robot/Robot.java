/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5699.robot;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();

	private Victor rightDrive, leftDrive;
	private Spark conveyor, intakeFlip;
	private Joystick driver;
	private DoubleSolenoid piston;
	private CameraServer server;

	private int direction = 1;
	private boolean startPressed = false;
	private boolean inPressed = false;
	private boolean boxIn = false;

	public void robotInit() {
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		SmartDashboard.putData("Auto choices", m_chooser);

		driver = new Joystick(0);
		
		/******** using 2 cameras at the same time *********/
		server = CameraServer.getInstance();
		// front camera
		server.startAutomaticCapture(0);
		server.getVideo();
		server.putVideo("cam0", 160, 120);
		// back camera
		server.startAutomaticCapture(1);
		server.getVideo();
		server.putVideo("cam1", 160, 120);

		rightDrive = new Victor(0);
		leftDrive = new Victor(1);
		conveyor = new Spark(2);
		intakeFlip = new Spark(3);

		piston = new DoubleSolenoid(1, 0);
		piston.set(DoubleSolenoid.Value.kForward);
	}

	public void autonomousInit() {
		m_autoSelected = m_chooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
	}

	public void autonomousPeriodic() {
		switch (m_autoSelected) {
		case kCustomAuto:
			break;
		case kDefaultAuto:
		default:
			break;
		}
	}

	public void teleopPeriodic() {
		drive();
		intakeControl();
		conveyorControl();
	}

	public void testPeriodic() {}

	public void drive() {

		double rspeed = 0, lspeed = 0;

		double fwBackSpeed = driver.getRawAxis(3) - driver.getRawAxis(2),
			   turnVal = driver.getRawAxis(0); 
		
		if (driver.getRawButton(8) && !startPressed) {
			direction *= -1;
			startPressed = true;
		}
		if (driver.getRawButtonReleased(8)) {
			startPressed = false;
		}

		fwBackSpeed = (fwBackSpeed < -0.2 || fwBackSpeed > 0.2) ? fwBackSpeed : 0;
		turnVal = (turnVal < -0.2 || turnVal > 0.2) ? turnVal : 0;

		rspeed = fwBackSpeed * direction + turnVal/2;
		lspeed = fwBackSpeed * -direction + turnVal/2;

		if (rspeed > 1)
			rspeed = 1;
		else if (rspeed < -1)
			rspeed = -1;
		if (lspeed > 1)
			lspeed = 1;
		else if (lspeed < -1)
			lspeed = -1;

		rightDrive.set(rspeed);
		leftDrive.set(lspeed);
	}

	public void intakeControl() {

		if (driver.getRawButton(6) && !inPressed) {
			boxIn = !boxIn;
			inPressed = true;
		}
		if (driver.getRawButtonReleased(6)) {
			inPressed = false;
		}
		if (boxIn) {
			piston.set(DoubleSolenoid.Value.kForward);
		} else {
			piston.set(DoubleSolenoid.Value.kReverse);
		}

		double power = this.driver.getRawAxis(5);

		if (power > 0.2)
			power /= 1.5;
		else if (power < -0.2)
			power /= 2;
		else
			power = 0;

		intakeFlip.set(power);
	}
	
	public void conveyorControl() {
		
		boolean rolling = false;
		
		if (driver.getRawButton(4)) {
			conveyor.set(0.75);
			rolling = true;
		}
		if (driver.getRawButton(1)) {
			conveyor.set(-0.75);
			rolling = true;
		}
		if (!rolling) {
			conveyor.set(0);
		}
	}
}
