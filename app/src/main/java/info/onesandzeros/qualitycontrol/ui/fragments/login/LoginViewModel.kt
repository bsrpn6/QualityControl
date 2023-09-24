package info.onesandzeros.qualitycontrol.ui.fragments.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.onesandzeros.qualitycontrol.utils.Result.Failure
import info.onesandzeros.qualitycontrol.utils.Result.Success
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository) :
    ViewModel() {

    private val _state = MutableLiveData<LoginState>(LoginState.Idle)
    val state: LiveData<LoginState> = _state

    private val _effect = MutableLiveData<LoginEffect?>()
    val effect: LiveData<LoginEffect?> = _effect

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _state.value = LoginState.Error.EmptyFields
            return
        }

        _state.value = LoginState.Loading

        viewModelScope.launch {
            when (val result = loginRepository.loginUser(email, password)) {
                is Success -> {
                    _state.value = LoginState.LoginSuccess(result.data)
                    _effect.value = LoginEffect.NavigateToNextScreen
                }

                is Failure -> {
                    val error = result.exception.message
                    _state.value = when (error) {
                        "Invalid User" -> LoginState.Error.InvalidUser
                        "Invalid Credentials" -> LoginState.Error.InvalidCredentials
                        else -> LoginState.Error.UnknownError
                    }
                }
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class LoginSuccess(val email: String) : LoginState()
    sealed class Error : LoginState() {
        object EmptyFields : Error()
        object InvalidUser : Error()
        object InvalidCredentials : Error()
        object UnknownError : Error()
    }
}

sealed class LoginEffect {
    object NavigateToNextScreen : LoginEffect()
}
