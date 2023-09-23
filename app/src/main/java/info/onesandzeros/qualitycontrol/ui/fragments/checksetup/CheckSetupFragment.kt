package info.onesandzeros.qualitycontrol.ui.fragments.checksetup

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.CheckType
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.constants.Constants.SITE_ID
import info.onesandzeros.qualitycontrol.databinding.FragmentCheckSetupBinding
import info.onesandzeros.qualitycontrol.databinding.ScrollViewDialogLayoutBinding
import info.onesandzeros.qualitycontrol.ui.viewmodels.CheckSetupViewModel
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel
import info.onesandzeros.qualitycontrol.utils.SpecsDetailsDisplayer
import javax.inject.Inject

@AndroidEntryPoint
class CheckSetupFragment : Fragment() {

    private var _binding: FragmentCheckSetupBinding? = null
    private val binding get() = _binding!!

    private val departments = mutableListOf<Department>()
    private val lines = mutableListOf<Line>()
    private val checkTypes = mutableListOf<CheckType>()
    private val idhNumbers = mutableListOf<IDHNumbers>()

    private lateinit var departmentAdapter: ArrayAdapter<String>
    private lateinit var lineAdapter: ArrayAdapter<String>
    private lateinit var checkTypeAdapter: ArrayAdapter<String>
    private lateinit var idhNumberAdapter: ArrayAdapter<Int>

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val checkSetupViewModel: CheckSetupViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        departmentAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item)
        lineAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item)
        idhNumberAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf<Int>()
        )

        binding.departmentSpinner.adapter = departmentAdapter
        binding.lineSpinner.adapter = lineAdapter
        binding.idhNumberAutoCompleteTextView.setAdapter(idhNumberAdapter)
        binding.idhNumberAutoCompleteTextView.threshold =
            1 // Set minimum number of characters to trigger suggestions

        bindDepartmentSpinner()
        bindLineSpinner()
        bindCheckTypeSpinner()
        bindIdhAutoCompleteTextView()

        // Check if previous values exist
        if (sharedViewModel.departmentLiveData.value != null
            && sharedViewModel.lineLiveData.value != null
            && sharedViewModel.checkTypeLiveData.value != null
            && sharedViewModel.idhNumberLiveData.value != null
        ) {
            // If previous values exist, populate the UI fields and disable user input
            populateFields()
            disableInputFields()
        } else {
            // If no previous values, enable user input
            checkSetupViewModel.getDepartments(SITE_ID)
            enableInputFields()
        }

        setupObservers()

        binding.startChecksButton.setOnClickListener {
            // Check if all three fields have valid selections
            if (sharedViewModel.departmentLiveData.value != null && sharedViewModel.lineLiveData.value != null && sharedViewModel.checkTypeLiveData.value != null && sharedViewModel.idhNumberLiveData.value != null) {

                sharedViewModel.checkStartTimestamp.value = System.currentTimeMillis()
                // Proceed to ChecksFragment when the button is clicked
                findNavController().navigate(R.id.action_checkSetupFragment_to_checksFragment)
            } else {
                // Display a toast indicating invalid selections
                Toast.makeText(
                    requireContext(), "Please select valid values for all fields", Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.viewResultsButton.setOnClickListener {
            findNavController().navigate(R.id.action_checkSetupFragment_to_ViewResultsFragment)
        }

        binding.logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun setupObservers() {
        checkSetupViewModel.departmentListLiveData.observe(viewLifecycleOwner) { departments ->
            // Update UI with the fetched departments
            departments?.let {
                loadDepartmentAdapter(it)


                // Fetch lines for the first department if there are departments
                if (it.isNotEmpty()) {
                    val selectedDepartment = it[0]
                    checkSetupViewModel.getLinesForDepartment(selectedDepartment.id)
                }
            }
        }

        checkSetupViewModel.lineListLiveData.observe(viewLifecycleOwner) { lines ->
            // Update UI with the fetched lines
            lines?.let {
                loadLinesAdapter(it)
            }

            // Set the first line in the sharedViewModel and trigger ViewModel to fetch check types and IDH numbers for it
            lines?.firstOrNull()?.let { selectedLine ->
                sharedViewModel.lineLiveData.value = selectedLine
                checkSetupViewModel.getCheckTypesForLine(selectedLine.id)  // Trigger ViewModel
                checkSetupViewModel.getProductsForLine(selectedLine.id)  // Trigger ViewModel
            }
        }

        checkSetupViewModel.checkTypeLiveData.observe(viewLifecycleOwner) { checkTypes ->
            // Update UI with the fetched check types
            checkTypes?.let {
                loadCheckTypesAdapter(it)
            }
        }

        checkSetupViewModel.idhNumberLiveData.observe(viewLifecycleOwner) { idhNumbers ->
            // Update UI with the fetched IDH numbers
            idhNumbers?.let {
                loadIDHNumbersAdapter(it)
            }
        }

        checkSetupViewModel.errorMessage.observe(viewLifecycleOwner) { errorEvent ->
            Toast.makeText(requireContext(), errorEvent.message, Toast.LENGTH_SHORT).show()
        }


        checkSetupViewModel.productSpecsLiveData.observe(viewLifecycleOwner) { specsResponses ->
            specsResponses?.let { validSpecsResponses ->
                sharedViewModel.idhNumberLiveData.value?.let { idhNumber ->

                    // Create the dialog
                    val dialog = Dialog(requireContext())

                    // Inflate the layout using View Binding
                    val binding =
                        ScrollViewDialogLayoutBinding.inflate(LayoutInflater.from(requireContext()))

                    // Set up the dialog's content view
                    dialog.setContentView(binding.root)

                    // Adjust the dialog's width and height
                    val window = dialog.window
                    window?.setLayout(
                        (resources.displayMetrics.widthPixels * 0.9).toInt(),
                        WindowManager.LayoutParams.WRAP_CONTENT
                    )

                    // Create a SpecsDetailsDisplayer and display the details
                    val displayer = SpecsDetailsDisplayer(
                        requireContext(), binding.detailsLayout
                    )
                    displayer.displaySpecsDetails(
                        validSpecsResponses,
                        idhNumber.productId,
                        idhNumber.description
                    )

                    dialog.setOnDismissListener {
                        checkSetupViewModel.productSpecsLiveData.value = null
                    }

                    // Set up the close button using View Binding
                    binding.closeButton.setOnClickListener {
                        dialog.dismiss()
                    }

                    // Show the dialog
                    dialog.show()
                }
            }
        }

    }

    private fun bindCheckTypeSpinner() {
        checkTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item)
        checkTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.checkTypeSpinner.adapter = checkTypeAdapter

        binding.checkTypeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (checkTypes.isNotEmpty()) {
                        val selectedCheckType = checkTypes[position]
                        sharedViewModel.checkTypeLiveData.value = selectedCheckType
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }


    private fun bindLineSpinner() {
        binding.lineSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (lines.isNotEmpty()) {
                    val selectedLine = lines[position]
                    if (sharedViewModel.lineLiveData.value != selectedLine) {
                        sharedViewModel.lineLiveData.value = selectedLine
                        sharedViewModel.idhNumberLiveData.value = null
                        sharedViewModel.checkTypeLiveData.value = null
                        binding.infoIconImageView.visibility = View.GONE
                        binding.idhNumberAutoCompleteTextView.setText("", false)
                        sharedViewModel.lineLiveData.value?.id?.let {
                            checkSetupViewModel.getCheckTypesForLine(it)
                            checkSetupViewModel.getProductsForLine(it)
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun bindIdhAutoCompleteTextView() {
        binding.idhNumberAutoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selectedIdhNumberValue = parent.getItemAtPosition(position) as Int
                val selectedIDHNumber = idhNumbers.find { it.productId == selectedIdhNumberValue }
                if (sharedViewModel.idhNumberLiveData.value != selectedIDHNumber) {
                    sharedViewModel.idhNumberLiveData.value = selectedIDHNumber
                }

                binding.infoIconImageView.visibility = View.VISIBLE
                binding.infoIconImageView.setOnClickListener {
                    sharedViewModel.idhNumberLiveData.value?.let { it ->
                        checkSetupViewModel.getSpecs(it.id)
                    }
                }
            }
    }


    private fun bindDepartmentSpinner() {
        binding.departmentSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val selectedDepartment =
                        checkSetupViewModel.departmentListLiveData.value?.get(position)
                    if (sharedViewModel.departmentLiveData.value != selectedDepartment) {
                        sharedViewModel.departmentLiveData.value = selectedDepartment
                        sharedViewModel.lineLiveData.value = null
                        sharedViewModel.checkTypeLiveData.value = null
                        sharedViewModel.idhNumberLiveData.value = null
                        binding.infoIconImageView.visibility = View.GONE
                        binding.idhNumberAutoCompleteTextView.setText("", false)
                        sharedViewModel.departmentLiveData.value?.id?.let {
                            checkSetupViewModel.getLinesForDepartment(it)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun loadDepartmentAdapter(departmentList: List<Department>) {
        departments.clear()
        departments.addAll(departmentList)
        val localDepartmentList = departments.map { it.name }
        departmentAdapter.clear()
        departmentAdapter.addAll(localDepartmentList)

        // Set the spinner selection based on the value from the SharedViewModel
        val selectedDepartment = sharedViewModel.departmentLiveData.value
        if (selectedDepartment != null) {
            binding.departmentSpinner.onItemSelectedListener = null
            val selectedIndex = departments.indexOfFirst { it.id == selectedDepartment.id }
            if (selectedIndex != -1) {
                binding.departmentSpinner.setSelection(selectedIndex)
            }
            bindDepartmentSpinner()
        }
    }

    private fun loadLinesAdapter(linesList: List<Line>) {
        lines.clear()
        lines.addAll(linesList)
        val localLineList = lines.map { it.name }
        lineAdapter.clear()
        lineAdapter.addAll(localLineList)

        // Set the spinner selection based on the value from the SharedViewModel
        val selectedLine = sharedViewModel.lineLiveData.value
        if (selectedLine != null) {
            binding.lineSpinner.onItemSelectedListener = null
            val selectedIndex = lines.indexOfFirst { it.id == selectedLine.id }
            if (selectedIndex != -1) {
                sharedViewModel.lineLiveData.value = lines[selectedIndex]
                binding.lineSpinner.setSelection(selectedIndex)
            }
            bindLineSpinner()
        } else {
            binding.lineSpinner.setSelection(0)
        }
    }

    private fun loadCheckTypesAdapter(checkTypeList: List<CheckType>) {
        checkTypes.clear()
        checkTypes.addAll(checkTypeList)
        val localCheckTypes = checkTypes.map { it.displayName }
        checkTypeAdapter.clear()
        checkTypeAdapter.addAll(localCheckTypes)

        // Set the spinner selection based on the value from the SharedViewModel
        val selectedCheckType = sharedViewModel.checkTypeLiveData.value
        if (selectedCheckType != null) {
            val selectedIndex =
                checkTypes.indexOfFirst { it.id == selectedCheckType.id }
            if (selectedIndex != -1) {
                binding.checkTypeSpinner.setSelection(selectedIndex)
            }
        }
    }

    private fun loadIDHNumbersAdapter(idhNumbersList: List<IDHNumbers>) {
        idhNumbers.clear()
        idhNumbers.addAll(idhNumbersList)
        val localIdhNumbers = idhNumbers.map { it.productId }
        idhNumberAdapter.clear()
        idhNumberAdapter.addAll(localIdhNumbers)

        // Set the spinner selection based on the value from the SharedViewModel
        val selectedIDHNumber = sharedViewModel.idhNumberLiveData.value
        if (selectedIDHNumber != null) {
            val selectedIndex =
                idhNumbers.indexOfFirst { it.id == selectedIDHNumber.id }
            if (selectedIndex != -1) {
                binding.idhNumberAutoCompleteTextView.setText(selectedIDHNumber.productId.toString())
            }
        }
    }

    private fun populateFields() {
        checkSetupViewModel.departmentListLiveData.value?.let { departments ->
            loadDepartmentAdapter(departments)
        }

        checkSetupViewModel.lineListLiveData.value?.let { lines ->
            loadLinesAdapter(lines)
        }

        checkSetupViewModel.checkTypeLiveData.value?.let { checkTypes ->
            loadCheckTypesAdapter(checkTypes)
        }

        checkSetupViewModel.idhNumberLiveData.value?.let { idhNumbers ->
            loadIDHNumbersAdapter(idhNumbers)
        }
    }

    private fun disableInputFields() {
        Log.d(TAG, "disableInputFields() called")
        binding.departmentSpinner.isEnabled = false
        binding.lineSpinner.isEnabled = false
        binding.checkTypeSpinner.isEnabled = false
        binding.idhNumberAutoCompleteTextView.isEnabled = false
        binding.infoIconImageView.visibility = View.VISIBLE
        Log.d(TAG, "Setting onClickListener for infoIconImageView")
        binding.infoIconImageView.setOnClickListener {
            Log.d(TAG, "infoIconImageView clicked")
            sharedViewModel.idhNumberLiveData.value?.let { it ->
                checkSetupViewModel.getSpecs(
                    it.id
                )
            }
        }
        binding.startNewCheckButton.visibility = View.VISIBLE
        binding.startNewCheckButton.setOnClickListener {
            enableInputFields()
        }
    }

    private fun enableInputFields() {
        binding.departmentSpinner.isEnabled = true
        binding.lineSpinner.isEnabled = true
        binding.checkTypeSpinner.isEnabled = true
        binding.idhNumberAutoCompleteTextView.isEnabled = true
        binding.startNewCheckButton.visibility = View.GONE
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        Toast.makeText(
            requireContext(), "Logout Successful", Toast.LENGTH_LONG
        ).show()
        findNavController().navigate(R.id.action_checkSetupFragment_to_loginFragment)
    }

    companion object {
        private const val TAG = "CheckSetupFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}