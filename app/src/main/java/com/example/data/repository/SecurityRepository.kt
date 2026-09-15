package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.DeviceEntity
import com.example.data.model.IntruderLogEntity
import com.example.data.model.TheftReportEntity
import kotlinx.coroutines.flow.Flow

class SecurityRepository(private val database: AppDatabase) {

    val allDevices: Flow<List<DeviceEntity>> = database.deviceDao().getAllDevices()
    val deviceCount: Flow<Int> = database.deviceDao().getDeviceCount()

    suspend fun insertDevice(device: DeviceEntity): Long {
        return database.deviceDao().insertDevice(device)
    }

    suspend fun updateDevice(device: DeviceEntity) {
        database.deviceDao().updateDevice(device)
    }

    suspend fun deleteDevice(device: DeviceEntity) {
        database.deviceDao().deleteDevice(device)
    }

    val allTheftReports: Flow<List<TheftReportEntity>> = database.theftReportDao().getAllReports()
    val recoveredReportsCount: Flow<Int> = database.theftReportDao().getRecoveredCount()
    val totalReportsCount: Flow<Int> = database.theftReportDao().getTotalReportsCount()

    fun searchTheftReports(query: String): Flow<List<TheftReportEntity>> {
        return database.theftReportDao().searchReports(query)
    }

    suspend fun insertTheftReport(report: TheftReportEntity): Long {
        return database.theftReportDao().insertReport(report)
    }

    val intruderLogs: Flow<List<IntruderLogEntity>> = database.intruderDao().getAllLogs()

    suspend fun insertIntruderLog(log: IntruderLogEntity): Long {
        return database.intruderDao().insertLog(log)
    }

    suspend fun clearIntruderLogs() {
        database.intruderDao().clearLogs()
    }

    suspend fun deleteIntruderLog(log: IntruderLogEntity) {
        database.intruderDao().deleteLog(log)
    }
}
