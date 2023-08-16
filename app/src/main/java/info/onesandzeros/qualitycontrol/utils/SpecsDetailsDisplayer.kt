package info.onesandzeros.qualitycontrol.utils

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.SpecsResponse

class SpecsDetailsDisplayer(private val context: Context, private val layout: ViewGroup) {

    fun displaySpecsDetails(specsResponse: SpecsResponse, idhNumber: Int, description: String) {
        layout.removeAllViews()

        addTitle("IDH Number: $idhNumber\nDescription: $description")
        addSpacing()

        addGroupTitle("Case")
        addKeyValue("Barcode Value", specsResponse.Case.barcode_value)
        addKeyValue("Date Code Value", specsResponse.Case.date_code_value)
        addKeyValue("Tar Weight", specsResponse.Case.weight_specs?.tarWt.toString())
        addKeyValue("LSL", specsResponse.Case.weight_specs?.lsl.toString())
        addKeyValue("USL", specsResponse.Case.weight_specs?.usl.toString())
        addKeyValue("MAV", specsResponse.Case.weight_specs?.mav.toString())
        addSpacing()

        addGroupTitle("Package")
        addKeyValue("Barcode Value", specsResponse.Package.barcode_value)
        addKeyValue("Date Code Value", specsResponse.Package.date_code_value)
        addSpacing()

        addGroupTitle("Product")
        addKeyValue("Barcode Value", specsResponse.Product.barcode_value)
        addKeyValue("Date Code Value", specsResponse.Product.date_code_value)
        addKeyValue("Tar Weight", specsResponse.Product.weight_specs?.tarWt.toString())
        addKeyValue("LSL", specsResponse.Product.weight_specs?.lsl.toString())
        addKeyValue("USL", specsResponse.Product.weight_specs?.usl.toString())
        addKeyValue("MAV", specsResponse.Product.weight_specs?.mav.toString())
    }

    private fun addTitle(title: String) {
        val titleTextView = TextView(context)
        titleTextView.text = title
        titleTextView.typeface = Typeface.DEFAULT_BOLD
        titleTextView.textSize = 20f
        titleTextView.gravity = Gravity.CENTER

        layout.addView(titleTextView)
        addSpacing()
    }

    private fun addGroupTitle(title: String) {
        val groupTitleTextView = TextView(context)
        groupTitleTextView.text = title
        groupTitleTextView.typeface = Typeface.DEFAULT_BOLD
        groupTitleTextView.textSize = 18f
        groupTitleTextView.gravity = Gravity.CENTER

        layout.addView(groupTitleTextView)
        addSpacing()
    }

    private fun addKeyValue(key: String, value: String) {
        val keyValueTextView = TextView(context)
        keyValueTextView.text = "$key: $value"

        layout.addView(keyValueTextView)
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
