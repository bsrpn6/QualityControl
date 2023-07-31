package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import info.onesandzeros.qualitycontrol.QualityControlApplication
import info.onesandzeros.qualitycontrol.data.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(QualityControlApplication::class)
object RoomModule {
    @Provides
    @Singleton
    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "your_database_name"
        ).build()
    }
}
