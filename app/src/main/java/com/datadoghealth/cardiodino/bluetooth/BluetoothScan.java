/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datadoghealth.cardiodino.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.datadoghealth.cardiodino.R;
import com.datadoghealth.cardiodino.util.SharedPrefs;
import com.gc.materialdesign.views.ButtonFlat;

import java.lang.Object;import java.lang.Override;import java.lang.Runnable;import java.lang.String;import java.util.ArrayList;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 *
 * ECR via Android Open Source
 */
public class BluetoothScan extends Activity {
    private static final String TAG = BluetoothScan.class.getSimpleName();

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_device);

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.
        // Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "bluetooth not supported", Toast.LENGTH_SHORT)
                    .show();
            finish();
        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "bluetooth not supported", Toast.LENGTH_SHORT)
                    .show();
            finish();
        }


        // Listen for state change to control state of switch
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, intentFilter);
    }


    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        SwitchCompat sc = (SwitchCompat) findViewById(R.id.bluetooth_switch);

        // different behaviors depending on whether bluetooth is enabled
        if (!mBluetoothAdapter.isEnabled()) {
            // set toggle
            sc.setChecked(false);

            // set visibility of card telling user to turn bluetooth on
            CardView cv = (CardView) findViewById(R.id.bluetooth_message);
            cv.setVisibility(View.VISIBLE);
        } else {
            // set toggle
            sc.setChecked(true);


            // populate device list, start scanning
            initializeDeviceList();
        }


    }


    private void initializeDeviceList() {

        // set visibility of card telling user to turn bluetooth on
        CardView cv1 = (CardView) findViewById(R.id.bluetooth_status);
        cv1.setVisibility(View.VISIBLE);
        CardView cv2 = (CardView) findViewById(R.id.devices_card);
        cv2.setVisibility(View.VISIBLE);

        // Initializes list view adapter.
        ListView devicesListView = (ListView) findViewById(R.id.devices_list);
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        devicesListView.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);

        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null)
                    return;
                handleDeviceChoice(device);

            }
        });
    }

    public void switchedBluetooth(View v) {
        SwitchCompat sc = (SwitchCompat) v;
        if (sc.isChecked()) {
            mBluetoothAdapter.enable();
            //sc.setVisibility(View.GONE);
            sc.setClickable(false);
            TextView tv = (TextView) findViewById(R.id.bluetooth_switch_text);
            tv.setText("Enabling...");

        } else {
            mBluetoothAdapter.disable();
            //sc.setVisibility(View.GONE);
            sc.setClickable(false);
            TextView tv = (TextView) findViewById(R.id.bluetooth_switch_text);
            tv.setText("Disabling...");

            bluetoothOffViewUpdate();
        }
    }


    // handle connectivity for bluetooth switch
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent ) {
            final int state = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
            if (state == BluetoothAdapter.STATE_OFF) {
                TextView tv = (TextView) findViewById(R.id.bluetooth_switch_text);
                tv.setText("Bluetooth");
                SwitchCompat sc = (SwitchCompat) findViewById(R.id.bluetooth_switch);
                //sc.setVisibility(View.VISIBLE);
                sc.setClickable(true);
            } else if (state == BluetoothAdapter.STATE_ON) {
                TextView tv = (TextView) findViewById(R.id.bluetooth_switch_text);
                tv.setText("Bluetooth");
                SwitchCompat sc = (SwitchCompat) findViewById(R.id.bluetooth_switch);
                //sc.setVisibility(View.VISIBLE);
                sc.setClickable(true);
                bluetoothOnViewUpdate();
            }
        }
    };


    public void bluetoothOnViewUpdate() {
        CardView cv = (CardView) findViewById(R.id.bluetooth_message);
        cv.setVisibility(View.GONE);
        initializeDeviceList();
    }

    public void bluetoothOffViewUpdate() {
        CardView cv = (CardView) findViewById(R.id.bluetooth_message);
        cv.setVisibility(View.VISIBLE);
        CardView cv1 = (CardView) findViewById(R.id.bluetooth_status);
        cv1.setVisibility(View.GONE);
        CardView cv2 = (CardView) findViewById(R.id.devices_card);
        cv2.setVisibility(View.GONE);
    }

    public void toggleConnecting(View v) {
        ButtonFlat bf = (ButtonFlat)v;
        TextView tv = (TextView) findViewById(R.id.connection_text);
        ProgressBar pb = (ProgressBar) findViewById(R.id.spinny_thing);
        Log.i(TAG,bf.getText());
        if (bf.getText().equals("STOP")) {
            scanLeDevice(false);
            bf.setText("SCAN");
            tv.setText("Done scanning");
            pb.setVisibility(View.GONE);
        } else {
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();
            scanLeDevice(true);
            bf.setText("STOP");
            tv.setText("Scanning...");
            pb.setVisibility(View.VISIBLE);
        }
    }

    protected void handleDeviceChoice(BluetoothDevice device){
        Log.i(TAG, "handleDeviceChoice");
        // Save device choice to preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SharedPrefs.BLUETOOTH_DEVICE_NAME,device.getName());
        editor.putString(SharedPrefs.BLUETOOTH_DEVICE_ADDRESS,device.getAddress());
        editor.apply();


        // Move on
        final Intent intent = new Intent(this, BluetoothAfterScan.class);

        if (mScanning) {
            stopScan();
            mScanning = false;
        }
        startActivity(intent);
        overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT
                && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        scanLeDevice(false);
        if (mLeDeviceListAdapter!=null) mLeDeviceListAdapter.clear();
    }


    private void scanLeDevice(final boolean enable) {
        Log.i(TAG,"scanLeDevice");
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    startScan();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            startScan();
        } else {
            mScanning = false;
            stopScan();
        }
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflator = BluetoothScan.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                if (!(device.getName()==null)) {  // only add named devices
                    mLeDevices.add(device);
                }
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.layout_bluetooth_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view
                        .findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view
                        .findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("Unknown Device");
            viewHolder.deviceAddress.setText(device.getAddress());
            return view;
        }
    }


    // Device scan callback API lvl 20-
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };


    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    public void stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG,"LeScanCallback");
                            mLeDeviceListAdapter.addDevice(result.getDevice());
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    public void startScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mBluetoothAdapter!=null) {
            mBluetoothAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(result.getDevice());
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        } else {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }






}