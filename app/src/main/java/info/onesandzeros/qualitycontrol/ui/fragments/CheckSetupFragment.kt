package info.onesandzeros.qualitycontrol.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var myApi: MyApi

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        departmentAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item)
        lineAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item)

        binding.departmentSpinner.adapter = departmentAdapter
        binding.lineSpinner.adapter = lineAdapter
        idhNumberAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<Int>()
        )
        binding.idhNumberAutoCompleteTextView.setAdapter(idhNumberAdapter)
        binding.idhNumberAutoCompleteTextView.threshold =
            1 // Set minimum number of characters to trigger suggestions

        var selectedDepartment: Department? = null
        var selectedLine: Line? = null
        var selectedIDHNumber: Int? = null


        fetchDepartmentsFromApi()

        binding.departmentSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (departments.isNotEmpty()) {
                        selectedDepartment = departments[position]
                        selectedLine =
                            null // Clear the line selection when the department is changed
                        selectedIDHNumber =
                            null // Clear the IDH number selection when the department is changed
                        loadLinesForDepartment(selectedDepartment?.id ?: -1)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        binding.lineSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (lines.isNotEmpty()) {
                    selectedLine = lines[position]
                    selectedIDHNumber =
                        null // Clear the IDH number selection when the line is changed
                    loadIDHNumbersForLine(selectedLine?.id ?: -1)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.idhNumberAutoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                // Update the selectedIDHNumber when an IDH number is selected from the dropdown
                selectedIDHNumber = parent.getItemAtPosition(position) as Int
            }

        binding.startChecksButton.setOnClickListener {
            // Check if all three fields have valid selections
            if (selectedDepartment != null && selectedLine != null && selectedIDHNumber != null) {
                sharedViewModel.departmentLiveData.value = selectedDepartment
                sharedViewModel.lineLiveData.value = selectedLine?.name
                sharedViewModel.idhNumberLiveData.value = selectedIDHNumber

                // Proceed to ChecksFragment when the button is clicked
                findNavController().navigate(R.id.action_checkSetupFragment_to_checksFragment)
            } else {
                // Display a toast indicating invalid selections
                Toast.makeText(
                    requireContext(),
                    "Please select valid values for all fields",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadDepartmentsFromDatabase() {
        GlobalScope.launch {
            val localDepartments = appDatabase.departmentDao().getAllDepartments()
            if (localDepartments.isNotEmpty()) {
                departments.clear()
                departments.addAll(localDepartments.toDepartmentList())
                val departmentNames = departments.map { it.name }
                withContext(Dispatchers.Main) {
                    departmentAdapter.clear()
                    departmentAdapter.addAll(departmentNames)
                }
            }
        }
    }

    private fun fetchDepartmentsFromApi() {
        myApi.getDepartments().enqueue(object : Callback<List<Department>> {
            override fun onResponse(
                call: Call<List<Department>>,
                response: Response<List<Department>>
            ) {
                if (response.isSuccessful) {
                    departments.clear()
                    departments.addAll(response.body() ?: emptyList())
                    val departmentNames = departments.map { it.name }
                    departmentAdapter.clear()
                    departmentAdapter.addAll(departmentNames)

                    // Update Room database with the latest fetched departments
                    GlobalScope.launch {
                        val departmentEntities = departments.map { department ->
                            DepartmentEntity(department.id, department.name)
                        }
                        appDatabase.departmentDao().insertDepartments(departmentEntities)
                    }

                    if (departments.isNotEmpty()) {
                        val selectedDepartment = departments[0]
                        loadLinesForDepartment(selectedDepartment.id)
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


    private fun loadLinesForDepartment(departmentId: Int) {
        myApi.getLinesForDepartment(departmentId).enqueue(object : Callback<List<Line>> {
            override fun onResponse(call: Call<List<Line>>, response: Response<List<Line>>) {
                if (response.isSuccessful) {
                    lines.clear()
                    lines.addAll(response.body() ?: emptyList())
                    val lineNames = lines.map { it.name }
                    lineAdapter.clear()
                    lineAdapter.addAll(lineNames)

                    // Update Room database with the latest fetched lines for the department
                    GlobalScope.launch {
                        val lineEntities = lines.map { line ->
                            LineEntity(line.id, line.name, departmentId)
                        }
                        appDatabase.lineDao().insertLines(lineEntities)
                    }
                } else {
                    // If the API call fails, attempt to load lines from the Room database
                    loadLinesFromDatabase(departmentId)
                }
            }

            override fun onFailure(call: Call<List<Line>>, t: Throwable) {
                // If the API call fails, attempt to load lines from the Room database
                loadLinesFromDatabase(departmentId)
            }
        })
    }

    private fun loadLinesFromDatabase(departmentId: Int) {
        GlobalScope.launch {
            val localLines = appDatabase.lineDao().getLinesByDepartmentId(departmentId)
            if (localLines.isNotEmpty()) {
                lines.clear()
                lines.addAll(localLines.toLineList())
                val lineNames = lines.map { it.name }
                withContext(Dispatchers.Main) {
                    lineAdapter.clear()
                    lineAdapter.addAll(lineNames)
                }
            }
        }
    }

    private fun loadIDHNumbersForLine(lineId: Int) {
        myApi.getIDHNumbersForLine(lineId).enqueue(object : Callback<List<IDHNumbers>> {
            override fun onResponse(
                call: Call<List<IDHNumbers>>,
                response: Response<List<IDHNumbers>>
            ) {
                if (response.isSuccessful) {
                    idhNumbers.clear()
                    idhNumbers.addAll(response.body() ?: emptyList())

                    // Find the IDH numbers for the selected line
                    val filteredIDHNumbers = idhNumbers.find { it.lineId == lineId }?.idhNumbers

                    // Update the adapter with the filtered IDH numbers
                    idhNumberAdapter.clear()
                    if (filteredIDHNumbers != null) {
                        idhNumberAdapter.addAll(filteredIDHNumbers)

                        // Update Room database with the latest fetched IDH numbers for the line
                        GlobalScope.launch {
                            val idhNumbersEntity = IDHNumbersEntity(lineId, filteredIDHNumbers)
                            appDatabase.idhNumbersDao().insertIDHNumbers(idhNumbersEntity)
                        }
                    }
                    idhNumberAdapter.notifyDataSetChanged()
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
        GlobalScope.launch {
            val localIDHNumbers = appDatabase.idhNumbersDao().getIDHNumbersByLineId(lineId)

            // Check if localIDHNumbers is not null before adding it to the adapter
            localIDHNumbers.let {
                val idhNumbersEntity = it // Single IDHNumbersEntity object
                val idhNumbersList =
                    idhNumbersEntity.idhNumbers // Use the idhNumbers property directly from IDHNumbersEntity

                // Create a single IDHNumbers object and add it to the idhNumbers list
                val idhNumbersData = IDHNumbers(idhNumbersEntity.lineId, idhNumbersList)
                idhNumbers.clear()
                idhNumbers.add(idhNumbersData)

                idhNumberAdapter.clear()
                idhNumberAdapter.addAll(idhNumbersList)
                idhNumberAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        sharedViewModel.clearUserName()
        Toast.makeText(
            requireContext(),
            "Logout Successful",
            Toast.LENGTH_LONG
        ).show()
        findNavController().navigate(R.id.action_checkSetupFragment_to_loginFragment)

    }


    fun List<DepartmentEntity>.toDepartmentList(): List<Department> {
        return map { departmentEntity ->
            Department(departmentEntity.department_id, departmentEntity.name)
        }
    }

    fun List<LineEntity>.toLineList(): List<Line> {
        return map { lineEntity ->
            Line(lineEntity.line_id, lineEntity.name, lineEntity.departmentId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
