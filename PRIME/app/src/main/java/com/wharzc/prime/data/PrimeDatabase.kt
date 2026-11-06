package com.wharzc.prime.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Entity(tableName = "notifications")
data class FertilizerNotif(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val label: String,
    val time: String,
    val amPm: String,
    val isRecurring: Boolean,
    val dateOrRule: String
)

@Entity(tableName = "monitoring_logs")
data class MonitoringLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: String, // Format: MM/DD/YYYY
    val time: String, // Format: HH:MM AM/PM
    val avgN: Float, val avgP: Float, val avgK: Float,
    val avgTemp: Float, val avgTurbidity: Float, val avgPh: Float, val avgWaterLvl: Float
)

@Entity(tableName = "fertilizer_action_logs")
data class FertilizerActionLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: String,
    val time: String,
    val amPm: String,
    val quantity: String,
    val reason: String,
    val fieldAnalysis: String
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM monitoring_logs WHERE date = :targetDate ORDER BY time DESC")
    fun getMonitoringLogsByDate(targetDate: String): Flow<List<MonitoringLog>>

    @Query("SELECT * FROM fertilizer_action_logs WHERE date = :targetDate ORDER BY time DESC")
    fun getFertilizerLogsByDate(targetDate: String): Flow<List<FertilizerActionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFertilizerLog(log: FertilizerActionLog)
}

@Dao
interface NotifDao {
    @Query("SELECT * FROM notifications")
    fun getAllNotifs(): Flow<List<FertilizerNotif>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotif(notif: FertilizerNotif)

    @Delete
    suspend fun deleteNotif(notif: FertilizerNotif)
}

@Database(
    entities = [FertilizerNotif::class, MonitoringLog::class, FertilizerActionLog::class],
    version = 2,
    exportSchema = false
)
abstract class PrimeDatabase : RoomDatabase() {
    abstract fun notifDao(): NotifDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var INSTANCE: PrimeDatabase? = null

        fun getDatabase(context: Context): PrimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrimeDatabase::class.java,
                    "prime_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}