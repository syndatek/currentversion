package com.carditek.kesar.module

import com.carditek.kesar.bluetooth.State
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object StateModule {
    @Provides
    @Singleton
    fun provideState(): State {
        return State()
    }
}
