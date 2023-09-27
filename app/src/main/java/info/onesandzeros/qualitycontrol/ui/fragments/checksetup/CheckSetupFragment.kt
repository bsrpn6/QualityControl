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
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.CheckType
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.api.models.ProductSpecsResponse
import info.onesandzeros.qualitycontrol.constants.Constants.SITE_ID
import info.onesandzeros.qualitycontrol.databinding.FragmentCheckSetupBinding
import info.onesandzeros.qualitycontrol.databinding.ScrollViewDialogLayoutBinding
import info.onesandzeros.qualitycontrol.ui.displayers.SpecsDetailsDisplayer
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel
import info.onesandzeros.qualitycontrol.utils.ErrorEvent
import info.onesandzeros.qualitycontrol.utils.Event

@AndroidEntryPoint
class CheckSetupFragment : Fragment() {

    private var _binding: FragmentCheckSetupBinding? = null
    private val binding get() = _binding!!

    private lateinit var departmentAdapter: ArrayAdapter<String>
    private lateinit var lineAdapter: ArrayAdapter<String>
    private lateinit var checkTypeAdapter: ArrayAdapter<String>
    private lateinit var idhNumberAdapter: ArrayAdapter<Int>

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val checkSetupViewModel: CheckSetupViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

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

        bindViews()

        // Check if previous values exist
        if (sharedViewModel.departmentLiveData.value != null
            && sharedViewModel.lineLiveData.value != null
            && sharedViewModel.checkTypeLiveData.value != null
            && sharedViewModel.idhNumberLiveData.value != null
        ) {
            // If previous values exist, populate the UI fields and disable user input
            disableInputFields()
        } else {
            // If no previous values, enable user input
            checkSetupViewModel.getDepartments(SITE_ID)
            enableInputFields()
        }

        setupObservers()

        // Update the start checks button click listener
        binding.startChecksButton.setOnClickListener {
            sharedViewModel.departmentLiveData.value?.let {
                sharedViewModel.lineLiveData.value?.let {
                    sharedViewModel.checkTypeLiveData.value?.let {
                        sharedViewModel.idhNumberLiveData.value?.let {
                            sharedViewModel.checkStartTimestamp.value = System.currentTimeMillis()
                            checkSetupViewModel.startChecks()
                        }
                    }
                }
            }
        }


        // Update the logout button click listener
        binding.logoutButton.setOnClickListener {
            checkSetupViewModel.logoutUser()
        }

