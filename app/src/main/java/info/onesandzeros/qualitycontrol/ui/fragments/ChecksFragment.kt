package info.onesandzeros.qualitycontrol.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.MyApi
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.ChecksSubmissionRequest
import info.onesandzeros.qualitycontrol.api.models.SubmissionResult
import info.onesandzeros.qualitycontrol.data.AppDatabase
import info.onesandzeros.qualitycontrol.databinding.FragmentChecksBinding
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.data.models.CheckSubmissionEntity
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel
import info.onesandzeros.qualitycontrol.utils.StringUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class ChecksFragment : Fragment(R.layout.fragment_checks) {
    private lateinit var binding: FragmentChecksBinding

    @Inject
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var activityResultRegistry: ActivityResultRegistry

    @Inject
    lateinit var myApi: MyApi

    @Inject
    lateinit var appDatabase: AppDatabase


    private var checksMap: Map<String, List<CheckItem>> = emptyMap()

    private val sharedViewModel: SharedViewModel by activityViewModels()

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

        // Find the buttons by their IDs
        // Find the buttons by their IDs
        val exitButton = binding.exitButton
        val submitButton = binding.submitButton

        // Set click listeners for the buttons
        exitButton.setOnClickListener {
            // Handle exit checks action (e.g., navigate back to previous fragment)
            findNavController().popBackStack()
        }

        submitButton.setOnClickListener {
            sharedViewModel.checksLiveData.value = checksMap

            // Submit the data using Retrofit
            val submissionData = ChecksSubmissionRequest(
                sharedViewModel.checkStartTimestamp.value,
                sharedViewModel.usernameLiveData.value ?: "",
                sharedViewModel.departmentLiveData.value,
                sharedViewModel.lineLiveData.value,
                sharedViewModel.idhNumberLiveData.value,
                sharedViewModel.checkTypeLiveData.value,
                checksMap
            )

            saveSubmissionToLocalDatabase(submissionData)

            myApi.submitChecks(submissionData)
                .enqueue(object : Callback<SubmissionResult> {
                    override fun onResponse(
                        call: Call<SubmissionResult>,
                        response: Response<SubmissionResult>
                    ) {
                        // Handle the response from the server
                        if (response.isSuccessful) {
                            val result = response.body()
                            // Process the result and display it to the user
                            // For example, show a toast with the result message
                            Toast.makeText(
                                requireContext(),
                                "Successfully saved records to API",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            // Handle the error response
                            // For example, show a toast with the error message
                            Toast.makeText(
                                requireContext(),
                                "Failed to submit checks.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<SubmissionResult>, t: Throwable) {
                        // Handle the failure case
                        // For example, show a toast with the error message
                        Toast.makeText(
                            requireContext(),
                            "Failed to submit checks.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

            // Handle submit checks action (e.g., perform checks submission logic)
            // You can define your own logic here based on your app's requirements.
            // Navigate to SubmissionResultFragment and pass the totalFailedChecks as an argument
            val totalFailedChecks = retrieveTotalFailedChecks()
            val action = ChecksFragmentDirections.actionChecksFragmentToSubmissionResultFragment(
                totalFailedChecks.toTypedArray()
            )
            findNavController().navigate(action)
        }
    }

    private fun retrieveTotalFailedChecks(): List<CheckItem> {
        val checksList = mutableListOf<CheckItem>()

        for ((_, checkItems) in checksMap) {
            for (checkItem in checkItems) {
                val value = checkItem.expectedValue
                val result = checkItem.result

                // Check if the user input value does not match the expected value
                if (result != null && result != value) {
                    checksList.add(checkItem)
                }
            }
        }

        return checksList
    }

    private fun loadChecksDataFromApi() {
        myApi.getChecks(
            sharedViewModel.lineLiveData.value!!.id,
            sharedViewModel.checkTypeLiveData.value!!.id,
            sharedViewModel.idhNumberLiveData.value!!.id
        ).enqueue(object : Callback<List<CheckItem>> {
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
            val checkType = checkItem.section

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
        val fileNameWithoutExtension = checkType.substringBeforeLast(".")
        return resources.getIdentifier(
            fileNameWithoutExtension,
            "drawable",
            requireActivity().packageName
        )
    }

    private fun saveSubmissionToLocalDatabase(submissionData: ChecksSubmissionRequest) {
        val localSubmission = CheckSubmissionEntity(
            checkStartTimestamp = submissionData.checkStartTimestamp,
            username = submissionData.username,
            department = submissionData.department,
            line = submissionData.line,
            idhNumber = submissionData.idhNumber,
            checks = submissionData.checks
        )

        coroutineScope.launch {
            appDatabase.checkSubmissionDao().insertSubmission(localSubmission)
        }
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
