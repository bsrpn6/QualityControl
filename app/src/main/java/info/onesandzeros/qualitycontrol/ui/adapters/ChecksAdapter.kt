package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.adapters

import android.app.Dialog
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.WeightCheckItem
import info.onesandzeros.qualitycontrol.databinding.ItemBarcodeCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemBooleanCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemDatecodeCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemDoubleCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemIntegerCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemStringCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemUnknownCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemWeightCheckBinding
import info.onesandzeros.qualitycontrol.utils.BarcodeScannerUtil
import info.onesandzeros.qualitycontrol.utils.DateCodeScannerUtil
import info.onesandzeros.qualitycontrol.utils.ImageDetailsDisplayer
import info.onesandzeros.qualitycontrol.utils.WeightCaptureUtil

class ChecksAdapter(
    private val checksList: List<CheckItem>,
    private val barcodeScannerUtil: BarcodeScannerUtil,
    private val datecodeScannerUtil: DateCodeScannerUtil,
    private val weightCaptureUtil: WeightCaptureUtil
) :
    RecyclerView.Adapter<ChecksAdapter.CheckViewHolder>() {

    // ViewHolder to hold the views in each item using ViewBinding
    abstract class CheckViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract val descriptionTextView: TextView
        private var isDescriptionExpanded = false

        open fun bind(check: CheckItem) {
            // Reset maxLines to ensure that the entire text is displayed for calculating line count
            descriptionTextView.maxLines = Integer.MAX_VALUE
            descriptionTextView.text = check.description

            // Use a ViewTreeObserver to wait for the layout to be completed
            descriptionTextView.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Remove the listener to ensure it's not called multiple times
                    descriptionTextView.viewTreeObserver.removeOnPreDrawListener(this)

                    // Now that the layout is complete, check if the description exceeds two lines
                    if (descriptionExceedsTwoLines()) {
                        collapseDescription()
                        descriptionTextView.setOnClickListener {
                            if (isDescriptionExpanded) {
                                collapseDescription()
                            } else {
                                expandDescription()
                            }
                        }
                    }
                    return true
                }
            })
        }

        private fun collapseDescription() {
            if (descriptionExceedsTwoLines()) {
                descriptionTextView.maxLines = 2
            }
            isDescriptionExpanded = false
        }

        private fun expandDescription() {
            descriptionTextView.maxLines = Integer.MAX_VALUE
            isDescriptionExpanded = true
        }

        private fun descriptionExceedsTwoLines(): Boolean {
            descriptionTextView.maxLines = Integer.MAX_VALUE
            descriptionTextView.measure(
                View.MeasureSpec.makeMeasureSpec(
                    descriptionTextView.width,
                    View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            return descriptionTextView.lineCount > 2
        }
    }

    // ViewHolders with ViewBinding for each check type
    class BarcodeCheckViewHolder(
        private val binding: ItemBarcodeCheckBinding,
        private val scannerUtil: BarcodeScannerUtil
    ) : CheckViewHolder(binding) {

        override val descriptionTextView = binding.descriptionTextView

        override fun bind(check: CheckItem) {
            super.bind(check)

            // Bind the views for barcode check type using ViewBinding
            binding.titleTextView.text = check.title

            val myBarcode = (check.result as? String) ?: (check.expectedValue as? String) ?: ""

            // Set the switch state based on the user input value
            binding.barcodeValueTextView.text = myBarcode

            binding.barcodeIconImageView.setOnClickListener {
                scannerUtil.startBarcodeScanning { barcodeValue ->
                    binding.barcodeValueTextView.text = barcodeValue ?: "Scanning failed"
                    check.result = barcodeValue

                    // Re-evaluate the comparison and update the background color
                    val updatedUserInputMatchesExpected = check.expectedValue == check.result
                    if (updatedUserInputMatchesExpected) {
                        binding.root.setBackgroundColor(Color.WHITE)
                    } else {
                        binding.root.setBackgroundColor(Color.parseColor("#FFC0C0"))
                    }
                }
            }
        }
    }

    class DatecodeCheckViewHolder(
        private val binding: ItemDatecodeCheckBinding,
        private val scannerUtil: DateCodeScannerUtil
    ) : CheckViewHolder(binding) {

        override val descriptionTextView = binding.descriptionTextView

        override fun bind(check: CheckItem) {
            super.bind(check)

            // Bind the views for Datecode check type using ViewBinding
            binding.titleTextView.text = check.title

            val myDatecode = (check.result as? String) ?: (check.expectedValue as? String) ?: ""

            // Set the switch state based on the user input value
            binding.datecodeValueTextView.text = myDatecode

            binding.datecodeIconImageView.setOnClickListener {
                scannerUtil.startDateCodeScanning { DatecodeValue ->
                    binding.datecodeValueTextView.text = DatecodeValue ?: "Scanning failed"
                    check.result = DatecodeValue

                    // Re-evaluate the comparison and update the background color
                    val updatedUserInputMatchesExpected = check.expectedValue == check.result
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
        private val binding: ItemWeightCheckBinding,
        private val weightCaptureUtil: WeightCaptureUtil
    ) : CheckViewHolder(binding) {

        override val descriptionTextView = binding.descriptionTextView

        override fun bind(check: CheckItem) {
            super.bind(check)

            // Bind the views for barcode check type using ViewBinding
            binding.titleTextView.text = check.title

            val gson = Gson()
            val jsonString = gson.toJson(check.expectedValue)
            val weightCheckItem = gson.fromJson(jsonString, WeightCheckItem::class.java)
            binding.tinyGraphView.mav = weightCheckItem.mav
            binding.tinyGraphView.lsl = weightCheckItem.lsl
            binding.tinyGraphView.usl = weightCheckItem.usl

            // Set a click listener on the image (or button) to start weight capture
            binding.scaleIconImageView.setOnClickListener {
                weightCaptureUtil.startWeightCapture(weightCheckItem) { weightCaptureValue ->
                    check.result = weightCaptureValue
                    binding.tinyGraphView.fillHeadValues = weightCaptureValue!!
                }
            }
        }
    }

    class BooleanCheckViewHolder(private val binding: ItemBooleanCheckBinding) :
        CheckViewHolder(binding) {

        override val descriptionTextView = binding.descriptionTextView

        override fun bind(check: CheckItem) {
            super.bind(check)

            // Bind the views for boolean check type using ViewBinding
            binding.titleTextView.text = check.title

            if (!check.images.isNullOrEmpty()) {
                binding.imageIconView.visibility = View.VISIBLE

                // Set click listener for imageIconView
                binding.imageIconView.setOnClickListener {
                    val dialog = Dialog(binding.root.context)

                    // Inflate the layout containing a ViewGroup for the details
                    val detailsLayout = LayoutInflater.from(binding.root.context)
                        .inflate(R.layout.scroll_view_dialog_layout, null)

                    // Set up the dialog's content view
                    dialog.setContentView(detailsLayout)

                    // Adjust the dialog's width and height
                    val window = dialog.window
                    window?.setLayout(
                        (binding.root.resources.displayMetrics.widthPixels * 0.9).toInt(),
                        WindowManager.LayoutParams.WRAP_CONTENT
                    )

                    // Create an ImageDetailsDisplayer and display the image details
                    val displayer = ImageDetailsDisplayer(
                        binding.root.context, detailsLayout.findViewById(R.id.detailsLayout)
                    )
                    displayer.displayImageDetails(check.images)

                    // Set up the close button
                    detailsLayout.findViewById<Button>(R.id.closeButton)
                        .setOnClickListener {
                            dialog.dismiss()
                        }

                    // Show the dialog
                    dialog.show()
                }
            }

            val myBoolean = (check.result as? Boolean) ?: (check.expectedValue as? Boolean) ?: false

            // Set the switch state based on the user input value
            binding.checkSwitch.isChecked = myBoolean

            // Add an OnCheckedChangeListener to the switch
            binding.checkSwitch.setOnCheckedChangeListener { _, isChecked ->
                // Update the userInputValue property of the CheckItem with the new switch state
                check.result = isChecked

                // Re-evaluate the comparison and update the background color
                val updatedUserInputMatchesExpected = check.expectedValue == check.result
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

        override val descriptionTextView = binding.descriptionTextView

        override fun bind(check: CheckItem) {
            super.bind(check)

            // Bind the views for integer check type using ViewBinding
            binding.titleTextView.text = check.title

            val myInt = (check.result as? Int) ?: (check.expectedValue as? Int) ?: 0

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
                    val updatedUserInputMatchesExpected = check.expectedValue == check.result
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

        override val descriptionTextView = binding.descriptionTextView

        override fun bind(check: CheckItem) {
            super.bind(check)

            // Bind the views for double check type using ViewBinding
            binding.titleTextView.text = check.title

            val myDouble = (check.result as? Double) ?: (check.expectedValue as? Double) ?: 0.0

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
                    val updatedUserInputMatchesExpected = check.expectedValue == check.result
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

        override val descriptionTextView = binding.descriptionTextView

        override fun bind(check: CheckItem) {
            super.bind(check)

            // Bind the views for string check type using ViewBinding
            binding.titleTextView.text = check.title

            val myString = (check.result as? String) ?: (check.expectedValue as? String) ?: ""

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
                    val updatedUserInputMatchesExpected = check.expectedValue == check.result
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

        override val descriptionTextView = binding.descriptionTextView

        override fun bind(check: CheckItem) {
            super.bind(check)

            // Bind the views for unknown check type using ViewBinding
            binding.titleTextView.text = check.title
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

            TYPE_DATE_CODE -> {
                val binding = ItemDatecodeCheckBinding.inflate(inflater, parent, false)
                DatecodeCheckViewHolder(binding, datecodeScannerUtil)
            }

            TYPE_WEIGHT -> {
                val binding = ItemWeightCheckBinding.inflate(inflater, parent, false)
                WeightCheckViewHolder(binding, weightCaptureUtil)
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
            "datecode" -> TYPE_DATE_CODE
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
        private const val TYPE_DATE_CODE = 4
        private const val TYPE_WEIGHT = 5
        private const val TYPE_DOUBLE = 6

        // Add other types as needed
        private const val TYPE_UNKNOWN = -1 // Unknown type
    }
}
