package info.onesandzeros.qualitycontrol.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.onesandzeros.qualitycontrol.data.models.DepartmentEntity

@Dao
interface DepartmentDao {

    @Query("SELECT * FROM departments")
    suspend fun getAllDepartments(): List<DepartmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartments(departmentEntities: List<DepartmentEntity>)
}
