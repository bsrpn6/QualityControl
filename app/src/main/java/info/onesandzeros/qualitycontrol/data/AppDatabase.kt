package info.onesandzeros.qualitycontrol.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import info.onesandzeros.qualitycontrol.data.dao.CheckSubmissionDao
import info.onesandzeros.qualitycontrol.data.dao.CheckTypeDao
import info.onesandzeros.qualitycontrol.data.dao.DepartmentDao
import info.onesandzeros.qualitycontrol.data.dao.IDHNumbersDao
import info.onesandzeros.qualitycontrol.data.dao.LineDao
import info.onesandzeros.qualitycontrol.data.models.CheckSubmissionEntity
import info.onesandzeros.qualitycontrol.data.models.CheckTypeEntity
import info.onesandzeros.qualitycontrol.data.models.DepartmentEntity
import info.onesandzeros.qualitycontrol.data.models.IDHNumbersEntity
import info.onesandzeros.qualitycontrol.data.models.LineEntity

@TypeConverters(Converters::class)
@Database(
    entities = [DepartmentEntity::class, LineEntity::class, CheckTypeEntity::class, IDHNumbersEntity::class, CheckSubmissionEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun departmentDao(): DepartmentDao
    abstract fun lineDao(): LineDao
    abstract fun checkTypeDao(): CheckTypeDao
    abstract fun idhNumbersDao(): IDHNumbersDao
    abstract fun checkSubmissionDao(): CheckSubmissionDao
}
