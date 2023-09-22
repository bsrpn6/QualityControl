package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.fragments.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.databinding.FragmentLoginBinding
import info.onesandzeros.qualitycontrol.ui.fragments.login.viewmodel.LoginEffect
import info.onesandzeros.qualitycontrol.ui.fragments.login.viewmodel.LoginState
import info.onesandzeros.qualitycontrol.ui.fragments.login.viewmodel.LoginViewModel
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels() // Using viewModels() here as this ViewModel is shared across fragments in the same activity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        // Text change listener for usernameEditText to toggle clearUsernameButton visibility
        binding.usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearUsernameButton.visibility = if (s.isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        })

        // Click listener for clearUsernameButton to clear the usernameEditText
        binding.clearUsernameButton.setOnClickListener {
            binding.usernameEditText.text.clear()
        }


        binding.loginButton.setOnClickListener {
            val email = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            loginViewModel.login(email, password)
        }
    }

    private fun setupObservers() {
        // Observe sharedViewModel's usernameLiveData to populate the usernameEditText
        sharedViewModel.usernameLiveData.observe(viewLifecycleOwner) { email ->
            binding.usernameEditText.setText(email)
        }


        loginViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                }

                is LoginState.LoginSuccess -> {
                    sharedViewModel.usernameLiveData.value = state.email
                    binding.loadingProgressBar.visibility = View.GONE
                }

                is LoginState.Error -> {
                    val message = when (state) {
                        LoginState.Error.EmptyFields -> "Please enter email and password."
                        LoginState.Error.InvalidUser -> "Invalid user, please register."
                        LoginState.Error.InvalidCredentials -> "Invalid email or password."
                        LoginState.Error.UnknownError -> "Login failed. Please try again later."
                    }
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }

        loginViewModel.effect.observe(viewLifecycleOwner) { effect ->
            when (effect) {
                is LoginEffect.NavigateToNextScreen -> {
                    findNavController().navigate(R.id.action_loginFragment_to_checkSetupFragment)
                }

                null -> {}  // Do nothing for null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}