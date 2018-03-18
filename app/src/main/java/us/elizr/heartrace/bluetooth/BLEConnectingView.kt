package us.elizr.heartrace.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_connecting.*
import us.elizr.heartrace.R
import us.elizr.heartrace.core.MyApp
import us.elizr.heartrace.gameplay.GameplayView
import javax.inject.Inject

/**
 * Created by elizabethrussell on 3/16/18.
 */
class BLEConnectingView: Activity(), BLEConnectingViewInterface {

    @Inject
    lateinit var presenter: BLEConnectingPresenter

    lateinit var bluetoothDeviceArrayAdapter: ArrayAdapter<BluetoothDevice>
    val discoveredDeviceIds: ArrayList<String> = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connecting)
        MyApp.appComponent.inject(this)

        progress_connecting.visibility = View.INVISIBLE
        progress_connecting.isIndeterminate = true

        // set up bluetooth device list
        bluetoothDeviceArrayAdapter = object : ArrayAdapter<BluetoothDevice>(this, android.R.layout.select_dialog_singlechoice) {
            override fun getView(position: Int, convertView: android.view.View?, parent: ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(getResources().getColor(R.color.primary_text_heartrace))
                val name = getItem(position)!!.name
                val address = getItem(position)!!.address
                view.text = getString(R.string.bluetooth_device_description, name, address)
                return view
            }
        }
        list_connecting_devices.adapter = bluetoothDeviceArrayAdapter
        list_connecting_devices.onItemClickListener = object: AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                text_connecting.text = getString(R.string.bluetooth_device_selected)
                presenter.selectedDevice(bluetoothDeviceArrayAdapter.getItem(p2))
            }

        }

        presenter.assignView(this)
        presenter.startBluetoothCascade()

    }

    override fun requestEnableBluetooth(requestResult: Int) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, requestResult)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.gotActivityResult(requestCode, resultCode)
    }

    override fun showScanning() {
        text_connecting.text=getString(R.string.scanning_text)
        progress_connecting.visibility = View.VISIBLE

    }

    override fun showScanningStopped() {
        progress_connecting.visibility = View.INVISIBLE
    }

    override fun showNewDevice(device: BluetoothDevice) {
        text_connecting.text = getString(R.string.select_device_text)
        if (!discoveredDeviceIds.contains(device.address)) {
            bluetoothDeviceArrayAdapter.add(device)
            discoveredDeviceIds.add(device.address)
        }
    }

    override fun requestPermission(permissionString: String, permissionInt: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permissionString), permissionInt)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        if (grantResults != null) {
            val granted = grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            presenter.gotPermission(requestCode, granted)
        }
    }

    override fun transitionToGameplay() {
        val intent = Intent(this, GameplayView::class.java)
        startActivity(intent)
    }

}