package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;


import android.graphics.Point;

/**
 *  A generic container for a beacon
 *  Contains the following:
 *      -The beacon location, in some made up coordinate space
 *      -Calibration parameters:
 *      -n, the path loss exponent (this should be global)
 *      -Pl0, path loss at known distance
 *
 */

public class Beacon {
    private Boolean test=false;       //Init with fake values?
    private double distance;    //Hard coded distance for testing
    private Point3D myLocation;

    public Beacon(Point3D beaconLocation){
        myLocation = beaconLocation;
    }

    public Beacon(double x,double y,double z){
        myLocation = new Point3D(x,y,z);
    }

    //For testing
    public Beacon(double x,double y,double z,double hardCodeDist){
        myLocation = new Point3D(x,y,z);
        setDistance_test(hardCodeDist);
    }

    //For testing only
    public void setDistance_test(double d){
        this.test=true;
        this.distance = d;
    }

    public double getDistance(){
        //Get the RSSI for this beacon and compute the approximate distance
        if(test)
            return distance;


        return 0;
    }

    public Point3D getLocation(){
        return myLocation;
    }
}
