package info.onesandzeros.qualitycontrol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import info.onesandzeros.qualitycontrol.databinding.FragmentSubmissionResultBinding

class SubmissionResultFragment : Fragment(R.layout.fragment_submission_result) {
    private lateinit var binding: FragmentSubmissionResultBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmissionResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val completeChecksButton = binding.completeChecksButton

        // Access the passed argument for the total number of failed checks
        val totalFailedChecks = SubmissionResultFragmentArgs.fromBundle(requireArguments()).totalFailedChecks

        // Display the total number of failed checks and success message
        binding.totalFailedChecksTextView.text = "Total Failed Checks: $totalFailedChecks"
        binding.successMessageTextView.text = "Checks have been pushed to the database."

        completeChecksButton.setOnClickListener {
            // Handle exit checks action (e.g., navigate back to previous fragment)
            findNavController().popBackStack()
        }
    }
}
