package info.onesandzeros.qualitycontrol.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.databinding.FragmentViewResultsBinding
import info.onesandzeros.qualitycontrol.databinding.TabularRowLayoutBinding
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.viewmodels.ViewResultsViewModel
import info.onesandzeros.qualitycontrol.utils.StringUtils
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class ViewResultsFragment : Fragment() {

    private lateinit var binding: FragmentViewResultsBinding
    private val viewModel: ViewResultsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewResultsBinding.inflate(inflater, container, false)

        // Set up the line selection dropdown
        val adapter = object : ArrayAdapter<Line>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf<Line>()
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val line = getItem(position)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.text = line?.name // Customize the text displayed in the spinner
                return view
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val line = getItem(position)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.text = line?.name // Customize the text displayed in the dropdown list
                return view
            }
        }

        binding.lineDropdown.adapter = adapter

        binding.lineDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                // When a line is selected, fetch and display the check submissions for that line
                val selectedLine = adapter.getItem(position)
                selectedLine?.let {
                    viewModel.setLineId(it)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle when nothing is selected (optional)
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp() // Navigate back to the previous screen
        }

        // Observe the LiveData for lines and update the adapter
        viewModel.lines.observe(viewLifecycleOwner, Observer { lines ->
            adapter.clear()
            adapter.addAll(lines)
        })

        // Observe the LiveData for check submissions and update the tabular layout
        viewModel.checkSubmissions.observe(viewLifecycleOwner, Observer { checkSubmissionsForLine ->
            // Clear previous tabular data
            binding.tabularLayout.removeAllViews()

            if (checkSubmissionsForLine.isNotEmpty()) {
                val rowBinding = TabularRowLayoutBinding.inflate(layoutInflater)
                rowBinding.dateTextView.text = "DATE"
                rowBinding.usernameTextView.text = "USERNAME"
                rowBinding.idhNumberTextView.text = "IDH NUMBER"

                rowBinding.failedCheckCountTextView.text = "# FAILED CHECKS"

                binding.tabularLayout.addView(rowBinding.root)
            }

            // Loop through check submissions and populate the tabular layout
            for (checkSubmission in checkSubmissionsForLine) {
                val rowBinding = TabularRowLayoutBinding.inflate(layoutInflater)

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val formattedDate: String = sdf.format(Date(checkSubmission.checkStartTimestamp!!))

                rowBinding.dateTextView.text = formattedDate
                rowBinding.usernameTextView.text =
                    StringUtils.parseUsername(checkSubmission.username)
                rowBinding.idhNumberTextView.text =
                    checkSubmission.idhNumber?.idhNumber.toString()

                rowBinding.failedCheckCountTextView.text =
                    calculateFailedChecks(checkSubmission.checks).toString()

                binding.tabularLayout.addView(rowBinding.root)
            }
        })

        // Load lines from the database using the ViewModel
        viewModel.loadLinesByDepartmentId(1) // Replace 1 with the desired department ID

        return binding.root
    }

    private fun calculateFailedChecks(checks: Map<String, List<CheckItem>>): Int {
        var failedCheckCount = 0

        for ((_, checkItems) in checks) {
            for (check in checkItems) {
                if (check.result != null && check.value != check.result) {
                    failedCheckCount++
                }
            }
        }

        return failedCheckCount
    }

}

