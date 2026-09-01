
package com.carditek.kesar.module

import android.content.Context
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

open class Patient(private val context: Context) {

    private val _name = MutableLiveData<String>()
    private val _phone = MutableLiveData<String>()
    private val _empty = MutableLiveData(true)

    val name: LiveData<String> = _name
    val phone: LiveData<String> = _phone
    val empty: LiveData<Boolean> = _empty

    // Your given regex pattern
    private val pattern = Regex(
        """^[A-Za-z]{3}\d+\s+([A-Za-z]+(\s+[A-Za-z]+){0,5})\s+([1-9][0-9]?|1[01][0-9]|120)/(M|F|m|f)$"""
    )

    fun set(name: String, phone: String) {
        // Validate name format before saving
        if (name.isNotBlank() && !pattern.matches(name.trim())) {
            Toast.makeText(context, "Name is invalid", Toast.LENGTH_SHORT).show()
            return
        }

        preferences.edit(commit = true) {
            putString(PATIENT_NAME, name)
            putString(PATIENT_PHONE, phone)
        }

        _name.postValue(name)
        _phone.postValue(phone)
        _empty.postValue(name.isEmpty() && phone.isEmpty())
    }

    fun clear() {
        set("", "")
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        val savedName = preferences.getString(PATIENT_NAME, "") ?: ""
        val savedPhone = preferences.getString(PATIENT_PHONE, "") ?: ""
        set(savedName, savedPhone)
    }

    companion object {
        private const val PATIENT_NAME = "patient_name"
        private const val PATIENT_PHONE = "patient_phone"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object PatientModule {
    @Provides
    @Singleton
    fun providePatient(@ApplicationContext context: Context): Patient {
        return Patient(context)
    }
}
