package com.jcinc.data.model

data class LoginResponse(
    val status: Boolean,
    val message: String,
    val user: LoginUserData?    // MUST be "user" not "data"
)

data class LoginUserData(
    val id: String,
    val email: String,
    val name: String,

    // Backend sends: "verified": "yes" / "no"
    val verified: String? = null
) {
    val isVerified: Boolean
        get() = verified?.lowercase() == "yes"
}