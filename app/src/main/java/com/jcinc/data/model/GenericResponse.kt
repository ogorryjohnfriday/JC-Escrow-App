package com.jcinc.data.model

data class GenericResponse(
    val status: Boolean,
    val message: String,
    val data: UserData? = null
)

data class UserData(
    val user_id: Int,
    val first_name: String,
    val last_name: String?,
    val email: String,
    val phone: String?,
    val token: String?,
    val requires_nin_verification: Boolean = false
)