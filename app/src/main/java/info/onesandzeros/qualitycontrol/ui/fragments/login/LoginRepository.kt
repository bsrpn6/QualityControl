package info.onesandzeros.qualitycontrol.ui.fragments.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import info.onesandzeros.qualitycontrol.utils.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginRepository @Inject constructor(private val firebaseAuth: FirebaseAuth) {
    suspend fun loginUser(email: String, password: String): Result<String> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            if (authResult.user != null) {
                Result.Success(authResult.user!!.email!!)
            } else {
                Result.Failure(Throwable("Authentication failed"))
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.Failure(Throwable("Invalid User"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.Failure(Throwable("Invalid Credentials"))
        } catch (e: Exception) {
            Result.Failure(Throwable("Unknown error occurred during authentication"))
        }
    }
}
