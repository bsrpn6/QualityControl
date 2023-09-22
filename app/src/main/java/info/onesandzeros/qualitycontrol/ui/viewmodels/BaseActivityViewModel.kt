package info.onesandzeros.qualitycontrol.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class BaseActivityViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    // LiveData to represent the authentication status of the user
    val isUserAuthenticated: MutableLiveData<Boolean> = MutableLiveData()

    init {
        checkUserLoginStatus()
    }

    fun checkUserLoginStatus() {
        val user = firebaseAuth.currentUser
        isUserAuthenticated.postValue(user != null)
    }

    fun logout() {
        firebaseAuth.signOut()
        isUserAuthenticated.postValue(false)
    }
}
