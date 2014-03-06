package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;

/**
 * Created by dimberman on 3/5/14.
 */
public class RoverController {
    public void forward(){
        if( MainActivity.looper.getPwmDriveLeftVal() <= ( MainActivity.looper.getPWM_MAX_VAL() - MainActivity.looper.getPWM_CHANGE_VAL() ) )
        {
            MainActivity.looper.setpwmDriveLeftVal(MainActivity.looper.getPwmDriveLeftVal() + MainActivity.looper.getPWM_CHANGE_VAL());
        }
        if (MainActivity.looper.getPwmDriveRightVal() >= (MainActivity.looper.getPWM_MIN_VAL() + MainActivity.looper.getPWM_CHANGE_VAL())) {
            MainActivity.looper.setPwmDriveRightVal(MainActivity.looper.getPwmDriveRightVal() - MainActivity.looper.getPWM_CHANGE_VAL());
        }
    }

    public void reverse(){
        if( MainActivity.looper.getpwmDriveLeftVal() >= ( MainActivity.looper.getPWM_MIN_VAL() + MainActivity.looper.getPWM_CHANGE_VAL() ) )
        {
            MainActivity.looper.setpwmDriveLeftVal(MainActivity.looper.getPwmDriveLeftVal() - MainActivity.looper.getPWM_CHANGE_VAL());
        }
        if( MainActivity.looper.getpwmDriveRightVal() <= ( MainActivity.looper.getPWM_MAX_VAL() - MainActivity.looper.getPWM_MAX_VAL() ) )
        {
            MainActivity.looper.setPwmDriveRightVal(MainActivity.looper.getpwmDriveRightVal() + MainActivity.looper.getPWM_CHANGE_VAL());
        }
    }



    public void turnRight(){
        if( MainActivity.looper.getPwmDriveLeftVal() <= ( MainActivity.looper.getPWM_MAX_VAL() - MainActivity.looper.getPWM_CHANGE_VAL() ) )
        {
            MainActivity.looper.setpwmDriveLeftVal(MainActivity.looper.getPwmDriveLeftVal() + MainActivity.looper.getPWM_CHANGE_VAL());
        }
        if( MainActivity.looper.getpwmDriveRightVal() <= ( MainActivity.looper.getPWM_MAX_VAL() - MainActivity.looper.getPWM_MAX_VAL() ) )
        {
            MainActivity.looper.setPwmDriveRightVal(MainActivity.looper.getpwmDriveRightVal() + MainActivity.looper.getPWM_CHANGE_VAL());
        }
    }


    public void turnLeft(){
        if( MainActivity.looper.getpwmDriveLeftVal() >= ( MainActivity.looper.getPWM_MIN_VAL() + MainActivity.looper.getPWM_CHANGE_VAL() ) )
        {
            MainActivity.looper.setpwmDriveLeftVal(MainActivity.looper.getPwmDriveLeftVal() - MainActivity.looper.getPWM_CHANGE_VAL());
        }
        if (MainActivity.looper.getPwmDriveRightVal() >= (MainActivity.looper.getPWM_MIN_VAL() + MainActivity.looper.getPWM_CHANGE_VAL())) {
            MainActivity.looper.setPwmDriveRightVal(MainActivity.looper.getPwmDriveRightVal() - MainActivity.looper.getPWM_CHANGE_VAL());
        }
    }

    public void LED(){
        MainActivity.toggleButton_.setChecked(MainActivity.toggleButton_.isChecked());
    }

    public void forkUp(){
        if( MainActivity.looper.getPwmForkliftVal() <= ( MainActivity.looper.getPWM_MAX_VAL() - MainActivity.looper.getPWM_CHANGE_VAL() ) )
        {
            MainActivity.looper.setPwmForkliftVal(MainActivity.looper.getPwmForkliftVal() + MainActivity.looper.getPWM_CHANGE_VAL());
        }
    }

    public void forkDown(){
        if( MainActivity.looper.getPwmForkliftVal() >= ( MainActivity.looper.getPWM_MIN_VAL() + MainActivity.looper.getPWM_CHANGE_VAL() ) )
        {
            MainActivity.looper.setPwmForkliftVal(MainActivity.looper.getPwmForkliftVal() - MainActivity.looper.getPWM_CHANGE_VAL());
        }
    }

    public void mirrorLeft(){
        if( MainActivity.looper.getPwmCameraPanVal() <= ( MainActivity.looper.getPWM_MAX_VAL() - MainActivity.looper.getPWM_CHANGE_VAL()) );
        {
            MainActivity.looper.setPwmCameraPanVal(MainActivity.looper.getPwmCameraPanVal() + MainActivity.looper.getPwmCameraPanVal());
        }
    }


    public void mirrorRight(){
        if( MainActivity.looper.getPwmCameraPanVal() >= ( MainActivity.looper.getPWM_MIN_VAL() + MainActivity.looper.getPWM_MIN_VAL() ) )
        {
            MainActivity.looper.setPwmCameraPanVal(MainActivity.looper.getPwmCameraPanVal() - MainActivity.looper.getPWM_CHANGE_VAL());
        }
    }
}
