package info.onesandzeros.qualitycontrol.ui.activities

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.constants.Constants.TIMEOUT_INTERVAL

abstract class BaseActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "BaseActivity"
        private const val PREFS_NAME = "InactivityPrefs"
        private const val LAST_INTERACTION_TIME_KEY = "LastInteractionTime"
    }

    private var shouldNavigateToLogin = false

    private lateinit var firebaseAuth: FirebaseAuth
    private val logoutHandler = Handler()

    private val sharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        checkUserLoginStatus()
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
            logout()
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

    fun checkUserLoginStatus() {
        val user = firebaseAuth.currentUser
        if (user == null) {
            performNavigationToLogin()
        } else {
            resetLogoutTimer()
        }
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
        logoutHandler.postDelayed(::logout, TIMEOUT_INTERVAL)
    }

    private fun logout() {
        Log.i(TAG, "User logout.")
        firebaseAuth.signOut()
        performNavigationToLogin()
    }
}
