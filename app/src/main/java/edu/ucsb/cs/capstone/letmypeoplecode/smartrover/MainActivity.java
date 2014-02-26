package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;

<<<<<<< HEAD
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
=======
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import java.lang.Math;
import android.os.Parcelable;
import android.preference.PreferenceManager;
>>>>>>> 3dfee154092bdc69b80557a995edca97d2f7d7fa
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
<<<<<<< HEAD
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends ActionBarActivity {
//    private MapView imgView;
=======
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

public class MainActivity extends ActionBarActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private SharedPreferences sharedPref;
    private BeaconManager BTManager;
    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                 Beacon device2= new Beacon(0,0,0,0);
//                out.append("\n  Device: " + device.getName() + ", " + device);
                try{
                    BTManager.addBeacon("corner1",device2);
                   }
                catch(BeaconError e)
                {}
            } else {
                if(BluetoothDevice.ACTION_UUID.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    ParcelUuid thing[] = device.getUuids();
                    Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    for (int i=0;uuidExtra!=null && i<uuidExtra.length; i++) {
                        Log.d("bt_scan_results", "Device: " + device.getName() + ", " + device + ", Service: " + uuidExtra[i].toString());
                    }
                }
            }
        }
    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override

                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    String ass = device.getName();
                    String addr = device.getAddress();
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
                    final double distance=(Math.exp(((rssi+(9.10396133590131))/-12.864335093782)));

                    String uniqueID=("M: "+Short.toString(major)+", m: "+Short.toString(minor));

                    /*if((uniqueID).equals("M: 7182, m: 12144"))
                        {
                        Log.d("success","detected corner beacon");
                        try{
                            Beacon corner1=new Beacon(0,0,0);
                            BTManager.addBeacon("corner1",corner1);
                            }
                            catch(BeaconError e){}
                        }*/
                    //Log.d("bt_scan_results",device.getUuids());

                    /*Beacon corner1=new Beacon(0,0,0);
                    try{
                        BTManager.addBeacon(device.getName(),corner1);
                        }
                    catch(BeaconError e)
                        {
                        }*/
                    //BTManager.updateDistance(device.getName(), distance);

                    long waitPeriod=2000;
                    waitPeriod = (long)(Math.exp(-0.1151292546 * rssi - 4.029523913)*1.2 + 100);
                    Log.d("bt_scan_results", device.toString() + " " + Integer.toString(rssi) + " " + waitPeriod);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.Infolog)).setText("Distance: "+Double.toString(distance)+"M: "+Short.toString(major)+", m: "+Short.toString(minor));
                        }
                    });
                }
            };





>>>>>>> 3dfee154092bdc69b80557a995edca97d2f7d7fa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();

        }
<<<<<<< HEAD
        try{
//        int drawableID = getResources().getIdentifier("ic_launcher", "drawable", getPackageName());
//        Resources rsrc= getResources();
//        Bitmap bm = BitmapFactory.decodeResource(rsrc, drawableID, new BitmapFactory.Options());
//        imgView = (MapView) findViewById(R.id.mapImage);
//        imgView.setImageBitmap(bm);
;
        }
        catch (Exception e){

        }

=======

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

       this.sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_UUID);

        registerReceiver(ActionFoundReceiver, filter);

        this.scanRepeatedly();
    }





    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public void setSharedPref(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }




    public void scanRepeatedly(){
        final Thread scanThread = new Thread(new Runnable(){
            public void run(){
                Log.d("bt_scan_results","im in a thread");
                while(true){
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    try{
                        Thread.sleep(500);
                    }catch(InterruptedException e){
                        //NOTHING IS WRONG HO HO HO
                    }
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }
        });
        scanThread.start();
>>>>>>> 3dfee154092bdc69b80557a995edca97d2f7d7fa
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
