package com.jcinc.data.model

data class GenerateSessionResponse(
    val status: String,
    val session_id: String?,
    val face_id: String?
)