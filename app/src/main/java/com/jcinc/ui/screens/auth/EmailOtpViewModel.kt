package com.jcinc.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcinc.data.api.RetrofitInstance
import com.jcinc.data.model.GenericResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

// ✅ Represents UI state
data class OtpState(
    val status: Boolean = false,
    val message: String = ""
)

class EmailOtpViewModel : ViewModel() {

    private val otpApi = RetrofitInstance.api

    private val _otpState = MutableStateFlow(OtpState())
    val otpState = _otpState.asStateFlow()

    // ✅ Send or Resend OTP
    fun resendOtp(email: String) {
        viewModelScope.launch {
            try {
                val response: Response<GenericResponse> = otpApi.sendOtp(mapOf("email" to email))
                val body = response.body()
                _otpState.value = OtpState(
                    status = body?.status ?: false,
                    message = body?.message ?: "Failed to send OTP"
                )
            } catch (e: Exception) {
                _otpState.value = OtpState(false, "Network error: ${e.localizedMessage}")
            }
        }
    }

    // ✅ Verify OTP
    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            try {
                val response: Response<GenericResponse> = otpApi.verifyOtp(mapOf("email" to email, "otp" to otp))
                val body = response.body()
                _otpState.value = OtpState(
                    status = body?.status ?: false,
                    message = body?.message ?: "Verification failed"
                )
            } catch (e: Exception) {
                _otpState.value = OtpState(false, "Network error: ${e.localizedMessage}")
            }
        }
    }

    // ✅ Reset UI State
    fun resetState() {
        _otpState.value = OtpState()
    }
}