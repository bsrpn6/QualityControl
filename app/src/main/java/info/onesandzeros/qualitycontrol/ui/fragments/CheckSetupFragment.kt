package info.onesandzeros.qualitycontrol.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.MyApi
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.data.AppDatabase
import info.onesandzeros.qualitycontrol.data.models.DepartmentEntity
import info.onesandzeros.qualitycontrol.data.models.IDHNumbersEntity
import info.onesandzeros.qualitycontrol.data.models.LineEntity
import info.onesandzeros.qualitycontrol.databinding.FragmentCheckSetupBinding
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.viewmodels.CheckSetupViewModel
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel
import kotlinx.coroutines.GlobalScope
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
    private val idhNumbers = mutableListOf<IDHNumbers>()

    private lateinit var departmentAdapter: ArrayAdapter<String>
    private lateinit var lineAdapter: ArrayAdapter<String>
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
        bindIdhAutoCompleteTextView()

        // Check if previous values exist
        if (sharedViewModel.departmentLiveData.value != null
            && sharedViewModel.lineLiveData.value != null
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
            Log.d(
                "CheckSetupFragment",
                sharedViewModel.departmentLiveData.value.toString() + " | " + sharedViewModel.lineLiveData.value.toString() + " | " + sharedViewModel.idhNumberLiveData.value.toString()
            )
            // Check if all three fields have valid selections
            if (sharedViewModel.departmentLiveData.value != null && sharedViewModel.lineLiveData.value != null && sharedViewModel.idhNumberLiveData.value != null) {

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

    private fun bindLineSpinner() {
        binding.lineSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (lines.isNotEmpty()) {
                    val selectedLine = lines[position]
                    if (sharedViewModel.lineLiveData.value != selectedLine) {
                        sharedViewModel.lineLiveData.value = lines[position]
                        sharedViewModel.idhNumberLiveData.value =
                            null // Clear the IDH number selection when the line is changed
                        binding.idhNumberAutoCompleteTextView.setText("", false)
                        fetchIDHNumbersForLineFromApi(
                            sharedViewModel.lineLiveData.value?.id ?: -1
                        )
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
                val selectedIDHNumber = idhNumbers.find { it.idhNumber == selectedIdhNumberValue }
                if (sharedViewModel.idhNumberLiveData.value != selectedIDHNumber) {
                    sharedViewModel.idhNumberLiveData.value = selectedIDHNumber
                }
            }
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
                            sharedViewModel.lineLiveData.value =
                                null // Clear the line selection when the department is changed
                            sharedViewModel.idhNumberLiveData.value =
                                null // Clear the IDH number selection when the department is changed
                            binding.idhNumberAutoCompleteTextView.setText("", false)
                            fetchLinesForDepartmentFromApi(
                                sharedViewModel.departmentLiveData.value?.id ?: -1
                            )
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
        myApi.getDepartments().enqueue(object : Callback<List<Department>> {
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
                            DepartmentEntity(department.id, department.name)
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

    private fun fetchLinesForDepartmentFromApi(departmentId: Int) {
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
                            LineEntity(line.id, line.name, departmentId)
                        }
                        appDatabase.lineDao().insertLines(lineEntities)
                    }

                    if (lines.isNotEmpty()) {
                        val selectedLine = lines[0]
                        sharedViewModel.lineLiveData.value = selectedLine
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

    private fun loadLinesForDepartmentFromDatabase(departmentId: Int) {
        GlobalScope.launch {
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

    private fun fetchIDHNumbersForLineFromApi(lineId: Int) {
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
                                idhNumber.idhNumber,
                                idhNumber.lineId,
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

    private fun loadIDHNumbersFromDatabase(lineId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val localIDHNumbers = appDatabase.idhNumbersDao().getIDHNumbersByLineId(lineId)
            loadIDHNumbersAdapter(localIDHNumbers.toIdhNumbersList())
        }
    }

    private fun loadIDHNumbersAdapter(idhNumbersList: List<IDHNumbers>) {
        idhNumbers.clear()
        idhNumbers.addAll(idhNumbersList)
        val localIdhNumbers = idhNumbers.map { it.idhNumber }
        idhNumberAdapter.clear()
        idhNumberAdapter.addAll(localIdhNumbers)

        // Set the spinner selection based on the value from the SharedViewModel
        val selectedIDHNumber = sharedViewModel.idhNumberLiveData.value
        if (selectedIDHNumber != null) {
            val selectedIndex =
                idhNumbers.indexOfFirst { it.idhNumber == selectedIDHNumber.idhNumber }
            if (selectedIndex != -1) {
                binding.idhNumberAutoCompleteTextView.setText(
                    selectedIDHNumber.idhNumber.toString(),
                    false
                )
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

        checkSetupViewModel.idhNumberLiveData.value?.let { idhNumbers ->
            loadIDHNumbersAdapter(idhNumbers)
        }
    }

    private fun disableInputFields() {
        binding.departmentSpinner.isEnabled = false
        binding.lineSpinner.isEnabled = false
        binding.idhNumberAutoCompleteTextView.isEnabled = false
        binding.startNewCheckButton.visibility = View.VISIBLE
        binding.startNewCheckButton.setOnClickListener {
            // Clear previous values and enable input fields for a new check sequence
            //TODO - fetch new data but don't interupt the product selections
            //fetchDepartmentsFromApi()
            enableInputFields()
        }
    }

    private fun enableInputFields() {
        binding.departmentSpinner.isEnabled = true
        binding.lineSpinner.isEnabled = true
        binding.idhNumberAutoCompleteTextView.isEnabled = true
        binding.startNewCheckButton.visibility = View.GONE
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        sharedViewModel.clearUserName()
        Toast.makeText(
            requireContext(), "Logout Successful", Toast.LENGTH_LONG
        ).show()
        findNavController().navigate(R.id.action_checkSetupFragment_to_loginFragment)
    }

    private fun List<DepartmentEntity>.toDepartmentList(): List<Department> {
        return map { departmentEntity ->
            Department(departmentEntity.department_id, departmentEntity.name)
        }
    }

    private fun List<LineEntity>.toLineList(): List<Line> {
        return map { lineEntity ->
            Line(lineEntity.line_id, lineEntity.name, lineEntity.departmentId)
        }
    }

    private fun List<IDHNumbersEntity>.toIdhNumbersList(): List<IDHNumbers> {
        return map { idhNumbersEntity ->
            IDHNumbers(
                idhNumbersEntity.idhNumber,
                idhNumbersEntity.lineId,
                idhNumbersEntity.description
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
