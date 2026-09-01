package com.carditek.kesar.module

import android.content.Context
import com.carditek.kesar.Cache
import com.carditek.kesar.Device
import com.carditek.kesar.bluetooth.State
import com.carditek.kesar.util.filters.edgecomputing.CsvManager
import com.carditek.kesar.util.filters.edgecomputing.EdgeComputingProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object CacheModule {
    @Provides
    @Singleton
    fun provideCsvManager(@ApplicationContext context: Context): CsvManager {
        return CsvManager(context)
    }

    @Provides
    @Singleton
    fun provideEdgeComputingProcessor(
        @ApplicationContext context: Context,
        csvManager: CsvManager,
        patient: Patient,
        state: State
    ): EdgeComputingProcessor {
        return EdgeComputingProcessor(context, csvManager, patient, state)
    }

    @Provides
    @Singleton
    fun provideCache(
        @ApplicationContext context: Context,
        device: Device,
        edgeComputingProcessor: EdgeComputingProcessor
    ): Cache {
        return Cache(context, device, edgeComputingProcessor)
    }
}




