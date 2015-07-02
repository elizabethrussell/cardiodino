package com.datadoghealth.cardiodino.bluetooth;

import java.util.List;

import com.datadoghealth.cardiodino.AStart;
import com.datadoghealth.cardiodino.Levels;
import com.datadoghealth.cardiodino.R;
import com.datadoghealth.cardiodino.core.UniBus;
import com.datadoghealth.cardiodino.util.HR;
import com.datadoghealth.cardiodino.util.SharedPrefs;
import com.squareup.otto.Subscribe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import static com.datadoghealth.cardiodino.core.UniBus.*;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 *
 * ECR via Android Open Source project
 */
public class BluetoothAfterScan extends Activity {
    private final static String TAG = BluetoothAfterScan.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private boolean movedOn = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private Context context;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        // Defines callbacks for service binding, passed to bindservice()...  i.e. should run when service connected
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            Log.i(TAG, "OnServiceConnected callback");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) binder).getService();
            Log.i(TAG, "mBluetoothService initialized");
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onservicedisconnected callback");
            mBluetoothLeService = null;
        }
    };


    @Subscribe
    public void connectivityChange(String s) {
        switch(s) {
            case BluetoothLeService.ACTION_GATT_CONNECTED:
                mConnected = true;
                break;
            case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                mConnected=false;
                break;
            case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                gattSwitchboard(mBluetoothLeService.getSupportedGattServices());
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                //if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    //TODO: reset bluetooth here
                break;
        }
    }

    @Subscribe
    public void onHrReceived(HR hr) { exit();}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_wait_for_connection);

        UniBus.get().register(this);

        // Connect to some device address
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mDeviceAddress = prefs.getString(SharedPrefs.BLUETOOTH_DEVICE_ADDRESS, "");

        // check bluetooth permission
        checkBluetooth(this);


        // starting (binding) the service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        getApplicationContext().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        // launch not connected activity if it doesn't connect after 15 seconds//2 minutes
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (!movedOn) {
                    Intent i = new Intent(context, BluetoothConnectFailed.class);
                    startActivity(i);
                }
            }
        }, 10*60000);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothLeService != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            mDeviceAddress = prefs.getString(SharedPrefs.BLUETOOTH_DEVICE_ADDRESS, "");
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    protected void onStop() {
        super.onStop();
        movedOn = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        movedOn = true;
        if (mConnected) {
            try {
                unbindService(mServiceConnection);
            } catch (IllegalArgumentException e) {
                Log.e("IllArgExcept.\n",e.getMessage());
            }
        }
        mBluetoothLeService = null;
    }



    /**
     * Iterate through list of available gatt services 
     * @param gattServices services
     */
    private void gattSwitchboard(List<BluetoothGattService> gattServices) {

        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            Log.i("GattService",gattService.toString());

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                String uuid = gattCharacteristic.getUuid().toString();
                if (GattAttributes.isHr(uuid)) {
                    final int charaProp = gattCharacteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ)>0 ) {
                        // clear active notification if there is one
                        if (mNotifyCharacteristic != null ) {
                            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                            mBluetoothLeService.readCharacteristic(gattCharacteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) >0) {
                            mNotifyCharacteristic = gattCharacteristic;
                            mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                        }
                    }
                }
            }
        }
    }


    public static boolean checkBluetooth(Context c) {
        if (!c.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(c, "Bluetooth Not Supported", Toast.LENGTH_SHORT)
                    .show();
        } else {
            final BluetoothManager bluetoothManager = (BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
            // if ble not enabled, display dialog for user to enable
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                c.startActivity(enableBtIntent);
                Toast.makeText(c, "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
            }

            else {
                return true;
            }
        }
        return false;
    }

    public void exit() {
        if (!movedOn) {
            final Intent intent = new Intent(this, Levels.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
        }
    }

}
