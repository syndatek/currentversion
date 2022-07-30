package com.carditek.kesar.bluetooth

import android.bluetooth.*
import android.content.Context
import android.os.ParcelUuid
import android.os.Process
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

class Connection(
    context: Context,
    val address: String,
    val state: State,
    val handler: DataHandler
) {
    private var parameters: Parameters? = null

    fun close() {
        handler.close()
        callback.close()
    }

    private val callback = object : BluetoothGattCallback() {
        private val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE)
                as BluetoothManager).adapter
        private val device = adapter.getRemoteDevice(address)
        private var gatt: BluetoothGatt? = device.connectGatt(context, true, this)
        private lateinit var service: BluetoothGattService

        fun close() {
            ++state.stats.connections.disconnects
            Log.i(TAG, "Disconnected from $address")
            state.state.postValue(State.DISCONNECTED)
            gatt?.run {
                disconnect()
                close()
            }
            parameters = null
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (gatt != null && status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "${gatt.services?.size} services discovered")
                gatt.getService(SYDANTEK_SERVICE_UUID.uuid)?.let { service ->
                    this.service = service
                    service.getCharacteristic(SYDANTEK_PARAMS_UUID.uuid)?.let {
                        if (this.gatt?.readCharacteristic(it) == true)
                            Log.i(TAG, "Initiated read of parameters")
                    }
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (characteristic != null && status == BluetoothGatt.GATT_SUCCESS) {
                parameters = Parameters.from(characteristic.value)
                Log.i(TAG, "$parameters")
            }

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
            handler.handle(characteristic?.value!!)
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.i(TAG, "MTU set to $mtu.")
            firebase.setCustomKey("MTU", mtu)
            gatt?.discoverServices()
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTING ->
                    state.state.postValue(State.CONNECTING)
                BluetoothProfile.STATE_CONNECTED -> if (gatt != null) {
                    state.state.postValue(State.CONNECTED)
                    Log.i(TAG, "Connected to ${gatt.device.address}")
                    Process.setThreadPriority(-20)
                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                    gatt.requestMtu(512)
                }
                BluetoothProfile.STATE_DISCONNECTING ->
                    state.state.postValue(State.DISCONNECTING)
                BluetoothProfile.STATE_DISCONNECTED -> {
                    close()
                    this.gatt = device.connectGatt(context, true, this)
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
        private val CLIENT_CHARACTERISTIC_CONFIG = ParcelUuid.fromString(
            "00002902-0000-1000-8000-00805f9b34fb"
        )
    }
}
