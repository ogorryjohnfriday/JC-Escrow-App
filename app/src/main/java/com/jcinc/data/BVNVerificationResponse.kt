package com.jcinc.data

data class BvnVerificationResponse(
    val status: String,
    val message: String,
    val match: Boolean,
    val confidence: Double,
    val data: BvnData?
)

data class BvnData(
    val bvn: String?,
    val first_name: String?,
    val last_name: String?,
    val middle_name: String?,
    val date_of_birth: String?,
    val gender: String?,
    val image: String?
)