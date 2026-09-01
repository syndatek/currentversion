//package com.carditek.kesar.cloud
//
//import android.content.Context
//import android.util.Log
//import androidx.hilt.work.HiltWorker
//import androidx.work.*
//import com.carditek.kesar.*
//import com.carditek.kesar.bluetooth.State
//import com.carditek.kesar.module.Patient
//import com.carditek.kesar.util.CloudBackend
//import com.google.firebase.crashlytics.FirebaseCrashlytics
//import dagger.Module
//import dagger.Provides
//import dagger.assisted.Assisted
//import dagger.assisted.AssistedInject
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.util.*
//import java.util.concurrent.TimeUnit
//import javax.inject.Inject
//import javax.inject.Singleton
//
//class Uploader @Inject constructor(
//    private val context: Context,
//    private val account: Account,
//    private val device: Device,
//    private val patient: Patient,
//    private val chunks: ChunkDao,
//    private val noteDao: NoteDao,
//    private val state: State
//) {
//    @HiltWorker
//    class ChunkWorker @AssistedInject constructor(
//        @Assisted context: Context,
//        @Assisted params: WorkerParameters,
//        private val chunks: ChunkDao,
//        private val backend: Backend,
//        private val state: State,
//        private val account: Account
//    ) : CoroutineWorker(context, params) {
//        override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
//            val address = inputData.getString(KEY_ADDRESS)!!
//            val stamp = inputData.getInt(KEY_TIMESTAMP, -1)
//            val chunk = chunks.get(address, stamp)
//            if (chunk == null) {
//                Log.e(TAG, "Couldn't find chunk: $address/$stamp")
//                firebase.recordException(Exception("Couldn't find chunk: $address/$stamp"))
//                Result.failure()
//            } else {
//                try {
//                    backend.store(chunk)
//                    chunks.delete(address, stamp)
//                    ++state.stats.cloud.upload
//                    state.stats.cloud.pending = chunks.count()
//                    val result = "${chunk.address}/${chunk.stamp}/${chunk.leads}/${chunk.frequency}"
//                    Log.i(TAG, "Uploaded chunk: $address/$stamp")
//                    Log.d(TAG, "trying to upload-------------------------------")
//                    Log.d(TAG, "one-------------------------------")
//                    Result.success(workDataOf(KEY_RESULT to result))
//                } catch (e: CloudBackend.HttpException) {
//                    if (e.code == 401) {
//                        // Permissions failure.  Perhaps token expired?
//                        account.refresh()
//                        Log.d(TAG, "RE---401 error_1-------------------------------")
//                        Log.d(TAG, "Two-------------------------------")
//
//                    }
//                   // account.refresh()
//                    Log.e(TAG, "Got exception (code: ${e.code}", e)
//                    firebase.recordException(e)
//                    Log.d(TAG, "RE---trying to upload with account refrest-------------------------------")
//                    Log.d(TAG, "three-------------------------------")
//                    Result.retry()
//
//                } catch (e: Exception) {
//                    Log.e(TAG, "Got exception", e)
//                    Log.d(TAG, "RE---got eception without any exepectancy-------------------------------")
//                    Log.d(TAG, "four-------------------------------")
//                    firebase.recordException(e)
//                    Result.retry()
//                } finally {
//                    // This block is always executed, regardless of whether an exception occurred
//                    println("Finally block executed")
//                    Log.d(TAG, "five-------------------------------")
//                }
//            }
//        }
//    }
//
//    @HiltWorker
//    class NoteWorker @AssistedInject constructor(
//        @Assisted context: Context,
//        @Assisted params: WorkerParameters,
//        private val backend: Backend,
//        private val account: Account,
//        private val noteDao: NoteDao,
//        private val medicalHistoryUploadNotifier: MedicalHistoryUploadNotifier
//    ) : CoroutineWorker(context, params) {
//        override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
//            val address = inputData.getString(KEY_ADDRESS)!!
//            val stamp = inputData.getInt(KEY_TIMESTAMP, -1)
//            val note = inputData.getString(KEY_NOTE)!!
//            val noteId = inputData.getLong(KEY_NOTE_ID, -1L).takeIf { it > 0 }
//            try {
//                backend.note(address, stamp, note)
//                if (noteId != null) {
//                    noteDao.markAsUploaded(noteId, System.currentTimeMillis())
//                    medicalHistoryUploadNotifier.notifyMedicalHistorySaved()
//                }
//                Result.success(workDataOf(KEY_RESULT to "${address}/${stamp}"))
//            } catch (e: CloudBackend.HttpException) {
//                if (e.code == 401) {
//                    // Permissions failure.  Perhaps token expired?
//                    account.refresh()
//                    Log.d(TAG, "six-------------------------------")
//                } else if (e.code != 404) {
//                    // We get a 404 when the note was attempted to be saved before there was
//                    // an available chunk to store it against.  That's expected, don't log.
//                    Log.e(TAG, "Got exception (code: ${e.code}", e)
//                    firebase.recordException(e)
//                    Log.d(TAG, "seven-------------------------------")
//                }
//                Result.retry()
//            } catch (e: Exception) {
//                Log.e(TAG, "Got exception", e)
//                firebase.recordException(e)
//                Log.d(TAG, "eigth-------------------------------")
//                Result.retry()
//
//            }finally {
//                // This block is always executed, regardless of whether an exception occurred
//                Log.d(TAG, "nine-------------------------------")
//                println("Finally  noteblock executed")
//            }
//        }
//    }
//
//
//    // TODO(vjn): the device and/or patient could have changed, we're looking at live data.
//    suspend fun upload(stamp: Int, leads: Int, frequency: Int, data: ByteArray): UUID {
//        // Capture patient data at upload time to avoid null issues during async upload
//        val currentAddress = device.address.value
//        val currentEmail = account.email.value
//        val currentPatientName = patient.name.value
//        val currentPatientPhone = patient.phone.value
//
//        if (currentAddress == null) {
//            Log.e(TAG, "Cannot upload: device address is null")
//            throw IllegalStateException("Device address is null")
//        }
//        if (currentEmail == null) {
//            Log.e(TAG, "Cannot upload: account email is null")
//            throw IllegalStateException("Account email is null")
//        }
//
//        val chunk = Chunk(
//            address = currentAddress,
//            stamp = stamp,
//            email = currentEmail,
//            frequency = frequency,
//            leads = leads,
//            logged = 0,
//            patientName = currentPatientName ?: "Unknown",
//            patientPhone = currentPatientPhone ?: "Unknown",
//            data = data
//        )
//        Log.d(Uploader.TAG, "Uploading chunk - Address: $currentAddress, Patient: $currentPatientName, Phone: $currentPatientPhone")
//        chunks.insert(chunk)
//        state.stats.cloud.pending = chunks.count()
//        val request = OneTimeWorkRequestBuilder<ChunkWorker>()
//            .setInputData(workDataOf(KEY_ADDRESS to currentAddress, KEY_TIMESTAMP to stamp))
//            .setConstraints(constraints)
//            .build()
//
//        // Each request is independent of the others.  Normally, the work will be carried out in
//        // order.  Requests may be dispatched out of order, coming out of a network unavailability
//        // event, and that is okay.
//        WorkManager.getInstance(context).enqueue(request)
//        return request.id
//    }
//
//    fun note(stamp: Int, text: String, noteId: Long? = null): UUID {
//        val request = OneTimeWorkRequestBuilder<NoteWorker>()
//            .setInputData(
//                workDataOf(
//                    KEY_ADDRESS to device.address.value,
//                    KEY_TIMESTAMP to stamp,
//                    KEY_NOTE to text,
//                    KEY_NOTE_ID to (noteId ?: -1L)
//                )
//            )
//            .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.SECONDS)
//            .setConstraints(constraints).build()
//        WorkManager.getInstance(context).enqueue(request)
//        return request.id
//    }
//
//    companion object {
//        private const val KEY_ADDRESS = "address"
//        private const val KEY_RESULT = "result"
//        private const val KEY_TIMESTAMP = "stamp"
//        private const val KEY_NOTE = "note"
//        private const val KEY_NOTE_ID = "note_id"
//        private val constraints =
//            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
//        private val firebase = FirebaseCrashlytics.getInstance()
//        private const val TAG = "upload"
//    }
//}
//
//@Module
//@InstallIn(SingletonComponent::class)
//object UploaderModule {
//    @Provides
//    @Singleton
//    fun provideUploader(
//        @ApplicationContext context: Context,
//        account: Account,
//        device: Device,
//        patient: Patient,
//        chunks: ChunkDao,
//        noteDao: NoteDao,
//        state: State
//    ): Uploader {
//
//
//        return Uploader(context, account, device, patient, chunks, noteDao, state)
//    }
//}
//
//
//
//




