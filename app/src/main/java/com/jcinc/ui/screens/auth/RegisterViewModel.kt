package com.jcinc.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcinc.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterState(val status: Boolean, val message: String)

class RegisterViewModel : ViewModel() {

    private val _registerState = MutableStateFlow(RegisterState(false, ""))
    val registerState = _registerState.asStateFlow()

    fun registerUser(firstName: String, surname: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.registerUser(
                    mapOf(
                        "first_name" to firstName,
                        "surname" to surname,
                        "email" to email,
                        "phone" to phone,
                        "password" to password
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    _registerState.value = RegisterState(response.body()!!.status, response.body()!!.message)
                } else {
                    _registerState.value = RegisterState(false, "Server error")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState(false, "Error: ${e.message}")
            }
        }
    }
}