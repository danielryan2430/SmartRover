package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;

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

import java.io.IOException;
import java.lang.Math;
import android.os.Parcelable;
import android.preference.PreferenceManager;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

public class MainActivity extends IOIOActivity {
    // Start IOIO
    private TextView textView_;
    public static ToggleButton toggleButton_;
    private Button btnDriveRightFwd = null;
    private Button 	  btnDriveLeftFwd = null;
    private Button 	  btnDriveRightRev = null;
    private Button 	  btnDriveLeftRev = null;
    private Button 	  btnForkliftUp = null;
    private Button 	  btnForkliftDown= null;
    private Button 	  btnCameraPanLeft = null;
    private Button 	  btnCameraPanRight= null;
    public static Looper looper = null;
    // End IOIO

    private BluetoothAdapter mBluetoothAdapter;
    private SharedPreferences sharedPref;
    private BeaconManager bManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start IOIO
        textView_ = (TextView) findViewById(R.id.TextView);
        toggleButton_ = (ToggleButton) findViewById(R.id.ToggleButton);

        btnDriveRightFwd = (Button) findViewById( R.id.btnDriveRightFwd );
        btnDriveLeftFwd = (Button) findViewById( R.id.btnDriveLeftFwd );

        btnDriveRightRev = (Button) findViewById( R.id.btnDriveRightRev );
        btnDriveLeftRev = (Button) findViewById( R.id.btnDriveLeftRev );

        btnForkliftUp = (Button) findViewById( R.id.btnForkliftUp );
        btnForkliftDown = (Button) findViewById( R.id.btnForkliftDown );

        btnCameraPanLeft = (Button) findViewById( R.id.btnCameraPanLeft );
        btnCameraPanRight = (Button) findViewById( R.id.btnCameraPanRight );

        enableUi(false);

        new Thread(new Runnable() {
            public void run() {
                HttpServerTranslator httpServerTranslator = new HttpServerTranslator("http://ucsbsmartserver.no-ip.org/rover");
                while (true) {
                    try {
                        httpServerTranslator.parseJson();
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e ) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        // End IOIO


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
    }

    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public void setSharedPref(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
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

    @Override
    protected IOIOLooper createIOIOLooper() {
        MainActivity.looper = new Looper();
        return MainActivity.looper;
    }

    private void enableUi(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleButton_.setEnabled(enable);
                btnDriveRightFwd.setEnabled(enable);
                btnDriveLeftFwd.setEnabled(enable);
                btnDriveRightRev.setEnabled(enable);
                btnDriveLeftRev.setEnabled(enable);
                btnForkliftUp.setEnabled(enable);
                btnForkliftDown.setEnabled(enable);
                btnCameraPanLeft.setEnabled(enable);
                btnCameraPanRight.setEnabled(enable);


            }
        });
    }

    private void setNumber(float f) {
        final String str = String.format("%.2f", f);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView_.setText(str);
            }
        });
    }
    // End IOIO
}

