package com.jcinc.data

/**
 * Temporary in-memory store for KYC verification steps.
 * This data is cleared after verification is completed.
 */
object TempKycStore {

    // "nin" or "bvn"
    var mode: String? = null

    // Stores the captured selfie base64 string
    var selfieBase64: String? = null

    // Stores the NIN or BVN temporarily until liveness passes
    var idNumber: String? = null

    // Stores firstname + surname to match with NIN/BVN record
    var firstName: String? = null
    var lastName: String? = null
    var userEmail: String? = null
    // Clear all after final verification
    fun clear() {
        mode = null
        selfieBase64 = null
        idNumber = null
        firstName = null
        lastName = null
    }
}