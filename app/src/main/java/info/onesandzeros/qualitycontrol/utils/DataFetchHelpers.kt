package info.onesandzeros.qualitycontrol.utils

import android.util.Log

object DataFetchHelpers {

    sealed class DataResult<out T> {
        data class Success<T>(val data: T) : DataResult<T>()
        data class Error(val exception: CustomException) : DataResult<Nothing>()
    }

    inline fun <T> fetchDataFromNetworkFirst(
        networkCall: () -> T,
        databaseCall: () -> T,
        saveToDatabase: (T) -> Unit
    ): DataResult<T> {
        val data: T

        try {
            data = networkCall.invoke()
            Log.d("NetworkData", "Fetched data from network: $data")
        } catch (exception: Exception) {
            Log.e("NetworkError", "Failed to fetch data from network", exception)
            // If network fails, try fetching from database
            return try {
                val localData = databaseCall.invoke()
                Log.d("LocalData", "Fetched data from local database: $localData")
                DataResult.Success(localData)
            } catch (dbException: Exception) {
                Log.e("DatabaseError", "Failed to fetch data from the database", dbException)
                DataResult.Error(DatabaseException("Failed to fetch data from the database."))
            }
        }

        // Save to database
        return try {
            saveToDatabase.invoke(data)
            Log.d("DatabaseSave", "Saved data to database successfully")
            DataResult.Success(data)
        } catch (dbException: CustomException) {
            Log.e("DatabaseError", "Failed to save data to database", dbException)
            DataResult.Error(dbException)
        }
    }
}
