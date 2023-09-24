package info.onesandzeros.qualitycontrol.ui.displayers

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.utils.StringUtils


class FailedCheckDetailsDisplayer(private val context: Context, private val layout: ViewGroup) {
    private var currentCheckType: String? = null

    fun displayFailedCheckDetails(totalFailedChecks: Array<CheckItem>) {
        layout.removeAllViews()

        // Sort the totalFailedChecks by checkType
        val sortedFailedChecks = totalFailedChecks.sortedBy { it.section }

        for (checkItem in sortedFailedChecks) {
            val checkType = checkItem.section

            // Add a title for each new checkType group
            if (checkType != currentCheckType) {
                addGroupTitle(checkType)
                currentCheckType = checkType
            }

            addCheckTitle(checkItem.title)
            addExpectedValue("Expected Value: ${checkItem.expectedValue}")
            addRecordedValue("Recorded: ${checkItem.result}")
            addSpacing()
        }
    }

    private fun addGroupTitle(title: String) {
        val groupTitleTextView = TextView(context)
        groupTitleTextView.text = StringUtils.formatTabText(title)
        groupTitleTextView.typeface = Typeface.DEFAULT_BOLD
        groupTitleTextView.textSize = 18f // Update the text size as desired
        groupTitleTextView.gravity = Gravity.CENTER

        // Add some spacing before each group title
        val spacingView = View(context)
        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            context.resources.getDimensionPixelSize(R.dimen.spacing_small)
        )
        spacingView.layoutParams = layoutParams

        layout.addView(groupTitleTextView)
        layout.addView(spacingView)
    }

    private fun addCheckTitle(title: String) {
        val checkTitleTextView = TextView(context)
        checkTitleTextView.typeface = Typeface.DEFAULT_BOLD
        checkTitleTextView.text = title

        layout.addView(checkTitleTextView)
    }

    private fun addExpectedValue(expectedValue: String) {
        val valueTextView = TextView(context)
        valueTextView.text = expectedValue
        layout.addView(valueTextView)
    }

    private fun addRecordedValue(recordedValue: String) {
        val resultTextView = TextView(context)
        resultTextView.text = recordedValue
        layout.addView(resultTextView)
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
