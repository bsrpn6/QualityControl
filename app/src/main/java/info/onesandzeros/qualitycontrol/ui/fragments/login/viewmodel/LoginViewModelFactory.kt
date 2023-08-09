package info.onesandzeros.qualitycontrol.ui.fragments.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.fragments.login.viewmodel.LoginViewModel
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel

class LoginViewModelFactory(private val sharedViewModel: SharedViewModel) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(sharedViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


