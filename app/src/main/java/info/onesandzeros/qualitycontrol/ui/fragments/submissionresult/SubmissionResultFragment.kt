package info.onesandzeros.qualitycontrol.ui.fragments.submissionresult

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.databinding.FragmentSubmissionResultBinding
import info.onesandzeros.qualitycontrol.ui.displayers.FailedCheckDetailsDisplayer
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel

class SubmissionResultFragment : Fragment(R.layout.fragment_submission_result) {
    private lateinit var binding: FragmentSubmissionResultBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSubmissionResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val viewModel: SubmissionResultViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val completeChecksButton = binding.completeChecksButton
        val args: SubmissionResultFragmentArgs by navArgs()

        viewModel.resultState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SubmissionResultViewModel.ResultState.Failure -> {
                    binding.logoImageView.setImageResource(R.drawable.ic_failure)
                    binding.resultMessageTextView.text =
                        "Total Failed Checks: ${state.numberOfFailedChecks}"
                    val failedCheckDetailsDisplayer =
                        FailedCheckDetailsDisplayer(requireContext(), binding.failedChecksLayout)
                    failedCheckDetailsDisplayer.displayFailedCheckDetails(args.totalFailedChecks)
                }

                SubmissionResultViewModel.ResultState.Success -> {
                    binding.logoImageView.setImageResource(R.drawable.ic_complete)
                    binding.resultMessageTextView.text = "SUCCESS!! No checks failed."
                }
            }
            binding.successMessageTextView.text = "Checks have been pushed to the database."
        }

        viewModel.processCheckResults(args.totalFailedChecks)

        completeChecksButton.setOnClickListener {
            findNavController().navigate(R.id.action_submissionResultFragment_to_checkSetupFragment)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        // Clear the data in the SharedViewModel after a successful save
        sharedViewModel.clearDataSaveUserAndID()
    }
}
