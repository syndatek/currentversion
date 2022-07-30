package com.carditek.kesar

import android.content.Context
import androidx.room.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Entity(tableName = "chunk", primaryKeys = ["address", "timestamp"])
data class Chunk(
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "timestamp") val stamp: Int,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "frequency") val frequency: Int,
    @ColumnInfo(name = "leads") val leads: Int,
    @ColumnInfo(name = "logged") val logged: Int,
    @ColumnInfo(name = "patient_name") val patientName: String,
    @ColumnInfo(name = "patient_phone") val patientPhone: String,
    @ColumnInfo(name = "data", typeAffinity = ColumnInfo.BLOB) val data: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chunk

        if (address != other.address) return false
        if (stamp != other.stamp) return false
        if (email != other.email) return false
        if (frequency != other.frequency) return false
        if (leads != other.leads) return false
        if (logged != other.logged) return false
        if (patientName != other.patientName) return false
        if (patientPhone != other.patientPhone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + stamp
        result = 31 * result + email.hashCode()
        result = 31 * result + frequency
        result = 31 * result + leads
        result = 31 * result + logged
        result = 31 * result + patientName.hashCode()
        result = 31 * result + patientPhone.hashCode()
        return result
    }

    init {
        if (!MAC_ADDRESS_REGEX.matches(address))
            throw IllegalArgumentException("Invalid MAC address: '$address'")
        if (stamp < TIMESTAMP_MIN)
            throw IllegalArgumentException("Invalid timestamp: $stamp")
        if (!VALID_FREQUENCIES.contains(frequency))
            throw IllegalArgumentException("Invalid frequency: $frequency")
        if (!VALID_LEADS.contains(leads))
            throw IllegalArgumentException("Invalid leads: $leads")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            throw IllegalArgumentException("Invalid email: '$email'")
        if (!android.util.Patterns.PHONE.matcher(patientPhone).matches())
            throw IllegalArgumentException("Invalid phone number: '$patientPhone'")
        if (patientName.length < 3)
            throw IllegalArgumentException("Patient name too short: '$patientName'")
        if (stamp % CHUNK_LENGTH_SECONDS != 0)
            throw IllegalArgumentException("$stamp not multiple of $CHUNK_LENGTH_SECONDS")
        val expected = CHUNK_LENGTH_SECONDS * 3 * leads * frequency
        if (data.size != expected)
            throw IllegalArgumentException("Data ${data.size} bytes, expected $expected")
    }

    companion object {
        private val MAC_ADDRESS_REGEX = Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
        private const val TIMESTAMP_MIN = 1262332800
        private val VALID_FREQUENCIES = setOf(100, 500, 1000, 2000)
        private val VALID_LEADS = setOf(1, 2, 8)
        private const val CHUNK_LENGTH_SECONDS = 15
    }
}

@Dao
interface ChunkDao {
    @Query("SELECT * FROM chunk")
    suspend fun getAll(): List<Chunk>

    @Query("SELECT * FROM chunk WHERE address = :address AND timestamp = :stamp")
    suspend fun get(address: String, stamp: Int): Chunk?

    @Query("SELECT COUNT(*) FROM chunk")
    fun count(): Int

    @Query("DELETE from chunk")
    fun clear()

    @Insert
    suspend fun insert(chunk: Chunk)

    @Query("DELETE FROM chunk WHERE address = :address AND timestamp = :stamp")
    suspend fun delete(address: String, stamp: Int)
}

@Database(entities = [Chunk::class], version = 1)
abstract class ChunkDatabase : RoomDatabase() {
    abstract fun chunkDao(): ChunkDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideChunkDatabase(@ApplicationContext context: Context): ChunkDatabase {
        return Room.databaseBuilder(context, ChunkDatabase::class.java, "Chunks").build()
    }

    @Provides
    fun provideChunkDao(database: ChunkDatabase): ChunkDao {
        return database.chunkDao()
    }
}
