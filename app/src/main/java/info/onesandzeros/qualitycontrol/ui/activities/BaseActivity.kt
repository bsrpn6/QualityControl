package info.onesandzeros.qualitycontrol.ui.activities

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

abstract class BaseActivity : AppCompatActivity() {
    companion object {
        private const val TIMEOUT_INTERVAL = 15 * 60 * 1000L // 15 minutes timeout
    }

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

    private fun checkUserLoginStatus() {
        val user = firebaseAuth.currentUser
        if (user == null) {
            // User is not logged in, navigate to LoginFragment
            navigateToLoginFragment()
        } else {
            // User is logged in, reset the logout timer
            resetLogoutTimer()
        }
    }

    private fun navigateToLoginFragment() {
        // Override this method in MainActivity to navigate to the LoginFragment
    }

    private fun resetLogoutTimer() {
        logoutHandler.removeCallbacksAndMessages(null)
        logoutHandler.postDelayed(::logout, TIMEOUT_INTERVAL)
    }

    private fun logout() {
        // Logout the user and navigate to the LoginFragment
        firebaseAuth.signOut()
        navigateToLoginFragment()
    }
}
