package us.elizr.heartrace.bluetooth

import android.bluetooth.BluetoothDevice

/**
 * Created by elizabethrussell on 3/16/18.
 */
interface BLEConnectingViewInterface {

        fun requestPermission(permissionString: String, permissionInt: Int)
        fun requestEnableBluetooth(requestResult: Int)
        fun showScanning()
        fun showScanningStopped()
        fun showNewDevice(device: BluetoothDevice)
        fun transitionToGameplay()

}