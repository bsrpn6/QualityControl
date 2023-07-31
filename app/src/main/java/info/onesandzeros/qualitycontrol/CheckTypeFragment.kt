package info.onesandzeros.qualitycontrol

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import info.onesandzeros.qualitycontrol.databinding.FragmentCheckTypeBinding

class CheckTypeFragment : Fragment(R.layout.fragment_check_type) {

    private lateinit var binding: FragmentCheckTypeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCheckTypeBinding.bind(view)

        // Get the list of checks for this check type from arguments
        val checkItems: List<CheckItem> = requireArguments().getParcelableArrayList(ARG_CHECK_ITEMS) ?: emptyList()

        // Set up the RecyclerView with the ChecksAdapter for this check type
        val checksRecyclerView = binding.checksRecyclerView
        val checksAdapter = ChecksAdapter(checkItems)
        checksRecyclerView.adapter = checksAdapter
        checksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    companion object {
        private const val ARG_CHECK_ITEMS = "arg_check_items"

        fun newInstance(checkItems: List<CheckItem>): CheckTypeFragment {
            val fragment = CheckTypeFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_CHECK_ITEMS, ArrayList(checkItems))
            fragment.arguments = args
            return fragment
        }
    }
}
