package info.onesandzeros.qualitycontrol.utils

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseConfigManager @Inject constructor() {

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0) // fetch every hour
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    fun fetchRemoteConfigs(completion: (Boolean) -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                completion(task.isSuccessful)
            }
    }

    fun getMinimumSupportedVersion(): String {
        return remoteConfig.getString("min_supported_version")
    }
}
