package info.onesandzeros.qualitycontrol

import ChecksAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.api.MyApi
import info.onesandzeros.qualitycontrol.databinding.FragmentChecksBinding
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.utils.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class ChecksFragment : Fragment(R.layout.fragment_checks) {
    private lateinit var binding: FragmentChecksBinding
    private lateinit var adapter: ChecksAdapter

    @Inject
    lateinit var activityResultRegistry: ActivityResultRegistry

    @Inject
    lateinit var myApi: MyApi


    private var checksMap: Map<String, List<CheckItem>> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChecksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch the list of checks from the JSON file and categorize them by type
        loadChecksDataFromApi()

        // Set up the tab layout and view pager
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        // Find the buttons by their IDs
        val exitButton = binding.exitButton
        val submitButton = binding.submitButton

        val fragmentList = mutableListOf<Fragment>()
        val tabTitleList = mutableListOf<String>()

        for ((checkType, checkItems) in checksMap) {
            // Create a new fragment for each check type
            val checkTypeFragment =
                CheckTypeFragment.newInstance(checkItems, activityResultRegistry)

            // Add the fragment to the list
            fragmentList.add(checkTypeFragment)

            // Add the tab title to the list
            tabTitleList.add(checkType)
        }

        val pagerAdapter = ChecksPagerAdapter(fragmentList, this)
        viewPager.adapter = pagerAdapter

        // Connect the tab layout with the view pager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Set the custom tab view with image and text for each tab
            val customTabView = LayoutInflater.from(tabLayout.context)
                .inflate(R.layout.custom_tab_layout, null, false)
            val tabIcon = customTabView.findViewById<ImageView>(R.id.tabIcon)
            val tabText = customTabView.findViewById<TextView>(R.id.tabText)
            val checkType = tabTitleList[position]
            tabIcon.setImageResource(getTabIconResourceId(checkType)) // Set the image for each tab
            tabText.text = StringUtils.formatTabText(checkType) // Set the text for each tab
            tab.customView = customTabView
        }.attach()

        // Set click listeners for the buttons
        exitButton.setOnClickListener {
            // Handle exit checks action (e.g., navigate back to previous fragment)
            findNavController().popBackStack()
        }

        submitButton.setOnClickListener {
            // Handle submit checks action (e.g., perform checks submission logic)
            // You can define your own logic here based on your app's requirements.
            // Navigate to SubmissionResultFragment and pass the totalFailedChecks as an argument
            val totalFailedChecks = calculateTotalFailedChecks()
            val action = ChecksFragmentDirections.actionChecksFragmentToSubmissionResultFragment(
                totalFailedChecks
            )
            findNavController().navigate(action)
        }
    }

    private fun calculateTotalFailedChecks(): Int {
        var totalFailedChecks = 0

        for ((_, checkItems) in checksMap) {
            for (checkItem in checkItems) {
                val value = checkItem.value
                val result = checkItem.result

                // Check if the user input value does not match the expected value
                if (result != null && result != value) {
                    totalFailedChecks++
                }
            }
        }

        return totalFailedChecks
    }

    private fun loadChecksDataFromApi() {
        myApi.getChecksData().enqueue(object : Callback<List<CheckItem>> {
            override fun onResponse(
                call: Call<List<CheckItem>>,
                response: Response<List<CheckItem>>
            ) {
                if (response.isSuccessful) {
                    val checkResponse = response.body()
                    checkResponse?.let { checksResponse ->
                        // Update the checksMap with the data from the API response
                        checksMap = categorizeChecksByType(checksResponse)

                        // Update the UI with the new data
                        setupViewPagerAndTabs()
                    }
                } else {
                    // Handle the API error here
                    Toast.makeText(
                        requireContext(),
                        "Failed to load checks data from the API.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<CheckItem>>, t: Throwable) {
                // Handle the network failure here
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch checks data. Check your internet connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun categorizeChecksByType(checks: List<CheckItem>): Map<String, List<CheckItem>> {
        val checksMap = mutableMapOf<String, MutableList<CheckItem>>()

        for (checkItem in checks) {
            val checkType = checkItem.checkType

            if (checksMap.containsKey(checkType)) {
                checksMap[checkType]?.add(checkItem)
            } else {
                checksMap[checkType] = mutableListOf(checkItem)
            }
        }

        return checksMap
    }

    private fun setupViewPagerAndTabs() {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        val fragmentList = mutableListOf<Fragment>()
        val tabTitleList = mutableListOf<String>()

        for ((checkType, checkItems) in checksMap) {
            // Create a new fragment for each check type
            val checkTypeFragment =
                CheckTypeFragment.newInstance(checkItems, activityResultRegistry)

            // Add the fragment to the list
            fragmentList.add(checkTypeFragment)

            // Add the tab title to the list
            tabTitleList.add(checkType)
        }

        val pagerAdapter = ChecksPagerAdapter(fragmentList, this)
        viewPager.adapter = pagerAdapter

        // Connect the tab layout with the view pager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Set the custom tab view with image and text for each tab
            val customTabView = LayoutInflater.from(tabLayout.context)
                .inflate(R.layout.custom_tab_layout, null, false)
            val tabIcon = customTabView.findViewById<ImageView>(R.id.tabIcon)
            val tabText = customTabView.findViewById<TextView>(R.id.tabText)
            val checkType = tabTitleList[position]
            tabIcon.setImageResource(getTabIconResourceId(checkType)) // Set the image for each tab
            tabText.text = StringUtils.formatTabText(checkType) // Set the text for each tab
            tab.customView = customTabView
        }.attach()
    }

    // Helper method to get the tab icon resource based on the check type
    private fun getTabIconResourceId(checkType: String): Int {
        // Implement your logic here to return the appropriate drawable resource for each tab
        // based on the checkType.
        // For example, you can use a when statement or a mapping of checkType to drawable resource.
        // For simplicity, you can name your drawable resources after the check types, e.g., "case.png", "con.png", etc.
        // and use the following code:
        val fileNameWithoutExtension = checkType.substringBeforeLast(".")
        return resources.getIdentifier(
            fileNameWithoutExtension,
            "drawable",
            requireActivity().packageName
        )
    }

    private inner class ChecksPagerAdapter(
        private val fragmentList: List<Fragment>,
        fragment: Fragment
    ) : FragmentStateAdapter(fragment.childFragmentManager, fragment.viewLifecycleOwner.lifecycle) {
        override fun getItemCount(): Int = fragmentList.size

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}
