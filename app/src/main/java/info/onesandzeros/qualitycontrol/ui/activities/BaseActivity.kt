package info.onesandzeros.qualitycontrol.ui.activities

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.constants.Constants.TIMEOUT_INTERVAL
import info.onesandzeros.qualitycontrol.ui.viewmodels.BaseActivityViewModel

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "BaseActivity"
        private const val PREFS_NAME = "InactivityPrefs"
        private const val LAST_INTERACTION_TIME_KEY = "LastInteractionTime"
    }

    private val viewModel: BaseActivityViewModel by viewModels()

    private val sharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val logoutHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isUserAuthenticated.observe(this) { isAuthenticated ->
            if (!isAuthenticated) {
                performNavigationToLogin()
            } else {
                resetLogoutTimer()
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        // Record the current time on user interaction
        sharedPreferences.edit()
            .putLong(LAST_INTERACTION_TIME_KEY, System.currentTimeMillis())
            .apply()
        resetLogoutTimer()
    }

    override fun onResume() {
        super.onResume()
        val lastInteractionTime = sharedPreferences.getLong(LAST_INTERACTION_TIME_KEY, 0)
        if (System.currentTimeMillis() - lastInteractionTime > TIMEOUT_INTERVAL) {
            viewModel.logout()
        } else {
            resetLogoutTimer()
        }
    }

    override fun onPause() {
        super.onPause()
        logoutHandler.removeCallbacksAndMessages(null)
        sharedPreferences.edit()
            .putLong(LAST_INTERACTION_TIME_KEY, System.currentTimeMillis())
            .apply()
    }

    private fun performNavigationToLogin() {
        val navController =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)?.findNavController()
        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(R.id.loginFragment, true)
            .build()

        navController?.navigate(R.id.loginFragment, null, navOptions)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)?.findNavController()
        return navController?.navigateUp() ?: false
    }

    private fun resetLogoutTimer() {
        logoutHandler.removeCallbacksAndMessages(null)
        logoutHandler.postDelayed({ viewModel.logout() }, TIMEOUT_INTERVAL)
    }
}
