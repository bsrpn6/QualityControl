package info.onesandzeros.qualitycontrol.ui.fragments.checks

import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import info.onesandzeros.qualitycontrol.api.MyApi
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.ChecksSubmissionRequest
import info.onesandzeros.qualitycontrol.api.models.ImageUploadRequest
import info.onesandzeros.qualitycontrol.api.models.SubmissionResult
import info.onesandzeros.qualitycontrol.data.AppDatabase
import info.onesandzeros.qualitycontrol.data.dao.CheckItemDao
import info.onesandzeros.qualitycontrol.data.models.CheckItemEntity
import info.onesandzeros.qualitycontrol.data.models.CheckSubmissionEntity
import info.onesandzeros.qualitycontrol.utils.DataFetchHelpers
import info.onesandzeros.qualitycontrol.utils.NetworkException
import info.onesandzeros.qualitycontrol.utils.toCheckItemList
import okhttp3.ResponseBody
import java.io.IOException
import javax.inject.Inject

class ChecksRepository @Inject constructor(
    private val myApi: MyApi,
    private val appDatabase: AppDatabase,
    private val checkItemDao: CheckItemDao
) {

    suspend fun getChecks(
        lineId: String,
        checkTypeId: String,
        idhNumberId: String
    ): DataFetchHelpers.DataResult<List<CheckItem>> {
        return DataFetchHelpers.fetchDataFromNetworkFirst(networkCall = {
            myApi.getChecks(
                lineId,
                checkTypeId,
                idhNumberId
            )
        },
            databaseCall = { getChecksFromDatabase(lineId, checkTypeId, idhNumberId) },
            saveToDatabase = { insertChecks(lineId, checkTypeId, idhNumberId, it) })
    }

    private suspend fun getChecksFromDatabase(
        lineId: String,
        checkTypeId: String,
        idhNumberId: String
    ): List<CheckItem> {
        val checkEntities = checkItemDao.getCheckItems(lineId, checkTypeId, idhNumberId)
        return checkEntities.toCheckItemList()
    }

    private suspend fun insertChecks(
        lineId: String,
        checkTypeId: String,
        idhNumberId: String,
        checkItems: List<CheckItem>
    ) {
        val checkItemEntities = checkItems.map { checkItem ->
            CheckItemEntity(
                checkItem._id,
                checkItem.section,
                checkItem.type,
                checkItem.title,
                checkItem.description,
                checkItem.expectedValue,
                checkItem.images,
                lineId,
                checkTypeId,
                idhNumberId
            )
        }
        checkItemDao.insertCheckItems(checkItemEntities)
    }

    suspend fun submitChecksToApi(submissionData: ChecksSubmissionRequest): SubmissionResult {
        val response = myApi.submitChecks(submissionData)
        if (response.isSuccessful) {
            return response.body() ?: throw NetworkException("No response body")
        } else {
            throw NetworkException(
                "Failed to submit checks to API: ${
                    response.errorBody()?.string()
                }"
            )
        }
    }

    suspend fun uploadImagesToBlobStorage(
        uri: Uri,
        fileName: String,
        contentResolver: ContentResolver
    ): ResponseBody {
        val imageBase64 = uriToBase64(contentResolver, uri) // Changed method name for clarity
        val imageUploadRequest = ImageUploadRequest(fileName, imageBase64) // Use passed fileName
        val response = myApi.submitResultPhotos(imageUploadRequest)

        if (response.isSuccessful) {
            return response.body() ?: throw NetworkException("No response body")
        } else {
            throw NetworkException(
                "Failed to upload result photos to API: ${
                    response.errorBody()?.string()
                }"
            )
        }
    }

    private fun uriToBase64(contentResolver: ContentResolver, uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: throw IOException("Failed to read bytes from URI")
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }


    suspend fun saveSubmissionToLocalDatabase(submissionData: ChecksSubmissionRequest) {
        val localSubmission = CheckSubmissionEntity(
            checkStartTimestamp = submissionData.checkStartTimestamp,
            username = submissionData.username,
            department = submissionData.department,
            line = submissionData.line,
            idhNumber = submissionData.idhNumber,
            checks = submissionData.checks
        )
        appDatabase.checkSubmissionDao().insertSubmission(localSubmission)
    }
}
