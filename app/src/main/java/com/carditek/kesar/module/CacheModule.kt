package com.carditek.kesar.module

import android.content.Context
import com.carditek.kesar.Cache
import com.carditek.kesar.Device
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
    fun provideState(@ApplicationContext context: Context, device: Device): Cache {
        return Cache(context, device)
    }
}
