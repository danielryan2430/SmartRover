package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;

import android.util.Log;
import android.view.View;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;


class Looper extends BaseIOIOLooper {
	private AnalogInput input_;
	private DigitalOutput led_;

	private PwmOutput pwmForklift;
	private PwmOutput pwmDriveRight;
	private PwmOutput pwmDriveLeft;
	private PwmOutput pwmCameraPan;
	private final int PWM_CENTER_VAL = 1500; // motor servo "off"
	private final int PWM_MIN_VAL = 1300;
	private final int PWM_MAX_VAL = 1735;
	private final int PWM_CHANGE_VAL = 25;
	private int		  pwmDriveRightVal = PWM_CENTER_VAL;
	private int		  pwmDriveLeftVal = PWM_CENTER_VAL;
	private int		  pwmForkliftVal = PWM_CENTER_VAL;
	private int		  pwmCameraPanVal = PWM_CENTER_VAL;

	private final String	TAG = "IOIOSimpleAppLooper";

	@Override
	public void setup() throws ConnectionLostException {
		led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
		input_ = ioio_.openAnalogInput(40);

		try
		{
			pwmForklift = ioio_.openPwmOutput(4, 50);
			pwmDriveRight = ioio_.openPwmOutput(3, 50);
			pwmDriveLeft = ioio_.openPwmOutput(2, 50);
			pwmCameraPan = ioio_.openPwmOutput(1, 50);
		}
		catch (ConnectionLostException e)
		{
			Log.e(TAG, "Connection to the controller is unavailable, configuration is aborted.");
			e.printStackTrace();
		}

		// Begin MDR Specific Buttons
		btnDriveRightFwd.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				// Subtract to move forward as servo is mounted opposite
				// to positive direction
				if( pwmDriveRightVal >= ( PWM_MIN_VAL + PWM_CHANGE_VAL ) )
				{
					pwmDriveRightVal -= PWM_CHANGE_VAL;
					Log.v( TAG, "New right pwm val: " + pwmDriveRightVal );
				}
			}
		});

		btnDriveLeftFwd.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				// Add to move forward as servo is mounted in positive
				// direction
				if( pwmDriveLeftVal <= ( PWM_MAX_VAL - PWM_CHANGE_VAL ) )
				{
					pwmDriveLeftVal += PWM_CHANGE_VAL;
					Log.v( TAG, "New left pwm val: " + pwmDriveLeftVal );
				}
			}
		});

		btnDriveRightRev.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				// Subtract to reverse as servo is mounted opposite
				// to positive direction
				if( pwmDriveRightVal <= ( PWM_MAX_VAL - PWM_CHANGE_VAL ) )
				{
					pwmDriveRightVal += PWM_CHANGE_VAL;
					Log.v( TAG, "New right pwm val: " + pwmDriveRightVal );
				}

			}
		});

		btnDriveLeftRev.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				// Add to reverse as servo is mounted in positive
				// direction
				if( pwmDriveLeftVal >= ( PWM_MIN_VAL + PWM_CHANGE_VAL ) )
				{
					pwmDriveLeftVal -= PWM_CHANGE_VAL;
					Log.v( TAG, "New left pwm val: " + pwmDriveLeftVal );
				}
			}
		});

		btnForkliftUp.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				if( pwmForkliftVal <= ( PWM_MAX_VAL - PWM_CHANGE_VAL ) )
				{
					pwmForkliftVal += PWM_CHANGE_VAL;
					Log.v( TAG, "New forklift pwm val: " + pwmForkliftVal );
				}
			}
		});

		btnForkliftDown.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				if( pwmForkliftVal >= ( PWM_MIN_VAL + PWM_CHANGE_VAL ) )
				{
					pwmForkliftVal -= PWM_CHANGE_VAL;
					Log.v( TAG, "New forklift pwm val: " + pwmForkliftVal );
				}
			}
		});

		btnCameraPanLeft.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				if( pwmCameraPanVal <= ( PWM_MAX_VAL - PWM_CHANGE_VAL ) )
				{
					pwmCameraPanVal += PWM_CHANGE_VAL;
					Log.v( TAG, "New camera pan pwm val: " + pwmCameraPanVal );
				}
			}
		});

		btnCameraPanRight.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				if( pwmCameraPanVal >= ( PWM_MIN_VAL + PWM_CHANGE_VAL ) )
				{
					pwmCameraPanVal -= PWM_CHANGE_VAL;
					Log.v( TAG, "New camera pan pwm val: " + pwmCameraPanVal );
				}
			}
		});

		// End MDR Specific Buttons

		enableUi(true);
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		setNumber(input_.read());
		led_.write(!toggleButton_.isChecked());

		pwmDriveRight.setPulseWidth( pwmDriveRightVal );
		pwmDriveLeft.setPulseWidth( pwmDriveLeftVal );
		pwmForklift.setPulseWidth( pwmForkliftVal );
		pwmCameraPan.setPulseWidth( pwmCameraPanVal );

		Thread.sleep(10);
	}

	@Override
	public void disconnected() {
		enableUi(false);
	}

	public AnalogInput getInput_() {
		return input_;
	}

	public void setInput_(AnalogInput input_) {
		this.input_ = input_;
	}

	public DigitalOutput getLed_() {
		return led_;
	}

	public void setLed_(DigitalOutput led_) {
		this.led_ = led_;
	}

	public PwmOutput getPwmForklift() {
		return pwmForklift;
	}

	public void setPwmForklift(PwmOutput pwmForklift) {
		this.pwmForklift = pwmForklift;
	}

	public PwmOutput getPwmDriveRight() {
		return pwmDriveRight;
	}

	public void setPwmDriveRight(PwmOutput pwmDriveRight) {
		this.pwmDriveRight = pwmDriveRight;
	}

	public PwmOutput getPwmDriveLeft() {
		return pwmDriveLeft;
	}

	public void setPwmDriveLeft(PwmOutput pwmDriveLeft) {
		this.pwmDriveLeft = pwmDriveLeft;
	}

	public PwmOutput getPwmCameraPan() {
		return pwmCameraPan;
	}

	public void setPwmCameraPan(PwmOutput pwmCameraPan) {
		this.pwmCameraPan = pwmCameraPan;
	}

	public int getPWM_CENTER_VAL() {
		return PWM_CENTER_VAL;
	}

	public int getPWM_MIN_VAL() {
		return PWM_MIN_VAL;
	}

	public int getPWM_MAX_VAL() {
		return PWM_MAX_VAL;
	}

	public int getPWM_CHANGE_VAL() {
		return PWM_CHANGE_VAL;
	}

	public int getPwmDriveRightVal() {
		return pwmDriveRightVal;
	}

	public void setPwmDriveRightVal(int pwmDriveRightVal) {
		this.pwmDriveRightVal = pwmDriveRightVal;
	}

	public int getPwmDriveLeftVal() {
		return pwmDriveLeftVal;
	}

	public void setPwmDriveLeftVal(int pwmDriveLeftVal) {
		this.pwmDriveLeftVal = pwmDriveLeftVal;
	}

	public int getPwmForkliftVal() {
		return pwmForkliftVal;
	}

	public void setPwmForkliftVal(int pwmForkliftVal) {
		this.pwmForkliftVal = pwmForkliftVal;
	}

	public int getPwmCameraPanVal() {
		return pwmCameraPanVal;
	}

	public void setPwmCameraPanVal(int pwmCameraPanVal) {
		this.pwmCameraPanVal = pwmCameraPanVal;
	}

	public String getTAG() {
		return TAG;
	}


	public void setpwmDriveLeftVal(int pwmDriveLeftVal) {
		this.pwmDriveLeftVal = pwmDriveLeftVal;
	}

	public int getpwmDriveRightVal() {
		return pwmDriveRightVal;
	}

	public int getpwmDriveLeftVal() {
		return pwmDriveLeftVal;
	}
}