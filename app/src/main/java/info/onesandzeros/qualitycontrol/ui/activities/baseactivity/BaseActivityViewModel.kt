package info.onesandzeros.qualitycontrol.ui.activities.baseactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import info.onesandzeros.qualitycontrol.constants.Constants.TIMEOUT_INTERVAL
import javax.inject.Inject


@HiltViewModel
class BaseActivityViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    // LiveData to represent the authentication status of the user
    val isUserAuthenticated: MutableLiveData<Boolean> = MutableLiveData()

    private val _logoutEvent = MutableLiveData<Unit>()
    val logoutEvent: LiveData<Unit> get() = _logoutEvent

    init {
        checkUserLoginStatus()
    }

    fun checkUserLoginStatus() {
        val user = firebaseAuth.currentUser
        isUserAuthenticated.postValue(user != null)
    }

    fun checkInactivity(lastInteractionTime: Long) {
        if (System.currentTimeMillis() - lastInteractionTime > TIMEOUT_INTERVAL) {
            logout()
            _logoutEvent.value = Unit
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        isUserAuthenticated.postValue(false)
    }
}
