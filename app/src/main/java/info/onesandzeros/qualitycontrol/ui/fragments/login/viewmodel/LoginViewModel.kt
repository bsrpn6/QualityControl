package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.fragments.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel

class LoginViewModel(private val sharedViewModel: SharedViewModel) : ViewModel() {

    private val _state = MutableLiveData<LoginState>(LoginState.Idle)
    val state: LiveData<LoginState> = _state

    private val _effect = MutableLiveData<LoginEffect?>()
    val effect: LiveData<LoginEffect?> = _effect

    fun submitAction(action: LoginAction) {
        when (action) {
            is LoginAction.Login -> {
                if (action.email.isEmpty() || action.password.isEmpty()) {
                    _state.value = LoginState.Error.EmptyFields
                    return
                }

                _state.value = LoginState.Loading

                FirebaseAuth.getInstance().signInWithEmailAndPassword(action.email, action.password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            sharedViewModel.usernameLiveData.value = action.email
                            _state.value = LoginState.LoginSuccess
                            _effect.value = LoginEffect.NavigateToNextScreen
                        } else {
                            val error = task.exception
                            _state.value = when (error) {
                                is FirebaseAuthInvalidUserException -> LoginState.Error.InvalidUser
                                is FirebaseAuthInvalidCredentialsException -> LoginState.Error.InvalidCredentials
                                else -> LoginState.Error.UnknownError
                            }
                        }
                    }
            }
        }
    }
}
