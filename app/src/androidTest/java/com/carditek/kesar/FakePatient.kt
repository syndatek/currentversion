package com.carditek.kesar

import android.content.Context
import com.carditek.kesar.module.Patient
import com.carditek.kesar.module.PatientModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

class FakePatient(context: Context): Patient(context) {
    init {
        set("Carditek Patient", "+1 (650) 555-5555")
    }
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PatientModule::class]
)
object FakePatientModule {
    @Provides
    @Singleton
    fun providePatient(@ApplicationContext context: Context): Patient = FakePatient(context)
}
