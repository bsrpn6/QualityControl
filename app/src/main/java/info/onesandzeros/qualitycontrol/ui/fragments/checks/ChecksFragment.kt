package info.onesandzeros.qualitycontrol.ui.fragments.checks

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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.ChecksSubmissionRequest
import info.onesandzeros.qualitycontrol.databinding.FragmentChecksBinding
import info.onesandzeros.qualitycontrol.ui.fragments.CheckTypeFragment
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel
import info.onesandzeros.qualitycontrol.utils.StringUtils
import javax.inject.Inject

@AndroidEntryPoint
class ChecksFragment : Fragment(R.layout.fragment_checks) {
    private var _binding: FragmentChecksBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var activityResultRegistry: ActivityResultRegistry

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val checksViewModel: ChecksViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChecksBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe changes in the ViewModel's LiveData
        checksViewModel.uiState.observe(viewLifecycleOwner) { state ->
            handleState(state)
        }

        // Check if initial load has been completed, if not then fetch
        if (checksViewModel.uiState.value?.initialLoadComplete == false) {
            loadChecksData()
        }

        // Find the buttons by their IDs
        val exitButton = binding.exitButton
        val submitButton = binding.submitButton

        // Set click listeners for the buttons
        exitButton.setOnClickListener {
            // Handle exit checks action (e.g., navigate back to previous fragment)
            findNavController().popBackStack()
        }

        submitButton.setOnClickListener {
            sharedViewModel.checksLiveData.value = checksViewModel.uiState.value?.checksMap

            val submissionData = checksViewModel.uiState.value?.checksMap?.let { it ->
                ChecksSubmissionRequest(
                    sharedViewModel.checkStartTimestamp.value,
                    sharedViewModel.usernameLiveData.value ?: "",
                    sharedViewModel.departmentLiveData.value,
                    sharedViewModel.lineLiveData.value,
                    sharedViewModel.idhNumberLiveData.value,
                    sharedViewModel.checkTypeLiveData.value,
                    it
                )
            }

            if (submissionData != null) {
                checksViewModel.saveSubmissionToLocalDatabase(submissionData)
            }

            val totalFailedChecks = retrieveTotalFailedChecks()
            val action = ChecksFragmentDirections.actionChecksFragmentToSubmissionResultFragment(
                totalFailedChecks.toTypedArray()
            )
            findNavController().navigate(action)
        }
    }

    private fun handleState(state: ChecksState) {
        // Update the UI based on the state
        if (state.isLoading) {
            binding.loadingProgressBar.visibility = View.VISIBLE
        } else if (state.error != null) {
            binding.loadingProgressBar.visibility = View.GONE
            Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
        } else {
            binding.loadingProgressBar.visibility = View.GONE
            setupViewPagerAndTabs()
        }
    }

    private fun loadChecksData() {
        val lineId = sharedViewModel.lineLiveData.value!!.id
        val checkTypeId = sharedViewModel.checkTypeLiveData.value!!.id
        val idhNumberId = sharedViewModel.idhNumberLiveData.value!!.id
        checksViewModel.getChecks(lineId, checkTypeId, idhNumberId)
    }

    private fun retrieveTotalFailedChecks(): List<CheckItem> {
        val checksList = mutableListOf<CheckItem>()

        val localChecksMap = checksViewModel.uiState.value?.checksMap
        if (localChecksMap != null) {
            for ((_, checkItems) in localChecksMap) {
                for (checkItem in checkItems) {
                    val value = checkItem.expectedValue
                    val result = checkItem.result

                    // Check if the user input value does not match the expected value
                    if (result != null && result != value) {
                        checksList.add(checkItem)
                    }
                }
            }
        }

        return checksList
    }

    private fun setupViewPagerAndTabs() {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        val fragmentList = mutableListOf<Fragment>()
        val tabTitleList = mutableListOf<String>()

        val localChecksMap = checksViewModel.uiState.value?.checksMap

        if (localChecksMap != null) {
            for ((checkType, checkItems) in localChecksMap) {
                // Create a new fragment for each check type
                val checkTypeFragment =
                    CheckTypeFragment.newInstance(checkItems, activityResultRegistry)

                // Add the fragment to the list
                fragmentList.add(checkTypeFragment)

                // Add the tab title to the list
                tabTitleList.add(checkType)
            }
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
