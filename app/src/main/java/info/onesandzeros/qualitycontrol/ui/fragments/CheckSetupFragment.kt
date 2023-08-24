package info.onesandzeros.qualitycontrol.ui.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.MyApi
import info.onesandzeros.qualitycontrol.api.models.CheckType
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.api.models.ProductSpecsResponse
import info.onesandzeros.qualitycontrol.constants.Constants.SITE_ID
import info.onesandzeros.qualitycontrol.data.AppDatabase
import info.onesandzeros.qualitycontrol.data.models.CheckTypeEntity
import info.onesandzeros.qualitycontrol.data.models.DepartmentEntity
import info.onesandzeros.qualitycontrol.data.models.IDHNumbersEntity
import info.onesandzeros.qualitycontrol.data.models.LineEntity
import info.onesandzeros.qualitycontrol.databinding.FragmentCheckSetupBinding
import info.onesandzeros.qualitycontrol.ui.viewmodels.CheckSetupViewModel
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel
import info.onesandzeros.qualitycontrol.utils.SpecsDetailsDisplayer
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class CheckSetupFragment : Fragment() {

    private var _binding: FragmentCheckSetupBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var appDatabase: AppDatabase

    private val departments = mutableListOf<Department>()
    private val lines = mutableListOf<Line>()
    private val checkTypes = mutableListOf<CheckType>()
    private val idhNumbers = mutableListOf<IDHNumbers>()

    private lateinit var departmentAdapter: ArrayAdapter<String>
    private lateinit var lineAdapter: ArrayAdapter<String>
    private lateinit var checkTypeAdapter: ArrayAdapter<String>
    private lateinit var idhNumberAdapter: ArrayAdapter<Int>

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var checkSetupViewModel: CheckSetupViewModel

    private lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var myApi: MyApi

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        checkSetupViewModel = ViewModelProvider(requireActivity())[CheckSetupViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

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
            fetchDepartmentsFromApi()
            enableInputFields()
        }


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

    private fun bindCheckTypeSpinner() {
        checkTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item)
        checkTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.checkTypeSpinner.adapter = checkTypeAdapter

        binding.checkTypeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
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
                        sharedViewModel.lineLiveData.value = lines[position]
                        sharedViewModel.idhNumberLiveData.value = null
                        sharedViewModel.checkTypeLiveData.value = null
                        binding.infoIconImageView.visibility = View.GONE
                        binding.idhNumberAutoCompleteTextView.setText("", false)
                        sharedViewModel.lineLiveData.value?.id?.let {
                            fetchCheckTypesForLineFromApi(it)
                            fetchIDHNumbersForLineFromApi(it)
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
                    fetchSpecsAndShowDialog()
                }
            }
    }

    private fun fetchSpecsAndShowDialog() {
        myApi.getSpecs(sharedViewModel.idhNumberLiveData.value?.id)
            .enqueue(object : Callback<ProductSpecsResponse> {
                override fun onResponse(
                    call: Call<ProductSpecsResponse>,
                    response: Response<ProductSpecsResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { specsResponses ->
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
                            val displayer = SpecsDetailsDisplayer(
                                requireContext(), detailsLayout.findViewById(
                                    R.id.detailsLayout
                                )
                            )
                            displayer.displaySpecsDetails(
                                specsResponses,
                                sharedViewModel.idhNumberLiveData.value!!.productId,
                                sharedViewModel.idhNumberLiveData.value!!.description
                            )

                            // Set up the close button
                            detailsLayout.findViewById<Button>(R.id.closeButton)
                                .setOnClickListener {
                                    dialog.dismiss()
                                }

                            // Show the dialog or navigate to the fragment
                            dialog.show()
                        }
                    }
                }

                override fun onFailure(call: Call<ProductSpecsResponse>, t: Throwable) {
                    Log.e(TAG, "Use case binding failed", t)
                }
            })
    }

    private fun bindDepartmentSpinner() {
        binding.departmentSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (departments.isNotEmpty()) {
                        val selectedDepartment = departments[position]
                        if (sharedViewModel.departmentLiveData.value != selectedDepartment) {
                            sharedViewModel.departmentLiveData.value = departments[position]
                            sharedViewModel.lineLiveData.value = null
                            sharedViewModel.checkTypeLiveData.value = null
                            sharedViewModel.idhNumberLiveData.value = null
                            binding.infoIconImageView.visibility = View.GONE
                            binding.idhNumberAutoCompleteTextView.setText("", false)
                            sharedViewModel.departmentLiveData.value?.id?.let {
                                fetchLinesForDepartmentFromApi(
                                    it
                                )
                            }
                        }

                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun loadDepartmentsFromDatabase() {
        viewLifecycleOwner.lifecycleScope.launch {
            val localDepartments = appDatabase.departmentDao().getAllDepartments()
            if (localDepartments.isNotEmpty()) {
                loadDepartmentAdapter(localDepartments.toDepartmentList())
            }
        }
    }

    private fun fetchDepartmentsFromApi() {
        myApi.getDepartments(SITE_ID).enqueue(object : Callback<List<Department>> {
            override fun onResponse(
                call: Call<List<Department>>, response: Response<List<Department>>
            ) {
                if (response.isSuccessful) {

                    response.body()?.let {
                        checkSetupViewModel.departmentListLiveData.value = it
                        loadDepartmentAdapter(it)
                    }

                    // Update Room database with the latest fetched departments
                    viewLifecycleOwner.lifecycleScope.launch {
                        val departmentEntities = departments.map { department ->
                            DepartmentEntity(
                                department.id,
                                department.name,
                                department.abbreviation,
                                department.description,
                                department.lines
                            )
                        }
                        appDatabase.departmentDao().insertDepartments(departmentEntities)
                    }

                    if (departments.isNotEmpty()) {
                        val selectedDepartment = departments[0]
                        fetchLinesForDepartmentFromApi(selectedDepartment.id)
                    }
                } else {
                    // If the API call fails, attempt to load departments from the Room database
                    loadDepartmentsFromDatabase()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                // If the API call fails, attempt to load departments from the Room database
                loadDepartmentsFromDatabase()
            }
        })
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

    private fun fetchLinesForDepartmentFromApi(departmentId: String) {
        myApi.getLinesForDepartment(departmentId).enqueue(object : Callback<List<Line>> {
            override fun onResponse(call: Call<List<Line>>, response: Response<List<Line>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        it
                        checkSetupViewModel.lineListLiveData.value = it
                        loadLinesAdapter(it)
                    }

                    // Update Room database with the latest fetched lines for the department
                    viewLifecycleOwner.lifecycleScope.launch {
                        val lineEntities = lines.map { line ->
                            LineEntity(
                                line.id,
                                line.abbreviation,
                                line.name,
                                departmentId,
                                line.checkTypes
                            )
                        }
                        appDatabase.lineDao().insertLines(lineEntities)
                    }

                    if (lines.isNotEmpty()) {
                        val selectedLine = lines[0]
                        sharedViewModel.lineLiveData.value = selectedLine
                        fetchCheckTypesForLineFromApi(selectedLine.id)
                        fetchIDHNumbersForLineFromApi(selectedLine.id)
                    }
                } else {
                    // If the API call fails, attempt to load lines from the Room database
                    loadLinesForDepartmentFromDatabase(departmentId)
                }
            }

            override fun onFailure(call: Call<List<Line>>, t: Throwable) {
                // If the API call fails, attempt to load lines from the Room database
                loadLinesForDepartmentFromDatabase(departmentId)
            }
        })
    }

    private fun loadLinesForDepartmentFromDatabase(departmentId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val localLines = appDatabase.lineDao().getLinesByDepartmentId(departmentId)
            if (localLines.isNotEmpty()) {
                loadLinesAdapter(localLines.toLineList())
            }
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

    private fun fetchCheckTypesForLineFromApi(lineId: String) {
        myApi.getCheckTypesForLine(lineId).enqueue(object : Callback<List<CheckType>> {
            override fun onResponse(
                call: Call<List<CheckType>>,
                response: Response<List<CheckType>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { it ->
                        checkSetupViewModel.checkTypeLiveData.value = it
                        loadCheckTypesAdapter(it)
                    }

                    // Update Room database with the latest fetched lines for the department
                    viewLifecycleOwner.lifecycleScope.launch {
                        val checkTypeEntities = checkTypes.map { checkType ->
                            CheckTypeEntity(
                                checkType.id,
                                checkType.name,
                                lineId,
                                checkType.displayName,
                                checkType.checks
                            )
                        }
                        appDatabase.checkTypeDao().insertCheckTypes(checkTypeEntities)
                    }
                } else {
                    // If the API call fails, attempt to load IDH numbers from the Room database
                    loadCheckTypesFromDatabase(lineId)
                }
            }

            override fun onFailure(call: Call<List<CheckType>>, t: Throwable) {
                loadCheckTypesFromDatabase(lineId)
            }
        })
    }

    private fun fetchIDHNumbersForLineFromApi(lineId: String) {
        myApi.getIDHNumbersForLine(lineId).enqueue(object : Callback<List<IDHNumbers>> {
            override fun onResponse(
                call: Call<List<IDHNumbers>>,
                response: Response<List<IDHNumbers>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { it ->
                        checkSetupViewModel.idhNumberLiveData.value = it
                        loadIDHNumbersAdapter(it)
                    }

                    // Update Room database with the latest fetched lines for the department
                    viewLifecycleOwner.lifecycleScope.launch {
                        val idhNumbersEntities = idhNumbers.map { idhNumber ->
                            IDHNumbersEntity(
                                idhNumber.id,
                                idhNumber.productId,
                                idhNumber.lineId,
                                idhNumber.name,
                                idhNumber.description
                            )
                        }
                        appDatabase.idhNumbersDao().insertIDHNumbers(idhNumbersEntities)
                    }
                } else {
                    // If the API call fails, attempt to load IDH numbers from the Room database
                    loadIDHNumbersFromDatabase(lineId)
                }
            }

            override fun onFailure(call: Call<List<IDHNumbers>>, t: Throwable) {
                // If the API call fails, attempt to load IDH numbers from the Room database
                loadIDHNumbersFromDatabase(lineId)
            }
        })
    }

    private fun loadCheckTypesFromDatabase(lineId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val localCheckTypes = appDatabase.checkTypeDao().getAllCheckTypes(lineId)
            loadCheckTypesAdapter(localCheckTypes.toCheckTypeList())
        }
    }

    private fun loadIDHNumbersFromDatabase(lineId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val localIDHNumbers = appDatabase.idhNumbersDao().getIDHNumbersByLineId(lineId)
            loadIDHNumbersAdapter(localIDHNumbers.toIdhNumbersList())
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
        binding.departmentSpinner.isEnabled = false
        binding.lineSpinner.isEnabled = false
        binding.checkTypeSpinner.isEnabled = false
        binding.idhNumberAutoCompleteTextView.isEnabled = false
        binding.infoIconImageView.visibility = View.VISIBLE
        binding.infoIconImageView.setOnClickListener {
            fetchSpecsAndShowDialog()
        }
        binding.startNewCheckButton.visibility = View.VISIBLE
        binding.startNewCheckButton.setOnClickListener {
            // Clear previous values and enable input fields for a new check sequence
            //TODO - fetch new data but don't interrupt the product selections
            //fetchDepartmentsFromApi()
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

    private fun List<DepartmentEntity>.toDepartmentList(): List<Department> {
        return map { departmentEntity ->
            Department(
                departmentEntity.departmentId,
                departmentEntity.name,
                departmentEntity.abbreviation,
                departmentEntity.description,
                departmentEntity.lines
            )
        }
    }

    private fun List<LineEntity>.toLineList(): List<Line> {
        return map { lineEntity ->
            Line(lineEntity.lineId, lineEntity.abbreviation, lineEntity.name, lineEntity.checkTypes)
        }
    }

    private fun List<IDHNumbersEntity>.toIdhNumbersList(): List<IDHNumbers> {
        return map { idhNumbersEntity ->
            IDHNumbers(
                idhNumbersEntity.id,
                idhNumbersEntity.productId,
                idhNumbersEntity.lineId,
                idhNumbersEntity.name,
                idhNumbersEntity.description
            )
        }
    }

    private fun List<CheckTypeEntity>.toCheckTypeList(): List<CheckType> {
        return map { checkTypeEntity ->
            CheckType(
                checkTypeEntity.id,
                checkTypeEntity.name,
                checkTypeEntity.lineId,
                checkTypeEntity.displayName,
                checkTypeEntity.checks
            )
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
