package info.onesandzeros.qualitycontrol

import ChecksAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.databinding.FragmentChecksBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class ChecksFragment : Fragment(R.layout.fragment_checks) {
    private lateinit var binding: FragmentChecksBinding
    private lateinit var adapter: ChecksAdapter

    @Inject
    lateinit var activityResultRegistry: ActivityResultRegistry // Inject the ActivityResultRegistry

    private lateinit var checksMap: Map<String, List<CheckItem>>

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
        checksMap = loadChecksDataFromJson()

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
            tabText.text = checkType // Set the text for each tab
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

    private fun loadChecksDataFromJson(): Map<String, List<CheckItem>> {
        val inputStream: InputStream = resources.openRawResource(R.raw.checks_data)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val jsonArray: JSONArray = jsonObject.optJSONArray("checks") ?: return emptyMap()

        val checksMap = mutableMapOf<String, MutableList<CheckItem>>()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val checkId = item.optInt("checkId")
            val checkType = item.optString("checktype") // Read the checktype field from JSON
            val type = item.optString("type")
            val title = item.optString("title")
            val description = item.optString("description")
            val value =
                item.opt("value") // This will be a dynamic type (Boolean, Integer, String, etc.)

            val checkItem = CheckItem(checkId, checkType, type, title, description, value)

            // Add the checkItem to the appropriate category in the checksMap
            if (checksMap.containsKey(checkType)) {
                checksMap[checkType]?.add(checkItem)
            } else {
                checksMap[checkType] = mutableListOf(checkItem)
            }
        }

        return checksMap
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
