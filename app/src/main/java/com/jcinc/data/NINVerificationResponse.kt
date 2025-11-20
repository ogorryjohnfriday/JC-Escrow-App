package com.jcinc.data

data class NinVerificationResponse(
    val status: String,
    val message: String,
    val match: Boolean,
    val confidence: Double,
    val data: NinData?
)

data class NinData(
    val nin: String?,
    val first_name: String?,
    val last_name: String?,
    val middle_name: String?,
    val date_of_birth: String?,
    val gender: String?,
    val image: String?
)