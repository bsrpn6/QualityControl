package info.onesandzeros.qualitycontrol.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.CheckType
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {
    val checkStartTimestamp = MutableLiveData<Long>()
    val usernameLiveData = MutableLiveData<String?>()
    val departmentLiveData = MutableLiveData<Department?>()
    val checkTypeLiveData = MutableLiveData<CheckType?>()
    val lineLiveData = MutableLiveData<Line?>()
    val idhNumberLiveData = MutableLiveData<IDHNumbers?>()
    val checksLiveData = MutableLiveData<Map<String, List<CheckItem>>?>()
    val photosLiveData = MutableLiveData<Map<String, MutableList<Uri>>?>()

    init {
        photosLiveData.value = mutableMapOf()
    }

    fun clearDataSaveUserAndID() {
        checksLiveData.value = null
        photosLiveData.value = null
    }

    fun clearDataSaveUser() {
        departmentLiveData.value = null
        lineLiveData.value = null
        idhNumberLiveData.value = null
        checksLiveData.value = null
        photosLiveData.value = null
    }

    fun clearUserName() {
        usernameLiveData.value = null
    }

    fun addImageUri(section: String, uri: Uri) {
        val currentPhotos = (photosLiveData.value ?: mutableMapOf()).toMutableMap()
        val sectionPhotos = (currentPhotos[section] ?: mutableListOf()).toMutableList()

        // Add the URI to the section's photo list
        sectionPhotos.add(uri)

        // Update the map
        currentPhotos[section] = sectionPhotos

        // Post the updated data back to the LiveData
        photosLiveData.postValue(currentPhotos)
    }

    fun removeImageUri(section: String, uri: Uri) {
        val currentPhotos = (photosLiveData.value ?: mutableMapOf()).toMutableMap()
        val sectionPhotos = (currentPhotos[section] ?: mutableListOf()).toMutableList()

        // Remove the URI from the section's photo list
        sectionPhotos.remove(uri)

        // Update the map only if the section's photo list is not empty
        if (sectionPhotos.isNotEmpty()) {
            currentPhotos[section] = sectionPhotos
        } else {
            currentPhotos.remove(section)
        }

        // Post the updated data back to the LiveData
        photosLiveData.postValue(currentPhotos)
    }

    fun getPhotosForSection(section: String): List<Uri> {
        return photosLiveData.value?.get(section) ?: emptyList()
    }

}

