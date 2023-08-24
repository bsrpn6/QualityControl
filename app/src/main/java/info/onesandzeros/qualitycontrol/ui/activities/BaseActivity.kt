package info.onesandzeros.qualitycontrol.ui.activities

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
    }

    private var shouldNavigateToLogin = false

    private lateinit var firebaseAuth: FirebaseAuth
    private val logoutHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        checkUserLoginStatus()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        // Reset the timer on user interaction (e.g., touch, keypress)
        resetLogoutTimer()
    }

    fun checkUserLoginStatus() {
        val user = firebaseAuth.currentUser
        if (user == null) {
            // User is not logged in, navigate to LoginFragment
            performNavigationToLogin()
        } else {
            // User is logged in, reset the logout timer
            resetLogoutTimer()
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if (shouldNavigateToLogin) {
            shouldNavigateToLogin = false
            performNavigationToLogin()
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
        // Logout the user and navigate to the LoginFragment
        Log.i(TAG, "User logout.")
        firebaseAuth.signOut()
        performNavigationToLogin()
    }
}
