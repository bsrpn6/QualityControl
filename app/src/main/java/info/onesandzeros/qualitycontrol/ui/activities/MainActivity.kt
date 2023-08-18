package info.onesandzeros.qualitycontrol.ui.activities

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.databinding.ActivityMainBinding

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
        checkUserLoginStatus()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
