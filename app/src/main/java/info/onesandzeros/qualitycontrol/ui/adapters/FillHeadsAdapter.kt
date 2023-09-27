package info.onesandzeros.qualitycontrol.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.FillHeadItem

class FillHeadsAdapter(
    private val mav: Double,
    private val lsl: Double,
    private val usl: Double,
    private val warningColor: Int,
    private val errorColor: Int
) :
    ListAdapter<FillHeadItem, FillHeadsAdapter.FillHeadViewHolder>(FillHeadItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FillHeadViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_fill_head, parent, false)
        return FillHeadViewHolder(view)
    }

    override fun onBindViewHolder(holder: FillHeadViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FillHeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fillHeadWeight: TextView = itemView.findViewById(R.id.fill_head_weight)

        fun bind(item: FillHeadItem) {
            val weightText = item.weight?.let { "${it}g" } ?: "[TBD]"
            fillHeadWeight.text =
                itemView.context.getString(R.string.fill_head_weight, item.fillHead, weightText)

            // Set color based on weight and spec values
            item.weight?.let { weight ->
                when {
                    weight < mav -> fillHeadWeight.setTextColor(errorColor)
                    weight in mav..lsl || weight > usl -> fillHeadWeight.setTextColor(warningColor)
                    else -> fillHeadWeight.setTextColor(Color.BLACK)
                }
            } ?: run {
                fillHeadWeight.setTextColor(Color.GRAY) // Default color when weight is null
            }
        }
    }
}

class FillHeadItemDiffCallback : DiffUtil.ItemCallback<FillHeadItem>() {
    override fun areItemsTheSame(oldItem: FillHeadItem, newItem: FillHeadItem): Boolean {
        return oldItem.fillHead == newItem.fillHead
    }

    override fun areContentsTheSame(oldItem: FillHeadItem, newItem: FillHeadItem): Boolean {
        return oldItem == newItem
    }
}
