package com.carditek.kesar

import com.carditek.kesar.util.BackendModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

class FakeBackend: Backend {
    private val chunks = HashMap<String, Chunk>()

    override suspend fun me(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun fetch(address: String, stamp: Int): ByteArray? {
        TODO("Not yet implemented")
    }

    override suspend fun store(chunk: Chunk) {
        val key = "${chunk.address}/${chunk.stamp}"
        chunks[key] = chunk
    }

    override suspend fun note(address: String, stamp: Int, text: String) {
        TODO("Not yet implemented")
    }
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [BackendModule::class]
)
object FakeBackendModule {
    @Provides
    @Singleton
    fun provideBackend(): Backend = FakeBackend()
}
