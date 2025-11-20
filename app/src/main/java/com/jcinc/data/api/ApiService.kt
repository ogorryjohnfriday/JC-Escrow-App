package com.jcinc.data.api

import com.jcinc.data.LivenessResponse
import com.jcinc.data.NinVerificationResponse
import com.jcinc.data.BvnVerificationResponse
import com.jcinc.data.model.FinalVerifyResponse
import com.jcinc.data.model.GenerateSessionResponse
import com.jcinc.data.model.RegisterResponse
import com.jcinc.data.model.GenericResponse
import com.jcinc.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    // REGISTER
    @POST("register.php")
    suspend fun registerUser(
        @Body data: Map<String, String>
    ): Response<RegisterResponse>

    // SEND OTP
    @POST("send_otp.php")
    suspend fun sendOtp(
        @Body data: Map<String, String>
    ): Response<GenericResponse>

    // VERIFY OTP
    @POST("verify_otp.php")
    suspend fun verifyOtp(
        @Body data: Map<String, String>
    ): Response<GenericResponse>

    // LOGIN
    @POST("login.php")
    suspend fun loginUser(
        @Body data: Map<String, String>
    ): Response<LoginResponse>

    // OPTIONAL - not being used
    @POST("generate_liveness_session.php")
    suspend fun generateLivenessSession(
        @Body data: Map<String, String>
    ): Response<GenerateSessionResponse>

    // LIVENESS CHECK (matches your verify_liveness.php)
    @POST("verify_liveness.php")
    suspend fun verifyLiveness(
        @Body body: Map<String, String>   // requires { "image": BASE64 }
    ): Response<LivenessResponse>

    // NIN + SELFIE
    @POST("verify_nin.php")
    suspend fun verifyNinWithLiveness(
        @Body data: Map<String, String>   // requires { "nin": "...", "selfie_image": "..."}
    ): Response<NinVerificationResponse>

    // BVN + SELFIE
    @POST("verify_bvn.php")
    suspend fun verifyBvnWithLiveness(
        @Body data: Map<String, String>   // requires { "bvn": "...", "selfie_image": "..."}
    ): Response<BvnVerificationResponse>

    // FINAL KYC UPDATE
    // Your PHP expects JSON but incorrectly uses $_POST. We still send JSON.
    @Headers("Content-Type: application/json")
    @POST("final_verify.php")
    suspend fun finalKycUpdate(
        @Body data: Map<String, String>
    ): Response<FinalVerifyResponse>
}