package com.jcinc.data.model

data class RegisterRequest(
    val first_name: String,
    val surname: String,
    val middle_name: String,
    val dob: String,
    val email: String,
    val phone: String,
    val password: String
)