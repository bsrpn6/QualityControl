package info.onesandzeros.qualitycontrol.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import info.onesandzeros.qualitycontrol.data.dao.DepartmentDao
import info.onesandzeros.qualitycontrol.data.dao.IDHNumbersDao
import info.onesandzeros.qualitycontrol.data.dao.LineDao
import info.onesandzeros.qualitycontrol.data.models.DepartmentEntity
import info.onesandzeros.qualitycontrol.data.models.IDHNumbersEntity
import info.onesandzeros.qualitycontrol.data.models.LineEntity

@TypeConverters(Converters::class)
@Database(
    entities = [DepartmentEntity::class, LineEntity::class, IDHNumbersEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun departmentDao(): DepartmentDao
    abstract fun lineDao(): LineDao
    abstract fun idhNumbersDao(): IDHNumbersDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
