package info.onesandzeros.qualitycontrol.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.onesandzeros.qualitycontrol.data.models.CheckTypeEntity

@Dao
interface CheckTypeDao {

    @Query("SELECT * FROM check_types WHERE lineId = :lineId")
    suspend fun getAllCheckTypes(lineId: String): List<CheckTypeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckTypes(checkTypeEntities: List<CheckTypeEntity?>)
}