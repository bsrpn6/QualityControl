package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.adapters

import BarcodeScannerUtil
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.FillHeadItem
import info.onesandzeros.qualitycontrol.api.models.WeightCheckItem
import info.onesandzeros.qualitycontrol.databinding.ItemBarcodeCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemBooleanCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemDoubleCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemIntegerCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemStringCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemUnknownCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemWeightCheckBinding
import info.onesandzeros.qualitycontrol.ui.fragments.ChecksFragmentDirections

class ChecksAdapter(
    private val checksList: List<CheckItem>,
    private val barcodeScannerUtil: BarcodeScannerUtil
) :
    RecyclerView.Adapter<ChecksAdapter.CheckViewHolder>() {

    // ViewHolder to hold the views in each item using ViewBinding
    abstract class CheckViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(check: CheckItem)
    }

    // ViewHolders with ViewBinding for each check type
    class BarcodeCheckViewHolder(
        private val binding: ItemBarcodeCheckBinding,
        private val scannerUtil: BarcodeScannerUtil
    ) : CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for barcode check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description

            val myBarcode = (check.result as? String) ?: (check.value as? String) ?: ""

            // Set the switch state based on the user input value
            binding.barcodeValueTextView.text = myBarcode

            binding.barcodeIconImageView.setOnClickListener {
                scannerUtil.startBarcodeScanning { barcodeValue ->
                    binding.barcodeValueTextView.text = barcodeValue ?: "Scanning failed"
                    check.result = barcodeValue

                    // Re-evaluate the comparison and update the background color
                    val updatedUserInputMatchesExpected = check.value == check.result
                    if (updatedUserInputMatchesExpected) {
                        binding.root.setBackgroundColor(Color.WHITE)
                    } else {
                        binding.root.setBackgroundColor(Color.parseColor("#FFC0C0"))
                    }
                }
            }
        }
    }

    class WeightCheckViewHolder(
        private val binding: ItemWeightCheckBinding
    ) : CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for barcode check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description

            val gson = Gson()
            val jsonString = gson.toJson(check.value)
            val weightCheckItem = gson.fromJson(jsonString, WeightCheckItem::class.java)
            if (check.result != null) {
                val fillHeads = check.result as List<FillHeadItem>

                binding.tinyGraphView.mav = weightCheckItem.mav
                binding.tinyGraphView.lsl = weightCheckItem.lsl
                binding.tinyGraphView.usl = weightCheckItem.usl
                binding.tinyGraphView.fillHeadValues = fillHeads
            }

            // Set a click listener on the image (or button) to start weight capture
            binding.scaleIconImageView.setOnClickListener {
                // Trigger navigation to the WeightCaptureFragment
                val action =
                    ChecksFragmentDirections.actionChecksFragmentToWeightCaptureFragment(check)
                it.findNavController().navigate(action)
            }
        }
    }

    class BooleanCheckViewHolder(private val binding: ItemBooleanCheckBinding) :
        CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for boolean check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description

            val myBoolean = (check.result as? Boolean) ?: (check.value as? Boolean) ?: false

            // Set the switch state based on the user input value
            binding.checkSwitch.isChecked = myBoolean

            // Add an OnCheckedChangeListener to the switch
            binding.checkSwitch.setOnCheckedChangeListener { _, isChecked ->
                // Update the userInputValue property of the CheckItem with the new switch state
                check.result = isChecked

                // Re-evaluate the comparison and update the background color
                val updatedUserInputMatchesExpected = check.value == check.result
                if (updatedUserInputMatchesExpected) {
                    binding.root.setBackgroundColor(Color.WHITE)
                } else {
                    binding.root.setBackgroundColor(Color.parseColor("#FFC0C0"))
                }
            }
        }
    }


    class IntegerCheckViewHolder(private val binding: ItemIntegerCheckBinding) :
        CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for integer check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description
            // Bind other views for integer check type as needed

            val myInt = (check.result as? Int) ?: (check.value as? Int) ?: 0

            // Set the text of the EditText based on the user input value
            binding.integerInputEditText.setText(myInt.toString())

            // Add an OnValueChangeListener to the EditText
            binding.integerInputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    // Update the userInputValue property of the CheckItem with the new integer value
                    val inputValue = s.toString()
                    check.result = inputValue.toIntOrNull()

                    // Re-evaluate the comparison and update the background color
                    val updatedUserInputMatchesExpected = check.value == check.result
                    if (updatedUserInputMatchesExpected) {
                        binding.root.setBackgroundColor(Color.WHITE)
                    } else {
                        binding.root.setBackgroundColor(Color.parseColor("#FFC0C0"))
                    }
                }
            })
        }
    }


    class DoubleCheckViewHolder(private val binding: ItemDoubleCheckBinding) :
        CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for double check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description
            // Bind other views for double check type as needed

            val myDouble = (check.result as? Double) ?: (check.value as? Double) ?: 0.0

            // Set the text of the EditText based on the user input value
            binding.doubleInputEditText.setText(myDouble.toString())

            // Add an OnValueChangeListener to the EditText
            binding.doubleInputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    // Update the userInputValue property of the CheckItem with the new double value
                    val inputValue = s.toString().toDoubleOrNull()
                    check.result = inputValue

                    // Re-evaluate the comparison and update the background color
                    val updatedUserInputMatchesExpected = check.value == check.result
                    if (updatedUserInputMatchesExpected) {
                        binding.root.setBackgroundColor(Color.WHITE)
                    } else {
                        binding.root.setBackgroundColor(Color.parseColor("#FFC0C0"))
                    }
                }
            })
        }
    }


    class StringCheckViewHolder(private val binding: ItemStringCheckBinding) :
        CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for string check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description

            val myString = (check.result as? String) ?: (check.value as? String) ?: ""

            // Set the text of the EditText based on the user input value
            binding.stringInputEditText.setText(myString)

            // Add an OnValueChangeListener to the EditText
            binding.stringInputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    // Update the userInputValue property of the CheckItem with the new string value
                    check.result = s.toString()

                    // Re-evaluate the comparison and update the background color
                    val updatedUserInputMatchesExpected = check.value == check.result
                    if (updatedUserInputMatchesExpected) {
                        binding.root.setBackgroundColor(Color.WHITE)
                    } else {
                        binding.root.setBackgroundColor(Color.parseColor("#FFC0C0"))
                    }
                }
            })
        }
    }


    class UnknownCheckViewHolder(private val binding: ItemUnknownCheckBinding) :
        CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for unknown check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description
            // Bind other views for unknown check type as needed
        }
    }

    // Create a new ViewHolder for each item in the RecyclerView based on its type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_BARCODE -> {
                val binding = ItemBarcodeCheckBinding.inflate(inflater, parent, false)
                BarcodeCheckViewHolder(binding, barcodeScannerUtil)
            }

            TYPE_WEIGHT -> {
                val binding = ItemWeightCheckBinding.inflate(inflater, parent, false)
                WeightCheckViewHolder(binding)
            }

            TYPE_BOOLEAN -> {
                val binding = ItemBooleanCheckBinding.inflate(inflater, parent, false)
                BooleanCheckViewHolder(binding)
            }

            TYPE_INTEGER -> {
                val binding = ItemIntegerCheckBinding.inflate(inflater, parent, false)
                IntegerCheckViewHolder(binding)
            }

            TYPE_DOUBLE -> {
                val binding = ItemDoubleCheckBinding.inflate(inflater, parent, false)
                DoubleCheckViewHolder(binding)
            }

            TYPE_STRING -> {
                val binding = ItemStringCheckBinding.inflate(inflater, parent, false)
                StringCheckViewHolder(binding)
            }
            // Add other cases for different check types using ViewBinding
            else -> {
                val binding = ItemUnknownCheckBinding.inflate(inflater, parent, false)
                UnknownCheckViewHolder(binding)
            }
        }
    }

    // Bind data to the ViewHolder's views for each item in the RecyclerView
    override fun onBindViewHolder(holder: CheckViewHolder, position: Int) {
        val currentItem = checksList[position]
        holder.bind(currentItem)
    }

    // Return the total number of items in the RecyclerView
    override fun getItemCount() = checksList.size

    // Get the view type based on the "type" field in CheckModel
    override fun getItemViewType(position: Int): Int {
        val currentItem = checksList[position]
        return when (currentItem.type) {
            "boolean" -> TYPE_BOOLEAN
            "integer" -> TYPE_INTEGER
            "string" -> TYPE_STRING
            "barcode" -> TYPE_BARCODE
            "weight" -> TYPE_WEIGHT
            "double" -> TYPE_DOUBLE
            // Add other types as needed
            else -> TYPE_UNKNOWN // Unknown type
        }
    }


    companion object {
        private const val TYPE_BOOLEAN = 0
        private const val TYPE_INTEGER = 1
        private const val TYPE_STRING = 2
        private const val TYPE_BARCODE = 3
        private const val TYPE_WEIGHT = 4
        private const val TYPE_DOUBLE = 5

        // Add other types as needed
        private const val TYPE_UNKNOWN = -1 // Unknown type
    }
}
