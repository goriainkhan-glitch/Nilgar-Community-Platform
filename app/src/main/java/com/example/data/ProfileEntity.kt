package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val gender: String, // Male, Female, Other
    val birthDate: String, // YYYY-MM-DD
    val bloodGroup: String, // A+, A-, B+, B-, O+, O-, AB+, AB-
    val isActiveBloodDonor: Boolean = false,
    val cityName: String,
    val stateName: String,
    val countryName: String = "India",
    val maskedGovId: String, // "XXXX-XXXX-4321"
    val govIdSecureHash: String, // SHA-256 Hash of Aadhaar
    val samagraId: String? = null, // Optional Samagra Member ID (9 chars)
    
    // DPDP Act Consent Tracking
    val dpdpConsented: Boolean = false,
    val consentTimestamp: Long = System.currentTimeMillis(),
    val consentVersion: String = "v1.0",
    val consentWithdrawnAt: Long? = null,
    val isVerified: Boolean = false,
    val isDiseased: Boolean = false,
    val deathDate: String? = null,
    
    // Parent/Guardian Verification for Minors under 18 years
    val isMinor: Boolean = false,
    val parentGuardianName: String? = null,
    val parentGuardianConsented: Boolean = false
)
