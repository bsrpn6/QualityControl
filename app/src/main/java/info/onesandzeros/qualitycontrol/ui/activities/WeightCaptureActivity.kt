package info.onesandzeros.qualitycontrol.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import info.onesandzeros.qualitycontrol.api.models.FillHeadItem
import info.onesandzeros.qualitycontrol.api.models.WeightCheckItem
import info.onesandzeros.qualitycontrol.databinding.ActivityWeightCaptureBinding
import info.onesandzeros.qualitycontrol.mock.MockBluetoothService
import info.onesandzeros.qualitycontrol.ui.adapters.FillHeadsAdapter

class WeightCaptureActivity : BaseActivity() {

    private lateinit var binding: ActivityWeightCaptureBinding
    private val fillHeadsAdapter = FillHeadsAdapter()
    private var currentFillHeadIndex = 0
    private lateinit var weightCheckItem: WeightCheckItem

    private lateinit var mockBluetoothService: MockBluetoothService

    private lateinit var fillHeadList: MutableList<FillHeadItem>

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        weightCheckItem = intent.getParcelableExtra("weight_check_item_data")!!
        fillHeadList = weightCheckItem.fillHeads.map { FillHeadItem(it, null) }.toMutableList()

        binding = ActivityWeightCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fillHeadsRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.fillHeadsRecyclerView.adapter = fillHeadsAdapter

        val fillHeads = weightCheckItem.fillHeads
        fillHeadsAdapter.submitList(fillHeads.map { FillHeadItem(it, null) })

        setSpecValues()

        mockBluetoothService = MockBluetoothService(handler)
        mockBluetoothService.connect(weightCheckItem.tarWt)

        binding.completeChecksButton.setOnClickListener {
            val resultIntent = Intent().apply {
                putParcelableArrayListExtra("weight_capture_data", ArrayList(fillHeadList))
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun setSpecValues() {
        binding.mavValue.text = String.format("MAV: %.1fg", weightCheckItem.mav)
        binding.lslValue.text = String.format("LSL: %.1fg", weightCheckItem.lsl)
        binding.tarWtValue.text = String.format("TarWt: %.1fg", weightCheckItem.tarWt)
        binding.uslValue.text = String.format("USL: %.1fg", weightCheckItem.usl)
    }

    private val handler: Handler = Handler(Looper.getMainLooper(), object : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            val data: Bundle = msg.data
            val weight = data.getDouble("weight")

            // Display the weight
            binding.weightDisplay.text = "$weight g"

            // Wait for 2 seconds, then process the weight
            Handler(Looper.getMainLooper()).postDelayed({
                animateWeightToTarget(weight)
            }, 2000) // 2 seconds delay

            return true
        }
    })

    private fun animateWeightToTarget(weight: Double) {
        val flyingTextView = TextView(this)
        flyingTextView.text = "$weight g"
        flyingTextView.textSize =
            binding.weightDisplay.textSize / resources.displayMetrics.scaledDensity
        flyingTextView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Add the flyingTextView to the root layout of your fragment
        (binding.root as ViewGroup).addView(flyingTextView)

        //TODO - This math doesn't make sense but the result is close.
        // Set the exact position of weightDisplay
        flyingTextView.x =
            binding.weightDisplay.x + (binding.weightDisplay.width - flyingTextView.width) / 6
        flyingTextView.y =
            binding.weightDisplay.y + (binding.weightDisplay.height - flyingTextView.height) / 6


        val targetView =
            binding.fillHeadsRecyclerView.layoutManager?.findViewByPosition(currentFillHeadIndex)
        if (targetView == null) {
            (binding.root as ViewGroup).removeView(flyingTextView)
            return
        }

        val endX = targetView.x
        val endY = targetView.y

        val animation = TranslateAnimation(0f, endX - flyingTextView.x, 0f, endY - flyingTextView.y)
        animation.duration = 500
        animation.fillAfter = true
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                // Update the value in the RecyclerView

                fillHeadList[currentFillHeadIndex] =
                    FillHeadItem(fillHeadList[currentFillHeadIndex].fillHead, weight)
                fillHeadsAdapter.submitList(fillHeadList)
                fillHeadsAdapter.notifyItemChanged(currentFillHeadIndex)

                // Remove the flyingTextView from the root layout
                (binding.root as ViewGroup).removeView(flyingTextView)

                // Increment the index for the next fill head
                currentFillHeadIndex++

                // Clear the weight display
                binding.weightDisplay.text = ""

                // Only continue with the mock service if there are more fill heads to capture
                if (currentFillHeadIndex < fillHeadList.size) {
                    mockBluetoothService.connect(weightCheckItem.tarWt)

                }
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        flyingTextView.startAnimation(animation)
    }
}
