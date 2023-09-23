package info.onesandzeros.qualitycontrol.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.databinding.FragmentViewResultsBinding
import info.onesandzeros.qualitycontrol.ui.adapters.ResultsAdapter
import info.onesandzeros.qualitycontrol.ui.viewmodels.ViewResultsViewModel
import info.onesandzeros.qualitycontrol.utils.FailedCheckDetailsDisplayer

@AndroidEntryPoint
class ViewResultsFragment : Fragment() {

    private lateinit var binding: FragmentViewResultsBinding
    private val viewModel: ViewResultsViewModel by viewModels()
    private lateinit var resultsAdapter: ResultsAdapter


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

        // Initialize RecyclerView and Adapter
        resultsAdapter = ResultsAdapter(emptyList()) { checkResult ->
            // Extract the failed checks for this item
            val failedChecks = retrieveTotalFailedChecks(checkResult.checks)

            // Create a dialog or navigate to a new fragment
            val dialog = Dialog(requireContext())

            // Inflate the layout containing a ViewGroup (e.g., LinearLayout) for the details
            val detailsLayout = LayoutInflater.from(requireContext())
                .inflate(R.layout.scroll_view_dialog_layout, null)

            // Set up the dialog's content view
            dialog.setContentView(detailsLayout)

            // Adjust the dialog's width and height
            val window = dialog.window
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )

            // Create a FailedCheckDetailsDisplayer and display the details
            val displayer = FailedCheckDetailsDisplayer(
                requireContext(), detailsLayout.findViewById(
                    R.id.detailsLayout
                )
            )
            displayer.displayFailedCheckDetails(failedChecks)

            // Set up the close button
            detailsLayout.findViewById<Button>(R.id.closeButton).setOnClickListener {
                dialog.dismiss()
            }

            // Show the dialog or navigate to the fragment
            dialog.show()
        }
        binding.resultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.resultsRecyclerView.adapter = resultsAdapter

        // Observe check submissions and update RecyclerView
        viewModel.checkSubmissions.observe(viewLifecycleOwner, Observer { checkSubmissions ->
            // Update the RecyclerView adapter with the filtered check submissions
            resultsAdapter.results = checkSubmissions
            resultsAdapter.notifyDataSetChanged()
        })

        // Load lines from the database using the ViewModel
        viewModel.loadLinesByDepartmentId(1) // Replace 1 with the desired department ID

        return binding.root
    }

    private fun retrieveTotalFailedChecks(checksMap: Map<String, List<CheckItem>>): Array<CheckItem> {
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

        return checksList.toTypedArray()
    }

}