package com.carditek.kesar.cloud

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.carditek.kesar.*
import com.carditek.kesar.bluetooth.State
import com.carditek.kesar.module.Patient
import com.carditek.kesar.util.CloudBackend
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

class Uploader @Inject constructor(
    private val context: Context,
    private val account: Account,
    private val device: Device,
    private val patient: Patient,
    private val chunks: ChunkDao,
    private val noteDao: NoteDao,
    private val state: State
) {
    @HiltWorker
    class ChunkWorker @AssistedInject constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val chunks: ChunkDao,
        private val backend: Backend,
        private val state: State,
        private val account: Account
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
            val address = inputData.getString(KEY_ADDRESS)!!
            val stamp = inputData.getInt(KEY_TIMESTAMP, -1)
            val chunk = chunks.get(address, stamp)
            if (chunk == null) {
                Log.e(TAG, "Couldn't find chunk: $address/$stamp")
                firebase.recordException(Exception("Couldn't find chunk: $address/$stamp"))
                Result.failure()
            } else {
                try {
                    backend.store(chunk)
                    chunks.delete(address, stamp)
                    ++state.stats.cloud.upload
                    state.stats.cloud.pending = chunks.count()
                    val result = "${chunk.address}/${chunk.stamp}/${chunk.leads}/${chunk.frequency}"
                    Log.i(TAG, "Uploaded chunk: $address/$stamp")
                    Log.d(TAG, "trying to upload-------------------------------")
                    Log.d(TAG, "one-------------------------------")
                    Result.success(workDataOf(KEY_RESULT to result))
                } catch (e: CloudBackend.HttpException) {
                    if (e.code == 401) {
                        // Permissions failure.  Perhaps token expired?
                        account.refresh()
                        Log.d(TAG, "RE---401 error_1-------------------------------")
                        Log.d(TAG, "Two-------------------------------")

                    }
                    // account.refresh()
                    Log.e(TAG, "Got exception (code: ${e.code}", e)
                    firebase.recordException(e)
                    Log.d(TAG, "RE---trying to upload with account refrest-------------------------------")
                    Log.d(TAG, "three-------------------------------")
                    Result.retry()

                } catch (e: Exception) {
                    Log.e(TAG, "Got exception", e)
                    Log.d(TAG, "RE---got eception without any exepectancy-------------------------------")
                    Log.d(TAG, "four-------------------------------")
                    firebase.recordException(e)
                    Result.retry()
                } finally {
                    // This block is always executed, regardless of whether an exception occurred
                    println("Finally block executed")
                    Log.d(TAG, "five-------------------------------")
                }
            }
        }
    }

    @HiltWorker
    class NoteWorker @AssistedInject constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val backend: Backend,
        private val account: Account,
        private val noteDao: NoteDao,
        private val medicalHistoryUploadNotifier: MedicalHistoryUploadNotifier
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
            val address = inputData.getString(KEY_ADDRESS)!!
            val stamp = inputData.getInt(KEY_TIMESTAMP, -1)
            val note = inputData.getString(KEY_NOTE)!!
            val noteId = inputData.getLong(KEY_NOTE_ID, -1L).takeIf { it > 0 }
            try {
                backend.note(address, stamp, note)
                if (noteId != null) {
                    noteDao.markAsUploaded(noteId, System.currentTimeMillis())
                    medicalHistoryUploadNotifier.notifyMedicalHistorySaved()
                }
                Result.success(workDataOf(KEY_RESULT to "${address}/${stamp}"))
            } catch (e: CloudBackend.HttpException) {
                if (e.code == 401) {
                    // Permissions failure.  Perhaps token expired?
                    account.refresh()
                    Log.d(TAG, "six-------------------------------")
                } else if (e.code != 404) {
                    // We get a 404 when the note was attempted to be saved before there was
                    // an available chunk to store it against.  That's expected, don't log.
                    Log.e(TAG, "Got exception (code: ${e.code}", e)
                    firebase.recordException(e)
                    Log.d(TAG, "seven-------------------------------")
                }
                Result.retry()
            } catch (e: Exception) {
                Log.e(TAG, "Got exception", e)
                firebase.recordException(e)
                Log.d(TAG, "eigth-------------------------------")
                Result.retry()

            }finally {
                // This block is always executed, regardless of whether an exception occurred
                Log.d(TAG, "nine-------------------------------")
                println("Finally  noteblock executed")
            }
        }
    }


    // TODO(vjn): the device and/or patient could have changed, we're looking at live data.
    suspend fun upload(stamp: Int, leads: Int, frequency: Int, data: ByteArray): UUID {
        val chunk = Chunk(
            address = device.address.value!!,
            stamp = stamp,
            email = account.email.value!!,
            frequency = frequency,
            leads = leads,
            logged = 0,
            patientName = patient.name.value!!,
            patientPhone = patient.phone.value!!,
            data = data
        )
        Log.d(Uploader.TAG, "enetery for upload tag-------------------------------")
        chunks.insert(chunk)
        state.stats.cloud.pending = chunks.count()
        val request = OneTimeWorkRequestBuilder<ChunkWorker>()
            .setInputData(workDataOf(KEY_ADDRESS to device.address.value, KEY_TIMESTAMP to stamp))
            .setConstraints(constraints)
            .build()

        // Each request is independent of the others.  Normally, the work will be carried out in
        // order.  Requests may be dispatched out of order, coming out of a network unavailability
        // event, and that is okay.
        WorkManager.getInstance(context).enqueue(request)
        return request.id
    }

    fun note(stamp: Int, text: String, noteId: Long? = null): UUID {
        val request = OneTimeWorkRequestBuilder<NoteWorker>()
            .setInputData(
                workDataOf(
                    KEY_ADDRESS to device.address.value,
                    KEY_TIMESTAMP to stamp,
                    KEY_NOTE to text,
                    KEY_NOTE_ID to (noteId ?: -1L)
                )
            )
            .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.SECONDS)
            .setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueue(request)
        return request.id
    }

    companion object {
        private const val KEY_ADDRESS = "address"
        private const val KEY_RESULT = "result"
        private const val KEY_TIMESTAMP = "stamp"
        private const val KEY_NOTE = "note"
        private const val KEY_NOTE_ID = "note_id"
        private val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        private val firebase = FirebaseCrashlytics.getInstance()
        private const val TAG = "upload"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object UploaderModule {
    @Provides
    @Singleton
    fun provideUploader(
        @ApplicationContext context: Context,
        account: Account,
        device: Device,
        patient: Patient,
        chunks: ChunkDao,
        noteDao: NoteDao,
        state: State
    ): Uploader {


        return Uploader(context, account, device, patient, chunks, noteDao, state)
    }
}
