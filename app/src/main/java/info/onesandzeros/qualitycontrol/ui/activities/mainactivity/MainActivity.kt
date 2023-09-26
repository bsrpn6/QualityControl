package info.onesandzeros.qualitycontrol.ui.activities.mainactivity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.databinding.ActivityMainBinding
import info.onesandzeros.qualitycontrol.ui.activities.baseactivity.BaseActivity

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels() // Use Hilt's viewModels delegate

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

        viewModel.isAppBlocked.observe(this) { isAppBlocked ->
            if (isAppBlocked) {
                binding.updateRequiredLayout.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
