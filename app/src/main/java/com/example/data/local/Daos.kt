package com.example.data.local

import androidx.room.*
import com.example.data.model.DeviceEntity
import com.example.data.model.IntruderLogEntity
import com.example.data.model.TheftReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM registered_devices ORDER BY isThisDevice DESC, id DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM registered_devices WHERE id = :id")
    suspend fun getDeviceById(id: Long): DeviceEntity?

    @Query("SELECT COUNT(*) FROM registered_devices")
    fun getDeviceCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity): Long

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Delete
    suspend fun deleteDevice(device: DeviceEntity)
}

@Dao
interface TheftReportDao {
    @Query("SELECT * FROM theft_reports ORDER BY reportTimestamp DESC")
    fun getAllReports(): Flow<List<TheftReportEntity>>

    @Query("SELECT * FROM theft_reports WHERE imei LIKE '%' || :query || '%' OR deviceModel LIKE '%' || :query || '%'")
    fun searchReports(query: String): Flow<List<TheftReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: TheftReportEntity): Long

    @Update
    suspend fun updateReport(report: TheftReportEntity)

    @Delete
    suspend fun deleteReport(report: TheftReportEntity)

    @Query("SELECT COUNT(*) FROM theft_reports WHERE status = 'RECOVERED'")
    fun getRecoveredCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM theft_reports")
    fun getTotalReportsCount(): Flow<Int>
}

@Dao
interface IntruderDao {
    @Query("SELECT * FROM intruder_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<IntruderLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: IntruderLogEntity): Long

    @Query("DELETE FROM intruder_logs")
    suspend fun clearLogs()

    @Delete
    suspend fun deleteLog(log: IntruderLogEntity)
}
