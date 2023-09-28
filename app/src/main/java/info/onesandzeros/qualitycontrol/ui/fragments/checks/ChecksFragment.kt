package info.onesandzeros.qualitycontrol.ui.fragments.checks

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.ChecksSubmissionRequest
import info.onesandzeros.qualitycontrol.databinding.CustomTabLayoutBinding
import info.onesandzeros.qualitycontrol.databinding.FragmentChecksBinding
import info.onesandzeros.qualitycontrol.databinding.PopupMenuBinding
import info.onesandzeros.qualitycontrol.ui.adapters.ImageReelAdapter
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

    private val reelAdapter = ImageReelAdapter { uriToRemove ->
        getCurrentSection().let { sharedViewModel.removeImageUri(it, uriToRemove) }
    }

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val checksViewModel: ChecksViewModel by viewModels()

    private val iconResourceMap = mapOf(
        "product_bottle" to R.drawable.product_bottle,
        "product_case" to R.drawable.product_case,
        "product_multi_pack" to R.drawable.product_multi_pack,
        "product_pallet" to R.drawable.product_pallet
        // add more mappings as needed
    )

    override fun onResume() {
        super.onResume()

        checksViewModel.uiState.value?.let {
            binding.viewPager.setCurrentItem(
                it.currentTabPosition, false
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChecksBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reelRecyclerView = binding.reelRecyclerView
        reelRecyclerView.adapter = reelAdapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val currentSection = getCurrentSection()
                updateReelForSection(currentSection)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Initial update for the first section when fragment is created
        updateReelForSection(getCurrentSection())

        sharedViewModel.photosLiveData.observe(viewLifecycleOwner) { photosMap ->
            val currentSection = getCurrentSection()
            val currentPhotos = photosMap?.get(currentSection) ?: emptyList()
            reelAdapter.setImages(currentPhotos)
        }


        // Observe changes in the ViewModel's LiveData
        checksViewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state != null) {
                handleState(state)
            }
        }

        // Check if initial load has been completed, if not then fetch
        if (checksViewModel.uiState.value?.initialLoadComplete == false) {
            loadChecksData()
        }

        // Set click listeners for the buttons
        binding.exitButton.setOnClickListener {
            // Handle exit checks action (e.g., navigate back to previous fragment)
            //TODO - use checksViewModel for nav and to clear sharedViewModel
            findNavController().popBackStack()
        }

        binding.fabAdd.setOnClickListener {
            showPopupMenu(binding.fabAdd, getCurrentSection())
        }

        binding.submitButton.setOnClickListener {
            sharedViewModel.checksLiveData.value = checksViewModel.uiState.value?.checksMap

            val submissionData = checksViewModel.uiState.value?.checksMap?.let {
                ChecksSubmissionRequest(
                    sharedViewModel.checkStartTimestamp.value,
                    sharedViewModel.usernameLiveData.value ?: "",
                    sharedViewModel.departmentLiveData.value,
                    sharedViewModel.lineLiveData.value,
                    sharedViewModel.idhNumberLiveData.value,
                    sharedViewModel.checkTypeLiveData.value,
                    it,
                    sharedViewModel.photosLiveData.value
                )
            }

            if (submissionData != null) {
                val contentResolver = requireContext().contentResolver
                checksViewModel.submitChecks(submissionData, contentResolver)
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
            val contextView = binding.root
            Snackbar.make(contextView, state.error, Snackbar.LENGTH_LONG).show()
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

        val currentTabPosition = viewPager.currentItem

        val fragmentList = mutableListOf<Fragment>()
        val tabTitleList = mutableListOf<String>()

        val localChecksMap = checksViewModel.uiState.value?.checksMap

        if (localChecksMap != null) {
            for ((checkType, checkItems) in localChecksMap) {
                val checkTypeFragment =
                    CheckTypeFragment.newInstance(checkItems, activityResultRegistry)
                fragmentList.add(checkTypeFragment)
                tabTitleList.add(checkType)
            }
        }

        val pagerAdapter = ChecksPagerAdapter(fragmentList, this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val customTabBinding =
                CustomTabLayoutBinding.inflate(LayoutInflater.from(tabLayout.context))
            val checkType = tabTitleList[position]
            customTabBinding.tabIcon.setImageResource(getTabIconResourceId(checkType))
            customTabBinding.tabText.text = StringUtils.formatTabText(checkType)
            tab.customView = customTabBinding.root
        }.attach()

        viewPager.setCurrentItem(currentTabPosition, false)
    }


    // Helper method to get the tab icon resource based on the check type
    private fun getTabIconResourceId(checkType: String): Int {
        val fileNameWithoutExtension = checkType.substringBeforeLast(".")
        return iconResourceMap[fileNameWithoutExtension] ?: R.drawable.product_default
    }

    private fun showPopupMenu(anchorView: View, currentSection: String) {
        val popupBinding = PopupMenuBinding.inflate(LayoutInflater.from(context))

        val popupWindow = PopupWindow(
            popupBinding.root,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true // This makes the popup window focusable
        )

        val isCommentAlreadyAdded = checksViewModel.isCommentAddedInSection(currentSection)
        popupBinding.btnAddComment.visibility =
            if (isCommentAlreadyAdded) View.GONE else View.VISIBLE

        popupBinding.btnAddComment.setOnClickListener {
            checksViewModel.addComment(currentSection)
            popupWindow.dismiss()
        }

        popupBinding.btnAttachPhoto.setOnClickListener {
            checksViewModel.uiState.value?.currentTabPosition = binding.viewPager.currentItem
            val action =
                ChecksFragmentDirections.actionChecksFragmentToCameraPreviewFragment(currentSection)
            findNavController().navigate(action)
            popupWindow.dismiss()
        }

        // Measure the popup to get the correct width and height
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        popupWindow.contentView.measure(widthMeasureSpec, heightMeasureSpec)

        // Calculate the coordinates to make the popup appear above the FAB and centered
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        val measuredWidth = popupWindow.contentView.measuredWidth
        val measuredHeight = popupWindow.contentView.measuredHeight

        val xPos = location[0] + anchorView.width / 2 - measuredWidth / 2
        val yPos = location[1] - measuredHeight

        // Show the popup window
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xPos, yPos)
    }


    private inner class ChecksPagerAdapter(
        private val fragmentList: List<Fragment>, fragment: Fragment
    ) : FragmentStateAdapter(fragment.childFragmentManager, fragment.viewLifecycleOwner.lifecycle) {
        override fun getItemCount(): Int = fragmentList.size

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }

    private fun getCurrentSection(): String {
        val currentPosition = binding.viewPager.currentItem
        val sectionsList = checksViewModel.uiState.value?.checksMap?.keys?.toList() ?: emptyList()
        if (sectionsList.isNotEmpty() && currentPosition in sectionsList.indices) {
            return sectionsList[currentPosition]
        }
        return ""
    }

    private fun updateReelForSection(section: String) {
        val photosForSection = sharedViewModel.getPhotosForSection(section)
        reelAdapter.setImages(photosForSection)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
