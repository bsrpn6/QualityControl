package info.onesandzeros.qualitycontrol.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.onesandzeros.qualitycontrol.data.models.IDHNumbersEntity

@Dao
interface IDHNumbersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIDHNumbers(idhNumberEntities: List<IDHNumbersEntity>)

    @Query("SELECT * FROM idh_numbers WHERE line_id = :lineId")
    suspend fun getIDHNumbersByLineId(lineId: Int): List<IDHNumbersEntity>
}