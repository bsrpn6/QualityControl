package info.onesandzeros.qualitycontrol.ui.displayers

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.Image

class ImageDetailsDisplayer(private val context: Context, private val layout: ViewGroup) {

    fun displayImageDetails(images: List<Image>) {
        layout.removeAllViews()

        for (image in images) {
            addImageTitle(image.title)
            addImage(image.url)
            addImageDescription(image.description)
            addSpacing()
        }
    }

    private fun addImageTitle(title: String) {
        val titleTextView = TextView(context)
        titleTextView.text = title
        titleTextView.typeface = Typeface.DEFAULT_BOLD
        titleTextView.textSize = 18f
        titleTextView.gravity = Gravity.CENTER

        layout.addView(titleTextView)
    }

    private fun addImage(url: String) {
        val imageView = ImageView(context)
        Glide.with(context).load(url).into(imageView) // Using Glide to load the image

        layout.addView(imageView)
    }

    private fun addImageDescription(description: String) {
        val descriptionTextView = TextView(context)
        descriptionTextView.text = description

        layout.addView(descriptionTextView)
    }

    private fun addSpacing() {
        val spacingView = View(context)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            context.resources.getDimensionPixelSize(R.dimen.spacing_small)
        )
        spacingView.layoutParams = layoutParams
        layout.addView(spacingView)
    }
}
