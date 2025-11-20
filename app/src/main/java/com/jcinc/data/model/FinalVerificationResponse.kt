package com.jcinc.data.model

data class FinalVerifyResponse(
    val status: String,
    val message: String,
    val name_match: Boolean
)