package info.onesandzeros.qualitycontrol.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.onesandzeros.qualitycontrol.data.models.LineEntity

@Dao
interface LineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lineEntities: List<LineEntity>)

    @Query("SELECT * FROM lines")
    suspend fun getAllLines(): List<LineEntity>

    @Query("SELECT * FROM lines WHERE departmentId = :departmentId")
    suspend fun getLinesByDepartmentId(departmentId: String): List<LineEntity>
}
