package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;


import android.animation.IntEvaluator;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * A generic container for a beacon
 * Contains the following:
 * -The beacon location, in some made up coordinate space
 * -Calibration parameters:
 * -n, the path loss exponent (this should be global)
 * -Pl0, path loss at known distance
 */

public class Beacon {
    private Boolean test = false;       //Init with fake values?
    private double distance;    //Hard coded distance for testing
    private Point3D myLocation;
    private double pathLoss_d0 = 42;     //path loss at a distance of 1 foot. Will depend on the beacon power and the phone
    private int myRSSI;
    private static int avgSize = 12;
    private static Boolean enableMovingAvg = true;
    private Semaphore changeRSSIlock;
    //^^THIS IS PATH LOSS, NOT RSSI (so just negate RSSI for all practical purposes)

    private ArrayList<Integer> movingAvg;

    public Beacon(Point3D beaconLocation) {
        myLocation = beaconLocation;
        otherConstruct();
    }

    public Beacon(double x, double y, double z) {
        myLocation = new Point3D(x, y, z);
        otherConstruct();
    }

    //For testing purposes only
    public Beacon(double x, double y, double z, double hardCodeDist) {
        myLocation = new Point3D(x, y, z);
        setDistance_test(hardCodeDist);
    }

    //FOR TESTING ONLY
    public void setDistance_test(double d) {
        this.test = true;
        this.distance = d;
    }

    public void setRSSI(int rssi){
//        myRSSI=rssi;
        if(enableMovingAvg){
            try{changeRSSIlock.acquire();}catch(InterruptedException e){}
            movingAvg.remove(0);
            movingAvg.add(rssi);
            changeRSSIlock.release();
        }else{
            myRSSI = rssi;
        }
    }


    public double computeDistance(double pathLossExp){
        //Get the RSSI for this beacon and compute the approximate distance
        //The beacon knows its own initial path loss, but not the path loss exponent (depends on the room)
        if (test)
            return distance;

        return Math.exp(0.2302585093 * (-getRSSI() - pathLoss_d0) / pathLossExp );
    }

    public Point3D getLocation() {
        return myLocation;
    }

    public double getRSSI(){
        if(enableMovingAvg){
            try{changeRSSIlock.acquire();}catch(InterruptedException e){}
            double avg=0;
            for(double v : movingAvg)
                avg += v;
            avg /= avgSize;
            changeRSSIlock.release();
            return avg;
        }else{
            return myRSSI;
        }
    }

    private void otherConstruct(){
        movingAvg = new ArrayList<Integer>();
        for(int i=0;i<avgSize;i++){
            movingAvg.add(0);
        }
        changeRSSIlock = new Semaphore(1);
    }
}
