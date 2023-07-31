package info.onesandzeros.qualitycontrol
// ChecksAdapter.kt

import androidx.viewbinding.ViewBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dagger.internal.DoubleCheck
import info.onesandzeros.qualitycontrol.databinding.ItemBarcodeCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemBooleanCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemDoubleCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemIntegerCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemStringCheckBinding
import info.onesandzeros.qualitycontrol.databinding.ItemUnknownCheckBinding

class ChecksAdapter(private val checksList: List<CheckItem>) :
    RecyclerView.Adapter<ChecksAdapter.CheckViewHolder>() {

    // ViewHolder to hold the views in each item using ViewBinding
    abstract class CheckViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(check: CheckItem)
    }

    // ViewHolders with ViewBinding for each check type
    class BarcodeCheckViewHolder(private val binding: ItemBarcodeCheckBinding) : CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for boolean check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description
            // Bind other views for boolean check type as needed
        }
    }

    class BooleanCheckViewHolder(private val binding: ItemBooleanCheckBinding) : CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for boolean check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description
            // Bind other views for boolean check type as needed
        }
    }

    class IntegerCheckViewHolder(private val binding: ItemIntegerCheckBinding) : CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for integer check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description
            // Bind other views for integer check type as needed
        }
    }

    class DoubleCheckViewHolder(private val binding: ItemDoubleCheckBinding) : CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for integer check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description
            // Bind other views for integer check type as needed
        }
    }

    class StringCheckViewHolder(private val binding: ItemStringCheckBinding) : CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for string check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description
            // Bind other views for string check type as needed
        }
    }

    class UnknownCheckViewHolder(private val binding: ItemUnknownCheckBinding) : CheckViewHolder(binding) {
        override fun bind(check: CheckItem) {
            // Bind the views for unknown check type using ViewBinding
            binding.titleTextView.text = check.title
            binding.descriptionTextView.text = check.description
            // Bind other views for unknown check type as needed
        }
    }

    // Add other CheckViewHolder classes for different check types as needed

    // Create a new ViewHolder for each item in the RecyclerView based on its type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_BARCODE -> {
                val binding = ItemBarcodeCheckBinding.inflate(inflater, parent, false)
                BarcodeCheckViewHolder(binding)
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
        private const val TYPE_DOUBLE = 4
        // Add other types as needed
        private const val TYPE_UNKNOWN = -1 // Unknown type
    }
}

