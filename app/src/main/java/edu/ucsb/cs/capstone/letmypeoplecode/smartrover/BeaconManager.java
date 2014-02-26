package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;


import java.util.ArrayList;
import java.util.HashMap;

import Jama.Matrix;
import Jama.QRDecomposition;

/*
Container for all beacons
Can do trilateration to compute our approximate location with regards to the beacons
You update the RSSI information on the beacons using this class.
They can't update themselves because the scanning for all beacons is handled in one place
*/

public class BeaconManager {
    private HashMap<String, Beacon> beaconTable;     //Hash table to store beacons. Key is probably the UUID

    public BeaconManager() {
        beaconTable = new HashMap<String, Beacon>();
    }

    public void addBeacon(String tag, Beacon b) throws BeaconError {
        if (beaconTable.containsKey(tag))
            throw new BeaconError("You are adding a duplicate beacon");
        beaconTable.put(tag, b);
    }

    //TODO
    public void updateRSSI(String whichBeacon, int rssi) {
        //This is how each beacon gets updated
        //And the updating will be going on in another thread
        //We might have to switch to a thread-safe hash table implementation
    }

    public Point3D doTrilateration() throws BeaconError {
        int numBeacons = beaconTable.size();
        if (numBeacons < 3)
            throw new BeaconError("You need at least 4 beacons for trilateration");

        //Coefficient and constant matrix before transformations
        double[][] M = new double[numBeacons][4];


        int row = 0;
        int col;
        for (Beacon bcn : beaconTable.values()) {
            //Construct matrix
            Point3D bpoint = bcn.getLocation();
            for (col = 0; col < 3; col++) {
                M[row][col] = -2.0 * bpoint.get(col);
            }

            double d = bcn.getDistance();       //The distance to this beacon
            M[row][3] = -(bpoint.x() * bpoint.x() + bpoint.y() * bpoint.y() + bpoint.z() * bpoint.z()) + d * d;
            row++;
        }

        //Matrix is constructed, now subtract off quadratic terms and linearize the whole thing
        //Each equation is now a combination of 2 previous ones
        //This means we lose one in order to keep the system linearly independent

        Matrix A = new Matrix(numBeacons - 1, 3);      //Coefficients
        Matrix B = new Matrix(numBeacons - 1, 1);      //Column vector of constants

        for (row = 0; row < numBeacons - 1; row++) {
            for (col = 0; col < 3; col++) {
                A.set(row, col, M[row][col] - M[row + 1][col]);
            }
            double constant = M[row][3] - M[row + 1][3];
            B.set(row, 0, constant);
        }

        //This probably means the z value cannot be determined
        //Understandable if all beacons are at the same height
        int rank = A.rank();
        if (rank < 3) {
            if (rank <= 1)
                throw new BeaconError("Trilateration result is undefined");
            for (row = 0; row < numBeacons - 1; row++) {
                //Assumption is all z values got subtracted out...if this isn't the case then uh oh
                if (Math.abs(A.get(row, 2) - 0.0) > 0.00001) {
                    throw new BeaconError("Matrix is rank deficient and we don't know why :(");
                }
            }

            A = A.getMatrix(0, numBeacons - 2, 0, 1);    //Throw out z values
        }
        QRDecomposition qr = new QRDecomposition(A);
        Matrix soln = qr.solve(B);
        if (rank < 3)
            return new Point3D(soln.get(0, 0), soln.get(1, 0), 0.0);
        else
            return new Point3D(soln.get(0, 0), soln.get(1, 0), soln.get(2, 0));
    }
}

