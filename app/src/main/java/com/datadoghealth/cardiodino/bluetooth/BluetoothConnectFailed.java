package com.datadoghealth.cardiodino.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.datadoghealth.cardiodino.R;


/**
 * When connection fails, give options to pair different device or continue
 * trying to connect
 *
 * ECR
 */
public class BluetoothConnectFailed extends Activity {
    private static final String TAG = BluetoothConnectFailed.class.getSimpleName();


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_connected);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, intentFilter);
    }



    // simply try to connect again
    public void retry(View v) {
        Intent i = new Intent(this, BluetoothAfterScan.class);
        startActivity(i);
        overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
    }

    public void rescan(View v) {
        Intent i = new Intent(this, BluetoothScan.class);
        startActivity(i);
        overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
    }

    public void reset(View v) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.isEnabled()) {
            adapter.disable();
        } else {
            Log.e("BluetoothConnectFailed", "Bluetooth isn't even enabled!");
        }
    }


    // Receive broadcasts about Bluetooth Adapter state, and goes to rescan when restarted
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent ) {
            final int state = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
            if (state == BluetoothAdapter.STATE_OFF) {
                BluetoothAdapter.getDefaultAdapter().enable();
            } else if (state == BluetoothAdapter.STATE_ON) {
                rescan(null);
            }
        }
    };

}
