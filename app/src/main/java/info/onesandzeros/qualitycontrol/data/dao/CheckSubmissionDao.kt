package info.onesandzeros.qualitycontrol.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.data.models.CheckSubmissionEntity

@Dao
interface CheckSubmissionDao {
    @Insert
    suspend fun insertSubmission(submission: CheckSubmissionEntity)

    @Query("SELECT * FROM check_submissions")
    fun getAllSubmissions(): LiveData<List<CheckSubmissionEntity>>

    @Query("SELECT * FROM check_submissions  WHERE line = :line")
    fun getAllSubmissionsForLine(line: Line): LiveData<List<CheckSubmissionEntity>>
}
