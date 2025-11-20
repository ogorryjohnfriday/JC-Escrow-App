package com.jcinc.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
private val USER_EMAIL_KEY = stringPreferencesKey("logged_in_email")
// -----------------------------------------------------
// 1️⃣ DataStore Instance (global extension for Context)
// -----------------------------------------------------
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

// -----------------------------------------------------
// 2️⃣ Preference Keys (all persistent KYC flags live here)
// -----------------------------------------------------
private val VERIFICATION_STATUS_KEY = stringPreferencesKey("verification_status")
private val USER_VERIFIED_KEY = booleanPreferencesKey("user_verified")
private val KYC_PROGRESS_KEY = stringPreferencesKey("kyc_progress") // optional future use

// -----------------------------------------------------
// 3️⃣ Save verification status: "pending", "success", "failed"
// -----------------------------------------------------
suspend fun saveVerificationStatus(context: Context, status: String) {
    context.dataStore.edit { prefs ->
        prefs[VERIFICATION_STATUS_KEY] = status
    }
}

// -----------------------------------------------------
// 4️⃣ Read verification status
// -----------------------------------------------------
suspend fun getVerificationStatus(context: Context): String? {
    return context.dataStore.data.map { prefs ->
        prefs[VERIFICATION_STATUS_KEY]
    }.first()
}

// -----------------------------------------------------
// 5️⃣ Save whether user is fully verified
// -----------------------------------------------------
suspend fun setUserVerified(context: Context, verified: Boolean) {
    context.dataStore.edit { prefs ->
        prefs[USER_VERIFIED_KEY] = verified
    }
}

// -----------------------------------------------------
// 6️⃣ Check if user is fully verified
// -----------------------------------------------------
suspend fun isUserVerified(context: Context): Boolean {
    return context.dataStore.data.map { prefs ->
        prefs[USER_VERIFIED_KEY] ?: false
    }.first()
}

// -----------------------------------------------------
// 7️⃣ OPTIONAL: Save KYC progress (e.g. "liveness", "nin_bvn", "final_step")
// -----------------------------------------------------
suspend fun saveKycProgress(context: Context, step: String) {
    context.dataStore.edit { prefs ->
        prefs[KYC_PROGRESS_KEY] = step
    }
}
suspend fun saveLoggedInEmail(context: Context, email: String) {
    context.dataStore.edit { prefs ->
        prefs[USER_EMAIL_KEY] = email
    }
}

suspend fun getLoggedInEmail(context: Context): String? {
    return context.dataStore.data.map { prefs ->
        prefs[USER_EMAIL_KEY]
    }.first()
}
// -----------------------------------------------------
// 8️⃣ OPTIONAL: Read KYC progress step
// -----------------------------------------------------
suspend fun getKycProgress(context: Context): String? {
    return context.dataStore.data.map { prefs ->
        prefs[KYC_PROGRESS_KEY]
    }.first()
}

// -----------------------------------------------------
// 9️⃣ Clear everything (call when user logs out or KYC completed)
// -----------------------------------------------------
suspend fun clearVerificationData(context: Context) {
    context.dataStore.edit { prefs ->
        prefs.remove(VERIFICATION_STATUS_KEY)
        prefs.remove(USER_VERIFIED_KEY)
        prefs.remove(KYC_PROGRESS_KEY)
    }
}