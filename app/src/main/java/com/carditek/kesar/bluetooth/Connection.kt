
package com.carditek.kesar.bluetooth

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.os.Process
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.carditek.kesar.Cache
import com.carditek.kesar.util.filters.edgecomputing.HisBundleData

class Connection(
    context: Context,
    val address: String,
    val state: State,
    val handler: DataHandler,
    val cache:Cache
) {
    private val appContext = context.applicationContext

    private fun hasBluetoothGattPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private var parameters: Parameters? = null

    fun close() {
        handler.close()
        callback.close()
        handler.cleanup()
    }

    fun writeCharacteristic(data: ByteArray): Boolean {
        return callback.writeCharacteristic(data)
    }//till added

    private val callback = object : BluetoothGattCallback() {
        private val adapter: BluetoothAdapter? = run {
            if (!hasBluetoothGattPermission()) {
                Log.e(TAG, "Bluetooth permission not granted for BluetoothManager")
                return@run null
            }
            try {
                (appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
            } catch (e: SecurityException) {
                Log.e(TAG, "BluetoothManager.adapter", e)
                null
            }
        }
        private val remoteDevice: BluetoothDevice? = run {
            val ad = adapter ?: return@run null
            if (!hasBluetoothGattPermission()) return@run null
            try {
                ad.getRemoteDevice(address)
            } catch (e: SecurityException) {
                Log.e(TAG, "Bluetooth permission denied for getRemoteDevice", e)
                null
            }
        }
        private var gatt: BluetoothGatt? = null
        private lateinit var service: BluetoothGattService

        init {
            val d = remoteDevice
            if (d != null && hasBluetoothGattPermission()) {
                try {
                    gatt = d.connectGatt(appContext, true, this)
                } catch (e: SecurityException) {
                    Log.e(TAG, "Bluetooth permission denied for connectGatt", e)
                }
            }
        }

        fun close() {
            ++state.stats.connections.disconnects
            Log.i(TAG, "Disconnected from $address")
            state.state.postValue(State.DISCONNECTED)
            if (hasBluetoothGattPermission()) {
                try {
                    gatt?.run {
                        disconnect()
                        close()
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "disconnect/close not permitted", e)
                }
            }
            parameters = null
        }
        // september 15 i added gatt write
        fun writeCharacteristic(data: ByteArray): Boolean {
            if (!hasBluetoothGattPermission()) return false
            return try {
                service.getCharacteristic(SYDANTEK_WRITE_UUID.uuid)?.let { characteristic ->
                    characteristic.value = data
                    gatt?.writeCharacteristic(characteristic) == true
                } ?: false
            } catch (e: SecurityException) {
                Log.e(TAG, "writeCharacteristic not permitted", e)
                false
            } catch (e: Exception) {
                Log.e(TAG, "Error writing characteristic", e)
                false
            }
        }//till added

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (!hasBluetoothGattPermission()) return
            if (gatt != null && status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "${gatt.services?.size} services discovered")
                try {
                    gatt.getService(SYDANTEK_SERVICE_UUID.uuid)?.let { service ->
                        this.service = service
                        service.getCharacteristic(SYDANTEK_PARAMS_UUID.uuid)?.let {
                            if (this.gatt?.readCharacteristic(it) == true)
                                Log.i(TAG, "Initiated read of parameters")
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "onServicesDiscovered GATT ops not permitted", e)
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (!hasBluetoothGattPermission()) return
            if (characteristic != null && status == BluetoothGatt.GATT_SUCCESS) {
                parameters = Parameters.from(characteristic.value)
                Log.i(TAG, "$parameters")
            }

            try {
                service.let { service ->
                    service.getCharacteristic(SYDANTEK_NOTIFY_UUID.uuid)?.let {
                        val descriptor = it.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG.uuid)
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt?.run {
                            setCharacteristicNotification(it, true)
                            writeDescriptor(descriptor)
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "onCharacteristicRead notification setup not permitted", e)
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Descriptor write successful, notifications enabled.")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (!hasBluetoothGattPermission()) return
            val value = characteristic?.value ?: return
            handler.handle(value)
        }
        //gatt write function added 15 the september
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Characteristic write successful: ${characteristic?.uuid}")
            } else {
                Log.e(TAG, "Characteristic write failed with status: $status")
            }
        }//till now

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (!hasBluetoothGattPermission()) return
            Log.i(TAG, "MTU set to $mtu.")
            firebase.setCustomKey("MTU", mtu)
            try {
                gatt?.discoverServices()
            } catch (e: SecurityException) {
                Log.e(TAG, "discoverServices not permitted", e)
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTING ->
                    state.state.postValue(State.CONNECTING)
                BluetoothProfile.STATE_CONNECTED -> if (gatt != null && hasBluetoothGattPermission()) {
                    state.state.postValue(State.CONNECTED)
                    cache.bleConnectedLive.postValue(true)
                    try {
                        Log.i(TAG, "Connected to ${gatt.device.address}")
                        Process.setThreadPriority(-20)
                        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                        gatt.requestMtu(512)
                    } catch (e: SecurityException) {
                        Log.e(TAG, "connection setup GATT not permitted", e)
                    }
                }
                BluetoothProfile.STATE_DISCONNECTING ->
                    state.state.postValue(State.DISCONNECTING)

                BluetoothProfile.STATE_DISCONNECTED -> {
                    handler.resetProcessing()     // reset  for hisbundle
                    Log.e("RESET_TEST", "Posting BLE false")
                    cache.bleConnectedLive.postValue(false)

                    HisBundleData.graphPath = ""
                    HisBundleData.graphPathLive.postValue("")
                    close()
                    if (!hasBluetoothGattPermission()) return
                    try {
                        this.gatt = remoteDevice?.connectGatt(appContext, true, this)
                    } catch (e: SecurityException) {
                        Log.e(TAG, "reconnect connectGatt denied", e)
                    }
                }
            }
        }
    }

    companion object {
        private val firebase = FirebaseCrashlytics.getInstance()
        private const val TAG = "Callback"
        private val SYDANTEK_SERVICE_UUID = ParcelUuid.fromString(
            "a965db41-5e30-ad9e-fe47-02a582287800"
        )
        private val SYDANTEK_PARAMS_UUID = ParcelUuid.fromString(
            "a965db41-5e30-ad9e-fe47-02a582287801"
        )
        private val SYDANTEK_NOTIFY_UUID = ParcelUuid.fromString(
            "a965db41-5e30-ad9e-fe47-02a582287802"
        )
//        private val SYDANTEK_WRITE_UUID = ParcelUuid.fromString(//add for gatt write
//            "a965db41-5e30-ad9e-fe47-02a582287803"
//        )
private val SYDANTEK_WRITE_UUID = ParcelUuid.fromString(//add for gatt write
    "abcdef01-1234-5678-1234-56789abcdef0"
)
        private val CLIENT_CHARACTERISTIC_CONFIG = ParcelUuid.fromString(
            "00002902-0000-1000-8000-00805f9b34fb"
        )
    }
}
