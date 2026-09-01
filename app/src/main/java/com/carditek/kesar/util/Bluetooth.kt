package com.carditek.kesar.util

import android.Manifest.permission.*
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
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.carditek.kesar.Device
import com.carditek.kesar.R
import com.carditek.kesar.ui.device_list.PatchesDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BluetoothUtils(
    private val activity: AppCompatActivity,
    private val device: Device,
    private val enableBluetoothLauncher: ActivityResultLauncher<Intent>
) {
    fun selectPatch() {
        val ad = adapter
        if (ad == null) {
            Log.w(TAG, activity.getString(R.string.ble_not_supported))
            return
        }
        startScan(ad)
        val progressBar = ProgressBar(activity).apply { isIndeterminate = true }
        val progress = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.app_name)
            .setMessage("Scanning ...")
            .setView(progressBar)
            .setCancelable(false)
            .create()
        progress.show()
        Handler(activity.mainLooper).postDelayed({
            stopScan(ad)
            progress.dismiss()
            val dialog = PatchesDialog(devices.keys.toTypedArray()) {
                Log.i(TAG, "Bluetooth LE device $it selected.")
                device.setAddress(it)
            }
            dialog.isCancelable = false
            dialog.show(activity.supportFragmentManager, "Device List")
        }, 8000)
    }

    private var adapter: BluetoothAdapter? = null

    init {
        if (activity.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var perms = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    perms = perms.plus(arrayOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT))
                }
                activity.requestPermissions(perms, 101)
            }
            adapter = obtainBluetoothAdapterOrNull()
            adapter?.let { ad ->
                if (hasBluetoothConnectForAdapterState() && !ad.isEnabled) {
                    Log.i(TAG, "Enabling Bluetooth Adapter.")
                    try {
                        enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Cannot request Bluetooth enable", e)
                    }
                }
            }
        } else {
            Log.w(TAG, activity.getString(R.string.ble_not_supported))
        }
    }

    /** API 31+: [BluetoothAdapter] state requires [BLUETOOTH_CONNECT]. */
    private fun hasBluetoothConnectForAdapterState(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(activity, BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED
    }

    /** Safe to call [BluetoothManager.getAdapter] only when connect permission is granted on API 31+. */
    private fun obtainBluetoothAdapterOrNull(): BluetoothAdapter? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(activity, BLUETOOTH_CONNECT) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "BLUETOOTH_CONNECT not granted; cannot access BluetoothAdapter")
            return null
        }
        return try {
            (activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        } catch (e: SecurityException) {
            Log.e(TAG, "BluetoothAdapter not accessible", e)
            null
        }
    }

    private fun hasBleScanPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(activity, BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return ContextCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun startScan(adapter: BluetoothAdapter) {
        device.setAddress("")

        if (!hasBleScanPermissions()) {
            Log.w(TAG, "BLE scan skipped: required permissions not granted")
            return
        }

        if (!hasBluetoothConnectForAdapterState()) {
            Log.w(TAG, "BLE scan skipped: BLUETOOTH_CONNECT not granted")
            return
        }

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
        try {
            adapter.startDiscovery()
            adapter.bluetoothLeScanner?.startScan(mutableListOf(filter), settings, scanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "BLE scan not permitted", e)
        }
    }

    private fun stopScan(adapter: BluetoothAdapter) {
        if (!hasBleScanPermissions() || !hasBluetoothConnectForAdapterState()) return
        try {
            adapter.cancelDiscovery()
            adapter.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "stopScan not permitted", e)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(type: Int, result: ScanResult?) {
            super.onScanResult(type, result)
            val r = result ?: return
            if (!hasBleScanPermissions() || !hasBluetoothConnectForAdapterState()) return
            try {
                val dev = r.device
                val addr = dev.address
                devices[addr] = dev
                val name = dev.name ?: "(unknown)"
                Log.i(TAG, "Device name: $name, address: $addr")
            } catch (e: SecurityException) {
                Log.w(TAG, "Cannot read device from scan result (permission)", e)
            }
        }

        override fun onScanFailed(code: Int) {
            super.onScanFailed(code)
            Log.w(TAG, "Scan failed: code=${code}")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            if (!hasBleScanPermissions() || !hasBluetoothConnectForAdapterState()) return
            results?.forEach { r ->
                try {
                    Log.i(TAG, "Batch scan result: ${r.device.address}")
                } catch (e: SecurityException) {
                    Log.w(TAG, "Batch scan result (address redacted)", e)
                }
            }
        }
    }

    private val devices = mutableMapOf<String, BluetoothDevice>()

    companion object {
        private const val TAG = "BLE"
        private const val SYDANTEK_DEVICE_NAME = "Sydäntek"
    }
}
