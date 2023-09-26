package info.onesandzeros.qualitycontrol.ui.activities.mainactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import info.onesandzeros.qualitycontrol.BuildConfig
import info.onesandzeros.qualitycontrol.utils.FirebaseConfigManager
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val firebaseConfigManager: FirebaseConfigManager) :
    ViewModel() {

    // LiveData to observe the app block status
    val isAppBlocked: LiveData<Boolean> get() = _isAppBlocked
    private val _isAppBlocked = MutableLiveData<Boolean>()

    init {
        checkAppVersion()
    }

    private fun checkAppVersion() {
        firebaseConfigManager.fetchRemoteConfigs { success ->
            if (success) {
                val minSupportedVersion = firebaseConfigManager.getMinimumSupportedVersion()
                val currentVersion = BuildConfig.VERSION_NAME
                _isAppBlocked.value = isUpdateRequired(currentVersion, minSupportedVersion)
            } else {
                // Handle error, e.g., use cached value
            }
        }
    }

    private fun isUpdateRequired(currentVersion: String, minSupportedVersion: String): Boolean {
        val currentParts = currentVersion.split(".").map { it.toInt() }
        val minSupportedParts = minSupportedVersion.split(".").map { it.toInt() }

        for (i in currentParts.indices) {
            if (currentParts[i] < minSupportedParts[i]) {
                return true
            } else if (currentParts[i] > minSupportedParts[i]) {
                return false
            }
        }
        return false // They are equal
    }
}