package info.onesandzeros.qualitycontrol.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import info.onesandzeros.qualitycontrol.api.models.FillHeadItem
import info.onesandzeros.qualitycontrol.api.models.WeightCheckItem
import info.onesandzeros.qualitycontrol.databinding.FragmentWeightCaptureBinding
import info.onesandzeros.qualitycontrol.mock.MockBluetoothService
import info.onesandzeros.qualitycontrol.ui.adapters.FillHeadsAdapter
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel


class WeightCaptureFragment : Fragment() {
    private val viewModel: SharedViewModel by activityViewModels()
    val fillHeadsAdapter = FillHeadsAdapter()
    private var _binding: FragmentWeightCaptureBinding? = null
    private val binding get() = _binding!!

    // Define a variable to track the current fill head index
    private var currentFillHeadIndex = 0

    // Define mockBluetoothService at the class level so it can be accessed in multiple methods
    private lateinit var mockBluetoothService: MockBluetoothService

    private lateinit var weightCheckItem: WeightCheckItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: WeightCaptureFragmentArgs by navArgs()
        weightCheckItem = args.weightCheckItem!!

        // Set the layout manager to a grid with 4 columns
        binding.fillHeadsRecyclerView.layoutManager = GridLayoutManager(context, 3)

        binding.fillHeadsRecyclerView.adapter = fillHeadsAdapter

        // Initialize the fill heads
        val fillHeads = weightCheckItem.fillHeads// Get the fill heads from the shared ViewModel
        fillHeadsAdapter.submitList(fillHeads.map { FillHeadItem(it, null) })

        setSpecValues()

        // Create an instance of the MockBluetoothService and connect
        mockBluetoothService = MockBluetoothService(handler)
        mockBluetoothService.connect(weightCheckItem.tarWt)
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
            }, 4000) // 2 seconds delay

            return true
        }
    })

    private fun animateWeightToTarget(weight: Double) {
        val flyingTextView = TextView(context)
        flyingTextView.text = "$weight g"
        flyingTextView.textSize =
            binding.weightDisplay.textSize / resources.displayMetrics.scaledDensity
        flyingTextView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Add the flyingTextView to the root layout of your fragment
        (view as ViewGroup).addView(flyingTextView)

        //TODO - This math doesn't make sense but the result is close.
        // Set the exact position of weightDisplay
        flyingTextView.x =
            binding.weightDisplay.x + (binding.weightDisplay.width - flyingTextView.width) / 6
        flyingTextView.y =
            binding.weightDisplay.y + (binding.weightDisplay.height - flyingTextView.height) / 6


        val targetView =
            binding.fillHeadsRecyclerView.layoutManager?.findViewByPosition(currentFillHeadIndex)
        if (targetView == null) {
            (view as ViewGroup).removeView(flyingTextView)
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
                val fillHeadList = fillHeadsAdapter.currentList.toMutableList()
                fillHeadList[currentFillHeadIndex] =
                    FillHeadItem(fillHeadList[currentFillHeadIndex].fillHead, weight)
                fillHeadsAdapter.submitList(fillHeadList)
                fillHeadsAdapter.notifyItemChanged(currentFillHeadIndex)

                // Remove the flyingTextView from the root layout
                (view as ViewGroup).removeView(flyingTextView)

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
