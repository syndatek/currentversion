package com.carditek.kesar.util

import android.Manifest.permission.*
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.carditek.kesar.Device
import com.carditek.kesar.R
import com.carditek.kesar.ui.device_list.PatchesDialog

class BluetoothUtils(
    private val activity: AppCompatActivity,
    private val device: Device
) {
    fun selectPatch() {
        startScan()
        val progress = ProgressDialog(activity)
        progress.setCancelable(false)
        progress.setMessage("Scanning ...")
        progress.show()
        Handler().postDelayed({
            stopScan()
            progress.dismiss()
            val dialog = PatchesDialog(devices.keys.toTypedArray()) {
                Log.i(TAG, "Bluetooth LE device $it selected.")
                device.setAddress(it)
            }
            dialog.isCancelable = false
            dialog.show(activity.supportFragmentManager, "Device List")
        }, 8000)
    }

    private lateinit var adapter: BluetoothAdapter

    init {
        if (activity.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            adapter = (activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
                .adapter
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var perms = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    perms = perms.plus(arrayOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT))
                activity.requestPermissions(perms, 101)
            }
            if (!adapter.isEnabled) {
                Log.i(TAG, "Enabling Bluetooth Adapter.")
                activity.startActivityForResult(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT
                )
            }
        } else {
            Log.w(TAG, activity.getString(R.string.ble_not_supported))
        }
    }

    private fun startScan() {
        device.setAddress("")

        // Make the service disconnect from current device, if any.
        if (!adapter.isEnabled) {
            Log.w(TAG, "Bluetooth LE adapter not enabled, aborting scan.")
            return
        }

        if (adapter.bluetoothLeScanner == null) {
            Log.w(TAG, "Bluetooth LE Scanner is null, aborting scan.")
            return
        }

        val settings = ScanSettings.Builder().build()
        val filter = ScanFilter.Builder().setDeviceName(SYDANTEK_DEVICE_NAME).build()
        adapter.startDiscovery()
        adapter.bluetoothLeScanner?.startScan(mutableListOf(filter), settings, scanCallback)
    }

    private fun stopScan() {
        adapter.cancelDiscovery()
        adapter.bluetoothLeScanner?.stopScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(type: Int, result: ScanResult?) {
            super.onScanResult(type, result)
            result?.let {
                devices[result.device.address] = result.device
                Log.i(TAG, "Device name: ${result.device.name}, address: ${result.device.address}")
            }
        }

        override fun onScanFailed(code: Int) {
            super.onScanFailed(code)
            Log.w(TAG, "Scan failed: code=${code}")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach {
                Log.i(TAG, "Batch scan result: $it")
            }
        }
    }

    private val devices = mutableMapOf<String, BluetoothDevice>()

    companion object {
        private const val TAG = "BLE"
        private const val REQUEST_ENABLE_BT = 101
        private const val SYDANTEK_DEVICE_NAME = "Sydäntek"
    }
}
