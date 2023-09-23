package info.onesandzeros.qualitycontrol.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import info.onesandzeros.qualitycontrol.data.AppDatabase
import info.onesandzeros.qualitycontrol.data.dao.CheckSubmissionDao
import info.onesandzeros.qualitycontrol.data.dao.CheckTypeDao
import info.onesandzeros.qualitycontrol.data.dao.DepartmentDao
import info.onesandzeros.qualitycontrol.data.dao.IDHNumbersDao
import info.onesandzeros.qualitycontrol.data.dao.LineDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideDepartmentDao(database: AppDatabase): DepartmentDao {
        return database.departmentDao()
    }

    @Provides
    fun provideLineDao(database: AppDatabase): LineDao {
        return database.lineDao()
    }

    @Provides
    fun provideCheckTypeDao(database: AppDatabase): CheckTypeDao {
        return database.checkTypeDao()
    }

    @Provides
    fun provideIDHNumbersDao(database: AppDatabase): IDHNumbersDao {
        return database.idhNumbersDao()
    }

    @Provides
    fun provideCheckSubmissionDao(database: AppDatabase): CheckSubmissionDao {
        return database.checkSubmissionDao()
    }
}
