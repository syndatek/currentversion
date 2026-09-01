//package com.carditek.kesar.util.filters.edgecomputing
//
//import android.content.Context
//import java.io.File
//import java.io.FileWriter
//import java.text.SimpleDateFormat
//import java.util.*
//import android.util.Log
//import android.os.Environment
//
//class CsvManager(private val context: Context) {
//
//    private val fileName = "ECG_Analysis.csv"
//
//    fun saveData(
//        patient: String,
//        hr: String,
//        pa: String,
//        ah: String,
//        hv: String,
//        pr: String,
//        qrs: String,
//        snr: String
//    ) {
//
//
//        val file = File(context.getExternalFilesDir(null), fileName)
////        val downloadsDir =
////            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
////        val file = File(downloadsDir, fileName)
//        Log.d("CSV_PATH", "CSV File = ${file.absolutePath}")
//        val isNewFile = !file.exists()
//
//        FileWriter(file, true).use { writer ->
//
//            if (isNewFile) {
//                writer.append(
//                    "Date,Time,Patient,Heart Rate,PA,AH,HV,PR,QRS,SNR\n"
//                )
//            }
//
//            val now = Date()
//
//            val date = SimpleDateFormat(
//                "dd-MM-yyyy",
//                Locale.getDefault()
//            ).format(now)
//
//            val time = SimpleDateFormat(
//                "HH:mm:ss",
//                Locale.getDefault()
//            ).format(now)
//
//            writer.append(date).append(",")
//            writer.append(time).append(",")
//            writer.append(patient).append(",")
//            writer.append(hr).append(",")
//            writer.append(pa).append(",")
//            writer.append(ah).append(",")
//            writer.append(hv).append(",")
//            writer.append(pr).append(",")
//            writer.append(qrs).append(",")
//            writer.append(snr).append("\n")
//
//            writer.flush()
//        }
//    }
//}
//
package com.carditek.kesar.util.filters.edgecomputing

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class CsvManager(private val context: Context) {

    private val fileName = "His_Bundlecmc.csv"

    fun saveData(
        patient: String,
        macAddress: String,
        hr: String,
        beatNumber: String,
        pOnsetLocation: String,
        pPeakLocation: String,
        qrsOnLocation: String,
        qrsOffLocation: String,
        rPeakLocation: String,
        prDuration: String,
        qrsDuration: String,
        paDuration: String,
        ahDuration: String,
        hvDuration: String,
        hAmplitude: Double
    ) {
        val csvData = buildCsvString(
            patient, macAddress, hr, beatNumber, pOnsetLocation, pPeakLocation,
            qrsOnLocation, qrsOffLocation, rPeakLocation, prDuration,
            qrsDuration, paDuration, ahDuration, hvDuration, hAmplitude
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDownloadsMediaStore(csvData)
        } else {
            saveToDownloadsLegacy(csvData)
        }
    }

    private fun buildCsvString(
        patient: String,
        macAddress: String,
        hr: String,
        beatNumber: String,
        pOnsetLocation: String,
        pPeakLocation: String,
        qrsOnLocation: String,
        qrsOffLocation: String,
        rPeakLocation: String,
        prDuration: String,
        qrsDuration: String,
        paDuration: String,
        ahDuration: String,
        hvDuration: String,
        hAmplitude: Double
    ): String {
        val now = Date()
        val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(now)
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)

        return "$date,$time,$patient,$macAddress,$hr,$beatNumber,$pOnsetLocation,$pPeakLocation," +
                "$qrsOnLocation,$qrsOffLocation,$rPeakLocation,$prDuration,$qrsDuration," +
                "$paDuration,$ahDuration,$hvDuration,${String.format("%.6f", hAmplitude)}\n"
    }

    private fun getHeader(): String {
        return "Date,Time,Patient,MAC Address,Heart Rate,Beat Number,P Onset Location,P Peak Location," +
                "QRS On Location,QRS Off Location,R Peak Location,PR Duration,QRS Duration," +
                "PA Duration,AH Duration,HV Duration,H Amplitude\n"
    }

    private fun saveToDownloadsMediaStore(csvData: String) {
        try {
            // For Android 10+, use app-specific storage which is more reliable
            // MediaStore append mode has issues on some devices
            saveToAppSpecific(csvData)
            Log.d("CSV_MANAGER", "CSV saved to app-specific storage (Android 10+)")
        } catch (e: Exception) {
            Log.e("CSV_MANAGER", "Error saving CSV", e)
        }
    }

    private fun saveToAppSpecific(csvData: String) {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        try {
            val isNewFile = !file.exists()
            FileWriter(file, true).use { writer ->
                if (isNewFile) {
                    writer.append(getHeader())
                }
                writer.append(csvData)
                writer.flush()
            }
            Log.d("CSV_MANAGER", "CSV saved to app-specific Downloads: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("CSV_MANAGER", "Error saving CSV to app-specific storage", e)
        }
    }

    private fun saveToDownloadsLegacy(csvData: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            val isNewFile = !file.exists()
            FileWriter(file, true).use { writer ->
                if (isNewFile) {
                    writer.append(getHeader())
                }
                writer.append(csvData)
                writer.flush()
            }
            Log.d("CSV_MANAGER", "CSV saved to Downloads (legacy): ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("CSV_MANAGER", "Error saving CSV (legacy)", e)
        }
    }
}
