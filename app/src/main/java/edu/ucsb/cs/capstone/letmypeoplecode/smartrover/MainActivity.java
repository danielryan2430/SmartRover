package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends ActionBarActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private SharedPreferences sharedPref;
    private BeaconManager bManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }else{
            return;
        }


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

        if(bManager != null)
            return;
        bManager = new BeaconManager();
        bManager.setBTadapter(mBluetoothAdapter);
		bManager.loadState(this);
        double gimbalD0 = 60.8;
		double ibeaconD0 = 65.8;

        try{
            bManager.addBeacon("FYTM-BTYFT", 	new Beacon(0,		0,		7.92,	gimbalD0));		//-61.0
			bManager.addBeacon("1NNH-VT376", 	new Beacon(0,		3.9167,	7.92,	gimbalD0));		//-60.8
			bManager.addBeacon("M: 17, m: 18",	new Beacon(4.0,		-1.875,	5.7325,	ibeaconD0)); 	//-67.3
			bManager.addBeacon("M: 33, m: 34",	new Beacon(4.0,		0,		7.92,	ibeaconD0));		//-66.3
			bManager.addBeacon("M: 49, m: 50",	new Beacon(4.0,		3.9167,	7.92,	ibeaconD0));		//-65.8
			bManager.addBeacon("ZT9M-3AJNE",	new Beacon(8.229,	0,		7.92,	gimbalD0));		//
			bManager.addBeacon("NE6T-Y7K5T", 	new Beacon(8.229,	3.833,	7.92,	gimbalD0)); 	//-62.6
            bManager.startBTupdating();
        }catch(BeaconError e){
            Log.e("bt","Bluetooth scanning fucked up",e);
        }

		ListView bList = (ListView) findViewById(R.id.beaconListView);
		ArrayAdapter<Beacon> adapter = new ArrayAdapter<Beacon>(this,R.layout.list_item,bManager.beaconList);
		bList.setAdapter(adapter);
		bManager.listViewEnable(adapter,this);
        bManager.logEnable();
    }

	public void onPause(){
		if(bManager!=null)
			bManager.saveState(this);
		super.onPause();
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
