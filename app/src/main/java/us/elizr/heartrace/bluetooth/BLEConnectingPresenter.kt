package us.elizr.heartrace.bluetooth

import android.Manifest
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.ParcelUuid
import android.support.v4.content.ContextCompat
import android.util.Log
import us.elizr.heartrace.core.MyApp
import java.util.*
import javax.inject.Inject

/**
 * Bluetooth cascade- from presenter side
 *
 * The bluetooth cascade is:
 * 1.  Check for permission (startBluetoothCascade) - async if user inut needed
 * 2.  Check for enabled (ensureEnabledBluetooth) - async if user input needed
 * 3.  Initialize bluetooth adapter
 * 4.  Scan for devices
 *
 * This is a heavyweight presenter with a bunch of Android dependency :/
 */
class BLEConnectingPresenter {


    @Inject
    lateinit var app: Application

    private var view: BLEConnectingViewInterface? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSearchUnderway = false
    private lateinit var scanCallback: ScanCallback

    val PERMISSION_REQUEST_COURSE_LOCATION = 5 // arbitrary
    val ACTIVITY_RESULT_ENABLE_BLUETOOTH = 1446
    val BLUETOOTH_SCAN_PERIOD: Long = 60000 // 1 min

    init {
        MyApp.appComponent.inject(this)
    }

    fun assignView(view: BLEConnectingViewInterface) {
        this.view = view
    }


    fun startBluetoothCascade() {
        if (!bluetoothSearchUnderway) {
            checkBluetoothPermission()
            bluetoothSearchUnderway = true
        }
    }

    fun stopBluetoothCascade() {
        if (bluetoothSearchUnderway) {
            stopScanningForDevices()
        }
    }

    private fun checkBluetoothPermission() {
        // Android M or higher must gather permissions
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val permissionCheck = ContextCompat.checkSelfPermission(app.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                view?.requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PERMISSION_REQUEST_COURSE_LOCATION)
            } else {
                ensureEnabledBluetooth()
            }
        } else {
            ensureEnabledBluetooth()
        }
    }

    fun gotPermission(requestCode: Int, granted: Boolean) {
        if (requestCode == PERMISSION_REQUEST_COURSE_LOCATION) {
            if (granted) {
                ensureEnabledBluetooth()
            } else {
                TODO("handle user not giving location permission")
            }
        }
    }

    private fun ensureEnabledBluetooth() {
        val bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter!!.isEnabled) {
                view?.requestEnableBluetooth(ACTIVITY_RESULT_ENABLE_BLUETOOTH)
            } else {
                scanForDevices()
            }
        }

    }

    fun gotActivityResult(requestCode: Int, resultCode: Int) {
        if (requestCode == ACTIVITY_RESULT_ENABLE_BLUETOOTH) {
            if (resultCode != Activity.RESULT_CANCELED) {
                scanForDevices()
            } else {
                TODO("handle user who does not enable bluetooth")
            }
        }
    }

    private fun scanForDevices() {
        Log.i("BLEPresenter","scanning for devices")
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                view?.showNewDevice(device)
            }
        }

        Handler().postDelayed({
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            bluetoothSearchUnderway = false
        }, BLUETOOTH_SCAN_PERIOD)
        val filters = ArrayList<ScanFilter>()
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(BLEService.UUID_HR_SERVICE)).build()
        filters.add(filter)
        val settings = ScanSettings.Builder().setScanMode(SCAN_MODE_LOW_LATENCY).build()
        bluetoothAdapter?.bluetoothLeScanner?.startScan(filters, settings, scanCallback)
        view?.showScanning()
    }

    private fun stopScanningForDevices() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        bluetoothSearchUnderway = false
    }

    fun selectedDevice(device: BluetoothDevice) {
        Log.i("BLEPresenter","selected device in presenter")
        view?.showScanningStopped()
        stopScanningForDevices()

        // start bluetooth service
        val intent = Intent(app, BLEService::class.java)
        intent.putExtra(BLEService.EXTRA_DEVICE, device)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            app.startForegroundService(intent)
        } else {
            app.startService(intent)
        }
        bluetoothSearchUnderway = false

        // begin gameplay
        view?.transitionToGameplay()

    }


}
