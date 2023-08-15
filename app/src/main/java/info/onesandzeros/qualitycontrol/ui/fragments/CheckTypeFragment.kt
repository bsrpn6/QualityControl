package info.onesandzeros.qualitycontrol.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultRegistry
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.databinding.FragmentCheckTypeBinding
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.adapters.ChecksAdapter
import info.onesandzeros.qualitycontrol.utils.BarcodeScannerUtil
import info.onesandzeros.qualitycontrol.utils.DateCodeScannerUtil
import info.onesandzeros.qualitycontrol.utils.WeightCaptureUtil

class CheckTypeFragment : Fragment(R.layout.fragment_check_type) {

    private lateinit var binding: FragmentCheckTypeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCheckTypeBinding.bind(view)

        // Get the list of checks for this check type from arguments
        val checkItems: List<CheckItem> =
            requireArguments().getParcelableArrayList(ARG_CHECK_ITEMS) ?: emptyList()

        // Create the info.onesandzeros.qualitycontrol.utils.BarcodeScannerUtil instance and pass it to the ChecksAdapter
        val barcodeScannerUtil =
            BarcodeScannerUtil(requireActivity(), requireActivity().activityResultRegistry)
        val datecodeScannerUtil =
            DateCodeScannerUtil(requireActivity(), requireActivity().activityResultRegistry)
        val weightCaptureUtil =
            WeightCaptureUtil(requireActivity(), requireActivity().activityResultRegistry)
        val checksAdapter =
            ChecksAdapter(checkItems, barcodeScannerUtil, datecodeScannerUtil, weightCaptureUtil)


        // Set up the RecyclerView with the ChecksAdapter for this check type
        val checksRecyclerView = binding.checksRecyclerView
        checksRecyclerView.adapter = checksAdapter
        checksRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        dividerItemDecoration.setDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.divider
            )!!
        )
        checksRecyclerView.addItemDecoration(dividerItemDecoration)
    }

    companion object {
        private const val ARG_CHECK_ITEMS = "arg_check_items"
        private const val ARG_ACTIVITY_RESULT_REGISTRY = "arg_activity_result_registry"

        fun newInstance(
            checkItems: List<CheckItem>,
            activityResultRegistry: ActivityResultRegistry
        ): CheckTypeFragment {
            val fragment = CheckTypeFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_CHECK_ITEMS, ArrayList(checkItems))
            fragment.arguments = args
            return fragment
        }
    }
}
