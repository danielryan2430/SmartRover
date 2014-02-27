package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import Jama.Matrix;
import Jama.QRDecomposition;

/*
Container for all beacons
Can do trilateration to compute our approximate location with regards to the beacons
You update the RSSI information on the beacons using this class.
They can't update themselves because the scanning for all beacons is handled in one place
*/

public class BeaconManager {
    private ConcurrentHashMap<String, Beacon> beaconTable;     //Hash table to store beacons. Key is probably the UUID

    //The path loss exponent. Higher values = more loss with distance
    //Should be relatively constant for a given area
    private double pathLossExp = 2.962122621;
    private BluetoothAdapter BTadapter;
    private Thread btScanThread;

    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override

                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    ByteBuffer M = ByteBuffer.allocate(2);
                    M.order(ByteOrder.BIG_ENDIAN);
                    M.put(scanRecord[25]);
                    M.put(scanRecord[26]);
                    final short major=M.getShort(0);

                    ByteBuffer m = ByteBuffer.allocate(2);
                    m.order(ByteOrder.BIG_ENDIAN);
                    m.put(scanRecord[27]);
                    m.put(scanRecord[28]);
                    final short minor=m.getShort(0);
//                    final double distance=(Math.exp(((rssi+(9.10396133590131))/-12.864335093782)));
                    String uniqueID=("M: "+Short.toString(major)+", m: "+Short.toString(minor));
                    try{
                        updateRSSI(uniqueID,rssi);
                    }catch(BeaconError e){
                        Log.w("bt_scan_results","Invalid beacon ID found:" + uniqueID);
                    }
                }
            };

    public BeaconManager() {
        beaconTable = new ConcurrentHashMap<String, Beacon>();
        btScanThread = new Thread(new Runnable(){
            public void run(){
                Log.d("bt_scan_results","im in a thread");
                while(true){
                    BTadapter.startLeScan(leScanCallback);
                    try{
                        Thread.sleep(500);
                    }catch(InterruptedException e){
                        //NOTHING IS WRONG HO HO HO
                    }
                    BTadapter.stopLeScan(leScanCallback);
                }
            }
        });
    }

    public void logEnable(){
        Thread logThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    Enumeration<Beacon> en = beaconTable.elements();
                    while(en.hasMoreElements()){
                        Beacon b = en.nextElement();
                        Point3D l = b.getLocation();
                        Log.i("bt_scan_results",l + " " + b.computeDistance(pathLossExp) + " " + b.getRSSI());
                    }
                    try{
                        Log.i("bt_scan_results","Trilateration: " + doTrilateration());
                    }catch(BeaconError e){
                        Log.w("bt_scan_results","Trilateration error",e);
                    }
                    try{
                        Thread.sleep(1000);
                    }catch(InterruptedException e){}
                }
            }
        });
        logThread.start();
    }

    public void setBTadapter(BluetoothAdapter ad){
        BTadapter=ad;
    }

    public void startBTupdating() throws BeaconError{
        if(BTadapter==null)
            throw new BeaconError("No bluetooth adapter has been set");
        btScanThread.start();
    }

    public void addBeacon(String tag, Beacon b) throws BeaconError {
        if (beaconTable.containsKey(tag))
            throw new BeaconError("You are adding a duplicate beacon");
        beaconTable.put(tag, b);
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

            double d = bcn.computeDistance(pathLossExp);       //The distance to this beacon
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


    /* *** THE PRIVATE SECTION *** */

    private void updateRSSI(String whichBeacon, int rssi) throws BeaconError{
        //This is how each beacon gets updated
        //And the updating will be going on in another thread
        //We might have to switch to a thread-safe hash table implementation
        Beacon UpdateBT= beaconTable.get(whichBeacon);
        if(UpdateBT==null)
            throw new BeaconError("That beacon doesn't exist!");
        UpdateBT.setRSSI(rssi);
//        Log.i("bt_scan_results",whichBeacon + ", rssi: " + rssi);
    }

}

