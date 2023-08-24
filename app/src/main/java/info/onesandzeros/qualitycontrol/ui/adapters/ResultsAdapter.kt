package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.databinding.ViewItemResultBinding
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.data.models.CheckSubmissionEntity
import info.onesandzeros.qualitycontrol.utils.StringUtils
import java.text.SimpleDateFormat
import java.util.Date

class ResultsAdapter(
    var results: List<CheckSubmissionEntity>,
    private val onMoreDetailsClick: (CheckSubmissionEntity) -> Unit
) : RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>() {

    inner class ResultViewHolder(val binding: ViewItemResultBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding =
            ViewItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val checkResult = results[position]
        with(holder.binding) {
            dateTextView.text =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(checkResult.checkStartTimestamp!!))
            usernameTextView.text = StringUtils.parseUsername(checkResult.username)
            idhNumberTextView.text = checkResult.idhNumber?.id.toString()
            failedCheckCountTextView.text = calculateFailedChecks(checkResult.checks).toString()
            if (calculateFailedChecks(checkResult.checks) == 0) {
                moreDetailsButton.visibility = View.INVISIBLE
            } else {
                moreDetailsButton.setOnClickListener { onMoreDetailsClick(checkResult) }
            }
        }
    }

    override fun getItemCount(): Int = results.size

    private fun calculateFailedChecks(checks: Map<String, List<CheckItem>>): Int {
        var failedCheckCount = 0
        for ((_, checkItems) in checks) {
            for (check in checkItems) {
                if (check.result != null && check.expectedValue != check.result) {
                    failedCheckCount++
                }
            }
        }
        return failedCheckCount
    }
}
