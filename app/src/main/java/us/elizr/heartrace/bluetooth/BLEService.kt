package us.elizr.heartrace.bluetooth

import android.annotation.TargetApi
import android.app.*
import android.bluetooth.*
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import us.elizr.heartrace.core.MyApp
import java.util.*
import javax.inject.Inject
import android.bluetooth.BluetoothGattCharacteristic



/**
 * Created by elizabethrussell on 3/16/18.
 */
class BLEService : Service() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var device: BluetoothDevice? = null

    companion object {
        val EXTRA_DEVICE = "extradevice"

        // UUIDS
        val UUID_HR_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val UUID_HR_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val UUID_CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        // Service deletion workflow
        val DELETE_KEY = "delkey4hrace"

        private val NOTIFICATION_ID = 12
    }

    val notificationChannelId: String
        @TargetApi(Build.VERSION_CODES.O)
        get() {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = "chrona_foreground_notif"
            val channelName = "Some Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            return channelId
        }

    override fun onCreate() {

    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int,
                                             newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //TODO:  broadcast
                bluetoothGatt!!.discoverServices()

            } else if (newState == STATE_DISCONNECTED) {
                //TODO: broadcast
                stopSelf()

            }
        }


        // New services discovered -> subscribe to hr characteristic
        override
        fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val services = gatt.services
                if (services != null) {
                    for (service in services) {
                        if (service.uuid == UUID_HR_SERVICE) {
                            for (gattCharacteristic in service.characteristics) {
                                val charUuid = gattCharacteristic.uuid
                                if (charUuid == UUID_HR_MEASUREMENT) {
                                    val charaProp = gattCharacteristic.properties
                                    if ((charaProp or BluetoothGattCharacteristic.PROPERTY_READ)>0) {
                                        bluetoothGatt?.setCharacteristicNotification(gattCharacteristic, true)
                                        val descriptor = gattCharacteristic.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIG)
                                        descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                                        bluetoothGatt?.writeDescriptor(descriptor)
                                        bluetoothGatt?.readCharacteristic(gattCharacteristic)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                 characteristic: BluetoothGattCharacteristic,
                                 status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.uuid == UUID_HR_MEASUREMENT) {
                    handleHr(characteristic)
                }
            }
        }


        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == UUID_HR_MEASUREMENT) {
                handleHr(characteristic)
            }

        }
    }

    private fun handleHr(characteristic: BluetoothGattCharacteristic) {
        // parse
        val data = characteristic.value
        val flag = characteristic.properties
        val format: Int
        if (flag and 0x01 !== 0) {
            format = BluetoothGattCharacteristic.FORMAT_UINT16
        } else {
            format = BluetoothGattCharacteristic.FORMAT_UINT8
        }
        val hr = characteristic.getIntValue(format, 1)

        // TODO: inform
        Log.i("BLEService","Got heart rate "+hr)
    }

    private val binder = BLEServiceBinder()



    @TargetApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.extras != null && intent.getBooleanExtra(DELETE_KEY, false)) {
            return Service.START_STICKY
        } else {
            // start notification

            // delete notification and stop service intent
            val deleteIntent = Intent(this, BLEService::class.java)
            deleteIntent.putExtra(DELETE_KEY, true)
            val deletePendingIntent = PendingIntent.getService(this,
                    0,
                    deleteIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT)

            // Android 8+ start foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val builder = Notification.Builder(this, notificationChannelId)
                        .setSmallIcon(us.elizr.heartrace.R.drawable.ic_favorite_red_48px)
                        .setContentTitle("HeartRace")
                        .setContentText("You are connected to a Bluetooth heart rate monitor!")
                        .setDeleteIntent(deletePendingIntent)
                startForeground(NOTIFICATION_ID, builder.build())

            } else {
                val notificationBuilder = NotificationCompat.Builder(this)
                        .setSmallIcon(us.elizr.heartrace.R.drawable.ic_favorite_red_48px)
                        .setContentTitle("HeartRace")
                        .setContentText("You are connected to a Bluetooth heart rate monitor!")
                        .setDeleteIntent(deletePendingIntent)
                startForeground(NOTIFICATION_ID, notificationBuilder.build())
            }// Android < 8 start foreground service

            // connect to bluetooth device
            if (intent != null && intent.extras != null) {
                device = intent.extras?.getParcelable(EXTRA_DEVICE)
                if (device != null) {
                    // was getting more problem reports with autoconnect set to true :(
                    val modelString = Build.MODEL.toLowerCase()
                    var autoConnect: Boolean? = false
                    if (Arrays.asList("pixel", "x").contains(modelString)) {
                        autoConnect = true
                    }
                    bluetoothGatt = device!!.connectGatt(this, autoConnect!!, gattCallback)
                }
            }
            return Service.START_REDELIVER_INTENT
        }
    }



    override fun onDestroy() {
        if (bluetoothGatt == null) {
            return
        } else {
            bluetoothGatt!!.close()
            bluetoothGatt = null
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class BLEServiceBinder : Binder() {
        val service: BLEService
            get() = this@BLEService
    }



}