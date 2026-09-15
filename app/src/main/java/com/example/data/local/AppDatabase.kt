package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DeviceEntity
import com.example.data.model.IntruderLogEntity
import com.example.data.model.TheftReportEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DeviceEntity::class, TheftReportEntity::class, IntruderLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun theftReportDao(): TheftReportDao
    abstract fun intruderDao(): IntruderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "thief_hunter_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.deviceDao(), database.theftReportDao())
                    }
                }
            }

            suspend fun populateInitialData(deviceDao: DeviceDao, theftDao: TheftReportDao) {
                // Pre-register current device
                deviceDao.insertDevice(
                    DeviceEntity(
                        brand = "Google",
                        model = "Pixel (Current Device)",
                        imei1 = "358941094829103",
                        imei2 = "358941094829104",
                        serialNumber = "GPL-9921-SEC",
                        purchaseDate = "2025-01-15",
                        ownerName = "Active User",
                        emergencyPhone = "+1 (555) 019-2831",
                        status = "SAFE",
                        notes = "Primary protected device equipped with Thief Hunter Shield.",
                        isThisDevice = true
                    )
                )

                // Prepopulate community stolen registry entries for immediate search & test
                theftDao.insertReport(
                    TheftReportEntity(
                        deviceModel = "Galaxy S24 Ultra",
                        brand = "Samsung",
                        imei = "356281093847291",
                        theftDate = "2026-03-10",
                        locationName = "Central Metro Transit Hub, Platform 3",
                        latitude = 37.7749,
                        longitude = -122.4194,
                        firNumber = "FIR-2026-9812",
                        policeStation = "Downtown Central Police Station",
                        officerName = "Officer Miller",
                        contactEmail = "recovery.notice@safecommunity.org",
                        contactPhone = "+1 (555) 923-4411",
                        rewardAmount = "$150 USD",
                        description = "Titanium Gray device stolen while boarding morning train. Registered in Thief Hunter network.",
                        status = "ACTIVE_SEARCH"
                    )
                )
                theftDao.insertReport(
                    TheftReportEntity(
                        deviceModel = "iPhone 15 Pro",
                        brand = "Apple",
                        imei = "359182736451234",
                        theftDate = "2026-02-28",
                        locationName = "City Mall Food Court",
                        latitude = 40.7128,
                        longitude = -74.0060,
                        firNumber = "FIR-2026-4409",
                        policeStation = "Midtown Precinct 12",
                        officerName = "Det. Reynolds",
                        contactEmail = "owner.alert@safecommunity.org",
                        contactPhone = "+1 (555) 773-1920",
                        rewardAmount = "$200 USD",
                        description = "Stolen from cafe table during lunch hour. Black casing with clear back protector.",
                        status = "RECOVERED"
                    )
                )
            }
        }
    }
}
