package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.databinding.ItemImageReelBinding

class ImageReelAdapter(private val onImageRemove: (Uri) -> Unit) :
    RecyclerView.Adapter<ImageReelAdapter.ImageViewHolder>() {

    private val imageUris = mutableListOf<Uri>()

    fun setImages(uris: List<Uri>) {
        imageUris.clear()
        imageUris.addAll(uris)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding =
            ItemImageReelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageUris[position])
    }

    override fun getItemCount() = imageUris.size

    inner class ImageViewHolder(private val binding: ItemImageReelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            // Use Glide to load the image
            Glide.with(binding.reelImage.context)
                .load(uri)
                .placeholder(R.drawable.placeholder_image)  // Optional: shown when the image is loading
                .error(R.drawable.error_image)  // Optional: shown if there's an error loading the image
                .into(binding.reelImage)

            binding.closeButton.setOnClickListener {
                onImageRemove(uri)
            }
        }
    }
}
