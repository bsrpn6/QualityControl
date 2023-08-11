package info.onesandzeros.qualitycontrol.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.FillHeadItem

class FillHeadsAdapter :
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
            fillHeadWeight.text = "${item.fillHead}: $weightText"
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
