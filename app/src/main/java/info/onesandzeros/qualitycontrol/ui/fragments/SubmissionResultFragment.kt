package info.onesandzeros.qualitycontrol.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.databinding.FragmentSubmissionResultBinding
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.utils.FailedCheckDetailsDisplayer
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel

class SubmissionResultFragment : Fragment(R.layout.fragment_submission_result) {
    private lateinit var binding: FragmentSubmissionResultBinding
    private lateinit var sharedViewModel: SharedViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSubmissionResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val completeChecksButton = binding.completeChecksButton

        val args: SubmissionResultFragmentArgs by navArgs()
        val totalFailedChecks: Array<CheckItem> = args.totalFailedChecks

        // Display the total number of failed checks and success message
        if (totalFailedChecks.isNotEmpty()) {
            binding.logoImageView.setImageResource(R.drawable.ic_failure)
            binding.resultMessageTextView.text = "Total Failed Checks: ${totalFailedChecks.size}"

            val failedCheckDetailsDisplayer =
                FailedCheckDetailsDisplayer(requireContext(), binding.failedChecksLayout)
            failedCheckDetailsDisplayer.displayFailedCheckDetails(totalFailedChecks)
        } else {
            binding.logoImageView.setImageResource(R.drawable.ic_complete)
            binding.resultMessageTextView.text = "SUCCCESS!! No checks failed."
        }
        binding.successMessageTextView.text = "Checks have been pushed to the database."


        completeChecksButton.setOnClickListener {
            // Handle exit checks action (e.g., navigate back to previous fragment)
            findNavController().navigate(R.id.action_submissionResultFragment_to_checkSetupFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Clear the data in the SharedViewModel after a successful save
        sharedViewModel.clearDataSaveUserAndID()
    }
}
