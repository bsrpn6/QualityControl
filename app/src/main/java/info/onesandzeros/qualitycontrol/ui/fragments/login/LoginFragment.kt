package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.fragments.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.databinding.FragmentLoginBinding
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.fragments.login.viewmodel.LoginAction
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.fragments.login.viewmodel.LoginEffect
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.fragments.login.viewmodel.LoginState
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.fragments.login.viewmodel.LoginViewModel
import info.onesandzeros.qualitycontrol.ui.fragments.login.viewmodel.LoginViewModelFactory
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel


@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        viewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(sharedViewModel)
        )[LoginViewModel::class.java]

        binding.usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.clearUsernameButton.visibility = if (s.isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        })

        sharedViewModel.usernameLiveData.observe(viewLifecycleOwner) { email ->
            binding.usernameEditText.setText(email)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                LoginState.Loading -> {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                }

                LoginState.LoginSuccess -> {
                    binding.loadingProgressBar.visibility = View.GONE
                }

                is LoginState.Error -> {
                    // Handle different error states
                    val message = when (state) {
                        LoginState.Error.EmptyFields -> "Please enter email and password."
                        LoginState.Error.InvalidUser -> "Invalid user, please register."
                        LoginState.Error.InvalidCredentials -> "Invalid email or password."
                        LoginState.Error.UnknownError -> "Login failed. Please try again later."
                    }
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }

                else -> {
                    // Handle idle state if needed
                    binding.loadingProgressBar.visibility = View.GONE
                }
            }
        }

        viewModel.effect.observe(viewLifecycleOwner) { effect ->
            when (effect) {
                LoginEffect.NavigateToNextScreen -> {
                    findNavController().navigate(R.id.action_loginFragment_to_checkSetupFragment)
                }

                null -> {} // Do nothing for null
            }
        }

        binding.loginButton.setOnClickListener {
            val email = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            viewModel.submitAction(LoginAction.Login(email, password))
        }

        binding.clearUsernameButton.setOnClickListener {
            binding.usernameEditText.text.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


