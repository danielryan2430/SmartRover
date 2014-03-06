package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.ConditionVariable;
import android.util.Log;
import android.util.Xml;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import Jama.Matrix;
import Jama.QRDecomposition;

/*
Container for all beacons
Can do trilateration to compute our approximate location with regards to the beacons
You update the RSSI information on the beacons using this class.
They can't update themselves because the scanning for all beacons is handled in one place
*/

public class BeaconManager {
    private final ConcurrentHashMap<String, Beacon> beaconTable;     //Hash table to store beacons. Key is probably the UUID
    private final ConcurrentHashMap<String, String> gimbalResolves;      //Cache of Gimbal IDs resolved to their unique ID, so we don't have to spam
    private final ConcurrentLinkedQueue<String> resolveQueue;    //A queue of Gimbal IDs that have yet to be resolved. The network thread picks these up
	public final ArrayList<Beacon> beaconList; 		//Which we unfortunately need to update the listview
    private final Semaphore readyIDCount;               //How many IDs need to be resolved?

    //The path loss exponent. Higher values = more loss with distance
    //Should be relatively constant for a given area
    private double pathLossExp = 2.23;	 //2.962122621
    private BluetoothAdapter BTadapter;

    private double gimbalSum=0.0;
	private double otherSum=0.0;
	private int scanCount=0;

	private ArrayAdapter<Beacon> listviewAdapter = null; 	//Oh god what is happening to my beautiful class
	private Activity outsideActivity; 						//UI stuff is starting to infect everything oh no

    //First 4 bytes of each advertising packet
    //They're all unique, should be enough to identify them
//    0x02011a1a
//    0x1107ad77

    private static byte[] estimoteStartId = {0x02,0x01,0x1a,0x1a};
    private static byte[] gimbalStartId = {0x11,0x07,(byte)0xad,0x77};

    /**** Callbacks and threads *****/

