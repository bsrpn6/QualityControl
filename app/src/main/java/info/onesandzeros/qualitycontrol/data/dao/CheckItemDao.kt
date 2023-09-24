package info.onesandzeros.qualitycontrol.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.onesandzeros.qualitycontrol.data.models.CheckItemEntity

@Dao
interface CheckItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckItem(checkItem: CheckItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckItems(checkItems: List<CheckItemEntity>)

    @Query("SELECT * FROM check_items WHERE lineId = :lineId AND checkTypeId = :checkTypeId AND productId = :productId")
    suspend fun getCheckItems(
        lineId: String,
        checkTypeId: String,
        productId: String
    ): List<CheckItemEntity>

    @Query("SELECT * FROM check_items WHERE id = :id")
    suspend fun getCheckItemById(id: String): CheckItemEntity?

    @Query("SELECT * FROM check_items")
    suspend fun getAllCheckItems(): List<CheckItemEntity>
}
