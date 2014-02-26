package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;

//Exception class used for flagrant failures
public class BeaconError extends Exception {
    public BeaconError(String message) {
        super(message);
    }
}
