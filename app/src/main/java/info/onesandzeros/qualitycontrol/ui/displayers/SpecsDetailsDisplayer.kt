package info.onesandzeros.qualitycontrol.ui.displayers

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.ProductSpecsResponse

class SpecsDetailsDisplayer(private val context: Context, private val layout: ViewGroup) {

    fun displaySpecsDetails(
        specsResponse: ProductSpecsResponse
    ) {
        layout.removeAllViews()

        addTitle("IDH Number: ${specsResponse.product.productId}\nDescription: ${specsResponse.product.description}")
        addSpacing()

        addGroupTitle("Product")
        addKeyValue("Name", specsResponse.product.name)
        addKeyValue("Description", specsResponse.product.description)
        addKeyValue("Case Barcode", specsResponse.product.caseBarcode.toString())
        addKeyValue("Case SKU", specsResponse.product.caseSku)
        addKeyValue("Product Barcode", specsResponse.product.productBarcode.toString())
        addKeyValue("Consumables Per Case", specsResponse.product.consPerCase.toString())
        addKeyValue("Category", specsResponse.product.category)
        addKeyValue("Volume", specsResponse.product.volume)
        addSpacing()

        addGroupTitle("Formula")
        addKeyValue("Description", specsResponse.formula.description)
        addKeyValue("SPG", specsResponse.formula.spg.toString())
        addKeyValue("Tar Weight", specsResponse.formula.tarWt.toString())
        addKeyValue("LSL", specsResponse.formula.lsl.toString())
        addKeyValue("USL", specsResponse.formula.usl.toString())
        addKeyValue("MAV", specsResponse.formula.mav.toString())
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
