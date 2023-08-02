package info.onesandzeros.qualitycontrol.ui.activities

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.databinding.ActivityMainBinding
import info.onesandzeros.qualitycontrol.ui.fragments.LoginFragment

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use ViewBinding to set the content view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // Find the NavHostFragment using findFragmentById
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController

        // Set the navigation controller as the action bar's support action bar
        setupActionBarWithNavController(this, navController)

        // Check if the user is logged in
        if (!isUserLoggedIn()) {
            // User is not logged in, navigate to LoginFragment
            navigateToLoginFragment()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        // Implement your logic here to check if the user is already logged in.
        // You can use shared preferences, authentication tokens, or other methods for this check.
        return true // Replace with your actual check
    }

    private fun navigateToLoginFragment() {
        // Implement navigation to the LoginFragment here.
        // For example, you can use supportFragmentManager to replace the current fragment with LoginFragment.

        // Create an instance of the LoginFragment
        val loginFragment = LoginFragment()

        // Replace the current fragment with the LoginFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, loginFragment)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)?.findNavController()
        return navController?.navigateUp() ?: false
    }
}
