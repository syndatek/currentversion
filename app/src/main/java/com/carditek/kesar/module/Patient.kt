package com.carditek.kesar.module

import android.content.Context
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

open class Patient(context: Context) {
    private val _name = MutableLiveData<String>()
    private val _phone = MutableLiveData<String>()
    private val _empty = MutableLiveData(true)
    val name: LiveData<String> = _name
    val phone: LiveData<String> = _phone
    val empty: LiveData<Boolean> = _empty

    fun set(name: String, phone: String) {
        preferences.edit(commit = true) {
            putString(PATIENT_NAME, name)
            putString(PATIENT_PHONE, phone)
        }

        _name.postValue(name)
        _phone.postValue(phone)
        _empty.postValue(name.isEmpty() and phone.isEmpty())
    }

    fun clear() {
        set("", "")
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        val name = preferences.getString(PATIENT_NAME, "")!!
        val phone = preferences.getString(PATIENT_PHONE, "")!!
        set(name, phone)
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