    //Bluetooth scanning callback (call this when a new device comes in)
    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override

                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {

                    String scanRec = "";
                    for(byte i : scanRecord){
                        String digit = Integer.toHexString((i+256)%256);
                        if(digit.length()==1)
                            digit = "0" + digit;
                        scanRec += digit + ":";
                    }
                    String uniqueID = "";
                    try{
                        if(isGimbal(scanRecord)){
                            uniqueID = resolveGimbal(scanRecord);
                            Log.i("bt_scan_results","Gimbal " + uniqueID + " " + rssi);
                        }else{
                            //Assume estimote or something else supported
                            uniqueID = resolve_iBeacon(scanRecord);
                            Log.i("bt_scan_results","iBeacon " + uniqueID + " " + rssi);
                        }
                        updateRSSI(uniqueID,rssi);
						scanCount++;
                    }catch(BeaconError e){
                        Log.w("bt_error","Beacon update error",e);
                    }
                }
            };

    //Thread doing all the network stuff and resolving Gimbals
    private Thread resolveThread = new Thread(new Runnable() {
        @Override
        public void run() {
//            String requestURL = "https://api.getfyx.com/api/mbr/v1/transmitters/resolve/" + idToResolve +  "/lookahead/24?service_id=960C4A8A244C11E2B29900A0C60077AD&access_token=a05689bd69fe08e059eef111a38e1cf4a7817e99bf8db6f0b62397c335b43c7d";
            while(true){
                try{
                    readyIDCount.acquire();
                    if(resolveQueue.isEmpty())
                        throw new BeaconError("Thread unblocked, but no IDs to resolve");
                    String idToResolve = resolveQueue.peek();
                    String requestURL = "https://api.getfyx.com/api/mbr/v1/transmitters/resolve/" + idToResolve +  "/lookahead/24?service_id=960C4A8A244C11E2B29900A0C60077AD&access_token=a05689bd69fe08e059eef111a38e1cf4a7817e99bf8db6f0b62397c335b43c7d";
                    JSONObject gimbalResponse = readJsonFromUrl(requestURL);
                    String thisGimbal = gimbalResponse.getString("identifier").toUpperCase();
                    gimbalResolves.put(idToResolve,thisGimbal);
                    JSONArray moreResolves = gimbalResponse.getJSONArray("lookup_keys");
                    for(int i=0;i<moreResolves.length();i++){
                        String anotherResolve = moreResolves.getString(i);
                        gimbalResolves.put(anotherResolve,thisGimbal);
                    }
                    resolveQueue.remove(idToResolve);   //Actually remove it because we successfully found it
                    continue;
                }catch(Throwable e){
                    Log.e("bt_error","Gimbal resolve error, retrying",e);
                    readyIDCount.release();     //V it again so we can retry
                }
            }
        }
    });

    //Thread to start and stop scanning every once in a while (if we need it)
    private Thread btScanThread = new Thread(new Runnable(){
        public void run(){
            Log.d("bt_scan_results","im in a thread");
            while(true){
                BTadapter.startLeScan(leScanCallback);
                try{
                    Thread.sleep(200);
                }catch(InterruptedException e){
                    //NOTHING IS WRONG HO HO HO
                }
                BTadapter.stopLeScan(leScanCallback);
            }
        }
    });
    /**** End callbacks and threads *****/



    //Convert the minor/major values into a unique string used for lookup stuff
    public static String iBeaconKeyString(short major,short minor){
        return "M: " + major + ", m: " + minor;
    }

    public BeaconManager() {
        beaconTable = new ConcurrentHashMap<String, Beacon>();
        gimbalResolves = new ConcurrentHashMap<String, String>();
        resolveQueue = new ConcurrentLinkedQueue<String>();
		beaconList = new ArrayList<Beacon>();
        readyIDCount = new Semaphore(0);
        resolveThread.start();
    }

    public void logEnable(){
        Thread logThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
					try{
						Thread.sleep(1000);
					}catch(InterruptedException e){}
                    Enumeration<Beacon> en = beaconTable.elements();
					Boolean freshValues = true;
                    while(en.hasMoreElements()){
                        Beacon b = en.nextElement();
						if(b.isStale()){
							freshValues=false;
							break;
						}
                    }
					if(!freshValues)
						continue;
                    try{
						final Point3D result = doTrilateration();
                        Log.i("bt_trilat","Trilateration: " + result);
//						outsideActivity.runOnUiThread(new Runnable() {
//							@Override
//							public void run() {
//								TextView tresults = (TextView) outsideActivity.findViewById(R.id.trilatResultView);
//								tresults.setText(result.toString());
//								if(listviewAdapter!=null)
//									listviewAdapter.notifyDataSetChanged();
//							}
//						});
						en = beaconTable.elements();
						while(en.hasMoreElements()){
							Beacon b = en.nextElement();
							Log.i("bt_trilat","Beacon:"+b.getLocation() + "		RSSI:" + b.getRSSI() + "		Dist:" + b.computeDistance(pathLossExp));
						}
                    }catch(BeaconError e){
                        Log.w("bt_error","Trilateration error",e);
                    }

                }
            }
        });
        logThread.start();
    }

	//Save parts of the state to shared prefs
	//Because resolving gimbals is balls slow man
	public void saveState(Context app){
		if(!resolveQueue.isEmpty())
			return; 	//We want everything to be resolved before saving
		try{
			FileOutputStream saveFile = app.openFileOutput("gimbal_resolves.xml",Context.MODE_PRIVATE);
			XmlSerializer serial = Xml.newSerializer();
			serial.setOutput(saveFile,"UTF-8");
			serial.startDocument(null,Boolean.TRUE);
			Enumeration<String> en = gimbalResolves.keys();
			serial.startTag(null,"root");
			while(en.hasMoreElements()){
				String id = en.nextElement();
				String gimbal = gimbalResolves.get(id);
				serial.startTag(null,"entry");
				serial.startTag(null,"key");
				serial.text(id);
				serial.endTag(null,"key");
				serial.startTag(null,"value");
				serial.text(gimbal);
				serial.endTag(null,"value");
				serial.endTag(null,"entry");
			}
			serial.endDocument();
			serial.flush();
			saveFile.close();
		}catch(IOException e){
			Log.e("bt_error","Save state error",e);
		}
	}

	public void loadState(Context app){
		//Massive waste of memory and a bunch of boilerplate
		//Java at its finest
		try{
			FileInputStream saveFile = app.openFileInput("gimbal_resolves.xml");
			InputStreamReader infile = new InputStreamReader(saveFile);
			char[] buffer = new char[saveFile.available()];
			infile.read(buffer);
			String data = new String(buffer);
			saveFile.close();
			infile.close();
			InputStream is = new ByteArrayInputStream(data.getBytes("UTF-8"));
			DocumentBuilder doc = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document dom = doc.parse(is);
			dom.getDocumentElement().normalize();
			NodeList items = dom.getFirstChild().getChildNodes();
			for(int i=0;i<items.getLength();i++){
				Node entry = items.item(i);
				String key=entry.getFirstChild().getFirstChild().getNodeValue();
				String value=entry.getLastChild().getFirstChild().getNodeValue();
				gimbalResolves.put(key,value);
			}
		}catch(IOException e){
			Log.e("bt_error","Error loading state",e);
		}catch(ParserConfigurationException e){
			Log.e("bt_error","Bad save state xml",e);
		}catch(SAXException e){
			Log.e("bt_error","Bad xml",e);
		}
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
		beaconList.add(b);
		b.setID(tag);
    }

	//Dump beacons that we've found into a listview
	public void listViewEnable(ArrayAdapter<Beacon> adapter,Activity app){
		listviewAdapter = adapter;
		outsideActivity = app;
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

    //Is this advertising packet from an Estimote?
    private Boolean isEstimote(byte[] scanRecord){
        if(scanRecord.length < 29)
            return false;
        return checkSRagainst(scanRecord,estimoteStartId);
    }

    //From a Gimbal?
    private Boolean isGimbal(byte[] scanRecord){
        if(scanRecord.length < 31)
            return false;
        return checkSRagainst(scanRecord,gimbalStartId);
    }

    private Boolean checkSRagainst(byte[] scanRecord,byte[] checkAgainst){
        for(int i=0;i<4;i++)
            if(checkAgainst[i] != scanRecord[i])
                return false;
        return true;
    }

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

    //The Gimbal beacon is hidden in the rest of the advertising packet
    //Extract it and resolve it with their server
    private String resolveGimbal(byte[] ScanRecord) throws BeaconError{
        if(ScanRecord.length < 31)
            throw new BeaconError("Invalid advertising packet for Gimbal");
        ByteBuffer pktBytes = ByteBuffer.allocate(31);
        for(int i=0;i<31;i++){
            pktBytes.put(ScanRecord[i]);
        }
//        if(pktBytes.getInt() != gimbalStartId)
//            throw new BeaconError("Wrong header for Gimbal");
        pktBytes.position(20);
        String id = "";
        byte thisByte;
        for(int i=0;i<11;i++){
            thisByte = pktBytes.get();
            if((thisByte & 0xF0) == 0)
                id += "0";
            id += Integer.toHexString((thisByte+256)%256);
        }
        if(id.length()!=22)
            throw new BeaconError("Bad gimbal ID");
        final String idToResolve = id.toUpperCase();

        if(gimbalResolves.containsKey(idToResolve)){
            String resd = gimbalResolves.get(idToResolve);
            if(resd=="RESOLVING")
                throw new BeaconError("Not done resolving yet");
            return resd;
        }

        //Gimbal ID is not in our database, query the server
        gimbalResolves.put(idToResolve,"RESOLVING");
//        final String url = "http://ip.jsontest.com/";
        Log.i("bt_scan_results","Resolving Gimbal ID 0x" + idToResolve);
        resolveQueue.add(idToResolve);  //Add this to the queue
        readyIDCount.release();     //V that semaphore (yeah I actually like Dijkstra's notation better...'release' does not make sense here)
        throw new BeaconError("Resolve in progress...");    //Don't return anything useful
    }

    private String resolve_iBeacon(byte[] scanRecord){
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
        return "M: "+Short.toString(major)+", m: "+Short.toString(minor);
    }

    //Shamelessly stolen from StackOverflow
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

}