        binding.viewResultsButton.setOnClickListener {
            findNavController().navigate(R.id.action_checkSetupFragment_to_ViewResultsFragment)
        }

    }

    private fun bindViews() {
        bindDepartmentSpinner()
        bindLineSpinner()
        bindCheckTypeSpinner()
        bindIdhAutoCompleteTextView()
    }

    private fun setupObservers() {
        checkSetupViewModel.uiState.observe(viewLifecycleOwner) { state ->
            handleUIState(state)
        }

        sharedViewModel.idhNumberLiveData.observe(viewLifecycleOwner) {
            updateStartChecksButtonState()
        }

        checkSetupViewModel.navigateTo.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { destinationId ->
                findNavController().navigate(destinationId)
            }
        }
    }

    private fun handleUIState(state: CheckSetupState) {  // Assuming UIState is the data type
        loadDepartmentAdapter(state.departments)
        loadLinesAdapter(state.lines)
        loadCheckTypesAdapter(state.checkTypes)
        loadIDHNumbersAdapter(state.idhNumbers)

        state.productSpecs?.getContentIfNotHandled()?.let { specs ->
            showProductSpecsDialog(specs)
        }

        handleLoadingState(state.isLoading)
        handleStateError(state.error)
    }

    private fun updateStartChecksButtonState() {
        binding.startChecksButton.isEnabled =
            sharedViewModel.departmentLiveData.value != null &&
                    sharedViewModel.lineLiveData.value != null &&
                    sharedViewModel.checkTypeLiveData.value != null &&
                    sharedViewModel.idhNumberLiveData.value != null
    }

    private fun showProductSpecsDialog(productSpecs: ProductSpecsResponse?) {
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
        if (productSpecs != null) {
            displayer.displaySpecsDetails(productSpecs)
        }

        dialog.setOnDismissListener {
            // Clear the productSpecs from uiState
            val currentState = checkSetupViewModel.uiState.value
            if (currentState != null) {
                checkSetupViewModel.uiState.value = currentState.copy(productSpecs = Event(null))
            }
        }

        // Set up the close button using View Binding
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
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
                    if (checkSetupViewModel.uiState.value?.checkTypes?.isNotEmpty() == true) {
                        val selectedCheckType =
                            checkSetupViewModel.uiState.value?.checkTypes?.get(position)
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
                val selectedLine = checkSetupViewModel.uiState.value?.lines?.get(position)
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

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun bindIdhAutoCompleteTextView() {
        binding.idhNumberAutoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selectedIdhNumberValue = parent.getItemAtPosition(position) as Int
                val selectedIDHNumber =
                    checkSetupViewModel.uiState.value?.idhNumbers?.find { it.productId == selectedIdhNumberValue }
                if (sharedViewModel.idhNumberLiveData.value != selectedIDHNumber) {
                    sharedViewModel.idhNumberLiveData.value = selectedIDHNumber
                }

                binding.infoIconImageView.visibility = View.VISIBLE
                binding.infoIconImageView.setOnClickListener {
                    sharedViewModel.idhNumberLiveData.value?.let {
                        checkSetupViewModel.getSpecs(it.id)
                    }
                }
            }
    }


    private fun bindDepartmentSpinner() {
        binding.departmentSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedDepartment =
                        checkSetupViewModel.uiState.value?.departments?.get(position)

                    if (sharedViewModel.departmentLiveData.value != selectedDepartment) {
                        sharedViewModel.departmentLiveData.value = selectedDepartment
                        sharedViewModel.lineLiveData.value = null
                        sharedViewModel.checkTypeLiveData.value = null
                        sharedViewModel.idhNumberLiveData.value = null

                        binding.infoIconImageView.visibility = View.GONE
                        binding.idhNumberAutoCompleteTextView.setText("", false)

                        selectedDepartment?.let {
                            checkSetupViewModel.selectDepartment(it)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }


    private fun loadDepartmentAdapter(departmentList: List<Department>) {
        val localDepartmentList = departmentList.map { it.name }
        departmentAdapter.clear()
        departmentAdapter.addAll(localDepartmentList)

        // Set the spinner selection based on the value from the SharedViewModel
        val selectedDepartment = sharedViewModel.departmentLiveData.value
        if (selectedDepartment != null) {
            binding.departmentSpinner.onItemSelectedListener = null
            val selectedIndex =
                checkSetupViewModel.uiState.value?.departments?.indexOfFirst { it.id == selectedDepartment.id }
            if (selectedIndex != -1) {
                if (selectedIndex != null) {
                    binding.departmentSpinner.setSelection(selectedIndex)
                }
            }
            bindDepartmentSpinner()
        }
    }

    private fun loadLinesAdapter(linesList: List<Line>) {
        val localLineList = linesList.map { it.name }
        lineAdapter.clear()
        lineAdapter.addAll(localLineList)

        // Set the spinner selection based on the value from the SharedViewModel
        val selectedLine = sharedViewModel.lineLiveData.value
        if (selectedLine != null) {
            binding.lineSpinner.onItemSelectedListener = null
            val selectedIndex =
                checkSetupViewModel.uiState.value?.lines?.indexOfFirst { it.id == selectedLine.id }
            if (selectedIndex != -1) {
                if (selectedIndex != null) {
                    binding.lineSpinner.setSelection(selectedIndex)
                }
            }
            bindLineSpinner()
        } else {
            binding.lineSpinner.setSelection(0)
        }
    }

    private fun loadCheckTypesAdapter(checkTypeList: List<CheckType>) {
        val localCheckTypes = checkTypeList.map { it.displayName }
        checkTypeAdapter.clear()
        checkTypeAdapter.addAll(localCheckTypes)

        // Set the spinner selection based on the value from the SharedViewModel
        val selectedCheckType = sharedViewModel.checkTypeLiveData.value
        if (selectedCheckType != null) {
            val selectedIndex =
                checkSetupViewModel.uiState.value?.checkTypes?.indexOfFirst { it.id == selectedCheckType.id }
            if (selectedIndex != -1) {
                if (selectedIndex != null) {
                    binding.checkTypeSpinner.setSelection(selectedIndex)
                }
            }
        }
    }

    private fun loadIDHNumbersAdapter(idhNumbersList: List<IDHNumbers>) {
        val localIdhNumbers = idhNumbersList.map { it.productId }
        idhNumberAdapter.clear()
        idhNumberAdapter.addAll(localIdhNumbers)

        // Set the spinner selection based on the value from the SharedViewModel
        val selectedIDHNumber = sharedViewModel.idhNumberLiveData.value
        if (selectedIDHNumber != null) {
            val selectedIndex =
                checkSetupViewModel.uiState.value?.idhNumbers?.indexOfFirst { it.id == selectedIDHNumber.id }
            if (selectedIndex != -1) {
                binding.idhNumberAutoCompleteTextView.setText(selectedIDHNumber.productId.toString())
            }
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
            sharedViewModel.idhNumberLiveData.value?.let {
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

    private fun handleLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingProgressBar.visibility = View.VISIBLE
        } else {
            binding.loadingProgressBar.visibility = View.GONE
        }
    }

    private fun handleStateError(errorEvent: ErrorEvent?) {
        errorEvent?.let {
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        private const val TAG = "CheckSetupFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}