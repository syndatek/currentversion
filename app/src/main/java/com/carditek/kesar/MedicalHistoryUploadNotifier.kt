package com.carditek.kesar

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fires when a medical history note has been uploaded and marked in the DB ([Uploader.NoteWorker] success).
 * UI (e.g. MainActivity) collects this to show a single, reliable "saved" message on any screen.
 */
@Singleton
class MedicalHistoryUploadNotifier @Inject constructor() {

    private val _events = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val medicalHistorySavedEvents: SharedFlow<Unit> = _events.asSharedFlow()

    fun notifyMedicalHistorySaved() {
        _events.tryEmit(Unit)
    }
}
