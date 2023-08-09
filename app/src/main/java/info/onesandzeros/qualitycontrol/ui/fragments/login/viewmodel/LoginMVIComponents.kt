package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.fragments.login.viewmodel

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object LoginSuccess : LoginState()
    sealed class Error : LoginState() {
        object EmptyFields : Error()
        object InvalidUser : Error()
        object InvalidCredentials : Error()
        object UnknownError : Error()
    }
}

sealed class LoginAction {
    data class Login(val email: String, val password: String) : LoginAction()
}

sealed class LoginEffect {
    object NavigateToNextScreen : LoginEffect()
}
