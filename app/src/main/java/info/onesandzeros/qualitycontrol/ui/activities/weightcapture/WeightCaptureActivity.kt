package info.onesandzeros.qualitycontrol.ui.activities.weightcapture

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.FillHeadItem
import info.onesandzeros.qualitycontrol.bluetooth.MockBluetoothService
import info.onesandzeros.qualitycontrol.databinding.ActivityWeightCaptureBinding
import info.onesandzeros.qualitycontrol.ui.activities.baseactivity.BaseActivity
import info.onesandzeros.qualitycontrol.ui.adapters.FillHeadsAdapter

class WeightCaptureActivity : BaseActivity() {

    private lateinit var binding: ActivityWeightCaptureBinding
    private lateinit var fillHeadsAdapter: FillHeadsAdapter

    private lateinit var mockBluetoothService: MockBluetoothService

    private val viewModel: WeightCaptureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWeightCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.updateWeightCheckItem(
            intent.getParcelableExtra("weight_check_item_data")!!
        )

        // Initialize the fillHeadsAdapter here with the necessary values from viewModel
        fillHeadsAdapter = FillHeadsAdapter(
            viewModel.weightCheckItem.mav,
            viewModel.weightCheckItem.lsl,
            viewModel.weightCheckItem.usl,
            getColor(R.color.warning_yellow),
            getColor(R.color.warning_red)
        )
        setSpecValues()

        binding.fillHeadsRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.fillHeadsRecyclerView.adapter = fillHeadsAdapter




        mockBluetoothService = MockBluetoothService(viewModel::handleWeight)
        mockBluetoothService.connect(viewModel.weightCheckItem.tarWt)

        binding.completeChecksButton.setOnClickListener {
            val resultIntent = Intent().apply {
                putParcelableArrayListExtra(
                    "weight_capture_data", ArrayList(viewModel.fillHeadList)
                )
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        // Observers
        viewModel.weightLiveData.observe(this) { weight ->
            Log.d("WeightCaptureActivity", "Observing weight: $weight")
            animateWeightToTarget(weight)
        }

        viewModel.fillHeadListLiveData.observe(this) { list ->
            fillHeadsAdapter.submitList(list)
        }
    }

    private fun setSpecValues() {
        binding.mavValue.text = String.format("MAV: %.1fg", viewModel.weightCheckItem.mav)
        binding.lslValue.text = String.format("LSL: %.1fg", viewModel.weightCheckItem.lsl)
        binding.tarWtValue.text = String.format("TarWt: %.1fg", viewModel.weightCheckItem.tarWt)
        binding.uslValue.text = String.format("USL: %.1fg", viewModel.weightCheckItem.usl)
    }

    private fun animateWeightToTarget(weight: Double) {
        val flyingTextView = TextView(this)
        flyingTextView.text = getString(R.string.weight_grams, weight)
        flyingTextView.textSize = 48f // start size
        flyingTextView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Toggling this visibility on/off is a hack to prevent the TextView from flashing
        // momentarily in the top left of the window
        flyingTextView.visibility = View.INVISIBLE

        // Change color based on weight and spec values
        when {
            weight < viewModel.weightCheckItem.mav -> flyingTextView.setTextColor(getColor(R.color.warning_red))
            weight in viewModel.weightCheckItem.mav..viewModel.weightCheckItem.lsl -> flyingTextView.setTextColor(
                getColor(R.color.warning_yellow)
            )

            weight > viewModel.weightCheckItem.usl -> flyingTextView.setTextColor(getColor(R.color.warning_yellow))
            else -> flyingTextView.setTextColor(getColor(R.color.black))
        }

        // Add the flyingTextView to the root layout
        (binding.root as ViewGroup).addView(flyingTextView)

        // Center flyingTextView in the screen
        flyingTextView.post {
            val centerX = (binding.root.width - flyingTextView.width) / 2f
            val centerY = (binding.root.height - flyingTextView.height) / 2f

            flyingTextView.x = centerX
            flyingTextView.y = centerY
            flyingTextView.visibility = View.VISIBLE  // Set to VISIBLE once positioned


            val targetView =
                binding.fillHeadsRecyclerView.layoutManager?.findViewByPosition(viewModel.currentFillHeadIndex) as? TextView
            if (targetView == null) {
                (binding.root as ViewGroup).removeView(flyingTextView)
                return@post
            }

            val initialTextSize = 48f
            val destinationTextSize = targetView.textSize / resources.displayMetrics.scaledDensity
            val endX = targetView.x
            val endY = targetView.y

            val moveX = ObjectAnimator.ofFloat(flyingTextView, "x", flyingTextView.x, endX)
            val moveY = ObjectAnimator.ofFloat(flyingTextView, "y", flyingTextView.y, endY)

            val scaleX = ObjectAnimator.ofFloat(
                flyingTextView, "scaleX", 1f, destinationTextSize / initialTextSize
            )
            val scaleY = ObjectAnimator.ofFloat(
                flyingTextView, "scaleY", 1f, destinationTextSize / initialTextSize
            )

            val animationSet = AnimatorSet()
            animationSet.playTogether(moveX, moveY, scaleX, scaleY)
            animationSet.duration = 2000

            animationSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    Log.d(
                        "Animation",
                        "onAnimationEnd called for fill head ${viewModel.currentFillHeadIndex}"
                    )

                    // Update the value in the RecyclerView
                    viewModel.fillHeadList[viewModel.currentFillHeadIndex] = FillHeadItem(
                        viewModel.fillHeadList[viewModel.currentFillHeadIndex].fillHead, weight
                    )
                    fillHeadsAdapter.submitList(viewModel.fillHeadList.toList()) // Make a copy to trigger updates
                    fillHeadsAdapter.notifyItemChanged(viewModel.currentFillHeadIndex)

                    // Remove the flyingTextView from the root layout
                    (binding.root as ViewGroup).removeView(flyingTextView)

                    // Increment the index for the next fill head
                    viewModel.currentFillHeadIndex++
                    Log.d(
                        "WeightCaptureActivity",
                        "Observing currentFillHeadIndex: ${viewModel.currentFillHeadIndex}"
                    )

                    // Only continue with the mock service if there are more fill heads to capture
                    if (viewModel.currentFillHeadIndex < viewModel.fillHeadList.size) {
                        mockBluetoothService.connect(viewModel.weightCheckItem.tarWt)
                    }
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
            animationSet.start()

        }
    }
}
