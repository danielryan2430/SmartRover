package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;


import android.animation.IntEvaluator;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
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
	private String id; 		//The unique ID to refer to this beacon
	private Boolean staleValue = true; 	//Is this RSSI value fresh?
    private Boolean test = false;       //Init with fake values?
    private double distance;    //Hard coded distance for testing
    private Point3D myLocation;
    private double pathLoss_d0 = 42;     //path loss at a distance of d0 foot. Will depend on the beacon power and the phone
	private double d0 = 3.0; 			//The aforementioned distance
	//^^THIS IS PATH LOSS, NOT RSSI (so just negate RSSI for all practical purposes)

    private int myRSSI;

    private static int avgSize = 50;
    private static Boolean enableMovingAvg = true;
	private ConcurrentLinkedQueue<Integer> movingAvg;
	private Integer sumSoFar = 0;

    public Beacon(Point3D beaconLocation) {
        myLocation = beaconLocation;
        otherConstruct();
    }

    public Beacon(double x, double y, double z) {
        myLocation = new Point3D(x, y, z);
        otherConstruct();
    }

    public Beacon(double x,double y,double z,double lossD0){
        myLocation = new Point3D(x, y, z);
		pathLoss_d0 = lossD0;
        otherConstruct();
    }

    //For testing purposes only
//    public Beacon(double x, double y, double z, double hardCodeDist) {
//        myLocation = new Point3D(x, y, z);
//        setDistance_test(hardCodeDist);
//    }

    //FOR TESTING ONLY
    public void setDistance_test(double d) {
        this.test = true;
        this.distance = d;
    }

    public void setRSSI(int rssi){
//        myRSSI=rssi;
        if(enableMovingAvg){
			sumSoFar -= movingAvg.remove();
			sumSoFar += rssi;
			movingAvg.add(rssi);
        }else{
            myRSSI = rssi;
        }
		staleValue=false;
    }

	public void setID(String nID){
		this.id = nID;
	}

	public Boolean isStale(){
		return staleValue;
	}

    public double computeDistance(double pathLossExp){
        //Get the RSSI for this beacon and compute the approximate distance
        //The beacon knows its own initial path loss, but not the path loss exponent (depends on the room)
        if (test)
            return distance;

		staleValue = true;
        return d0 * Math.exp(0.2302585093 * (-getRSSI() - pathLoss_d0) / pathLossExp );
    }

    public Point3D getLocation() {
        return myLocation;
    }

    public double getRSSI(){
		if(!enableMovingAvg)
			return myRSSI;
		return (double)sumSoFar / avgSize;
    }


	public String toString(){
		double avg_rssi = getRSSI();
		return String.format("%-12s %2.3f %2.2f",id,avg_rssi,stdDeviation(avg_rssi));
	}

    private void otherConstruct(){
        movingAvg = new ConcurrentLinkedQueue<Integer>();
        for(int i=0;i<avgSize;i++){
            movingAvg.add(0);
        }
    }

	private double stdDeviation(double mean){
		double var=0;
		double d;
		for(double v : movingAvg){
			d = (v-mean);
			var += d*d;
		}
		return Math.sqrt(var/avgSize);
	}
}

