package com.jcinc.data

data class LivenessResponse(
    val status: String,
    val message: String,
    val data: LivenessData?
)

data class LivenessData(
    val liveness: Boolean?,
    val confidence: Double?,
    val face_id: String?,
    val session_id: String?
)