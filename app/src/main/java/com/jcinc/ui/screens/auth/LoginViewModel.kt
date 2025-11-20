package com.jcinc.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcinc.data.api.RetrofitInstance
import com.jcinc.data.setUserVerified
import com.jcinc.data.TempKycStore
import com.jcinc.data.saveLoggedInEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// -----------------------------
// UI State Model
// -----------------------------
data class LoginState(
    val status: Boolean = false,
    val message: String = "",
    val isVerified: Boolean = false,
    val loginCompleted: Boolean = false
)

// -----------------------------
// ViewModel
// -----------------------------
class LoginViewModel : ViewModel() {

    private val api = RetrofitInstance.api

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    fun loginUser(context: Context, email: String, password: String) {
        viewModelScope.launch {

            try {
                val response = api.loginUser(
                    mapOf("email" to email, "password" to password)
                )

                // ❌ SERVER DIDN'T RESPOND SUCCESSFULLY
                if (!response.isSuccessful) {
                    _loginState.value = LoginState(
                        status = false,
                        message = when (response.code()) {
                            404 -> "User not found"
                            401 -> "Invalid email or password"
                            408 -> "Request timeout. Please try again."
                            500 -> "Server error. Try again later."
                            else -> "Network error: ${response.code()}"
                        }
                    )
                    return@launch
                }

                val body = response.body()

                // ❌ Body is null
                if (body == null) {
                    _loginState.value = LoginState(
                        status = false,
                        message = "Empty server response"
                    )
                    return@launch
                }

                // ❌ Backend returned status = false
                if (!body.status) {
                    _loginState.value = LoginState(
                        status = false,
                        message = body.message   // ← server feedback
                    )
                    return@launch
                }

                // ✔ Correct backend mapping → uses "user" not "data"
                val backendData = body.user
                if (backendData == null) {
                    _loginState.value = LoginState(
                        status = false,
                        message = "Invalid server response: missing user data"
                    )
                    return@launch
                }

                // ------------------------------------
                // ✔ CHECK VERIFIED STATUS
                // backend sends: "verified": "yes" / "no"
                // ------------------------------------
                val isUserVerified = backendData.verified?.lowercase() == "yes"

                saveLoggedInEmail(context, backendData.email)
                setUserVerified(context, isUserVerified)

                TempKycStore.userEmail = backendData.email

                // ------------------------------------
                // SUCCESS RESULT
                // ------------------------------------
                _loginState.value = LoginState(
                    status = true,
                    message = body.message,
                    isVerified = isUserVerified,
                    loginCompleted = true
                )

            } catch (e: Exception) {

                // 🌐 Network timeout / no internet
                val errorMessage = when (e) {
                    is SocketTimeoutException ->
                        "Connection timeout. Check your network."
                    is UnknownHostException ->
                        "No internet connection."
                    else ->
                        "Network error: ${e.localizedMessage}"
                }

                _loginState.value = LoginState(
                    status = false,
                    message = errorMessage
                )
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState()
    }
}