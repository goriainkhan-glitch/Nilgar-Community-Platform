package com.example.util

import java.security.MessageDigest

object AadhaarUtils {

    // Verhoeff multiplication table (d)
    private val d = arrayOf(
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        intArrayOf(1, 2, 3, 4, 0, 6, 7, 8, 9, 5),
        intArrayOf(2, 3, 4, 0, 1, 7, 8, 9, 5, 6),
        intArrayOf(3, 4, 0, 1, 2, 8, 9, 5, 6, 7),
        intArrayOf(4, 0, 1, 2, 3, 9, 5, 6, 7, 8),
        intArrayOf(5, 9, 8, 7, 6, 0, 4, 3, 2, 1),
        intArrayOf(6, 5, 9, 8, 7, 1, 0, 4, 3, 2),
        intArrayOf(7, 6, 5, 9, 8, 2, 1, 0, 4, 3),
        intArrayOf(8, 7, 6, 5, 9, 3, 2, 1, 0, 4),
        intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
    )

    // Verhoeff permutation table (p)
    private val p = arrayOf(
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        intArrayOf(1, 5, 7, 6, 2, 8, 3, 0, 9, 4),
        intArrayOf(5, 8, 0, 3, 7, 9, 6, 1, 4, 2),
        intArrayOf(8, 9, 1, 6, 0, 4, 3, 5, 2, 7),
        intArrayOf(9, 4, 5, 3, 1, 2, 6, 8, 7, 0),
        intArrayOf(4, 2, 8, 6, 5, 7, 3, 9, 0, 1),
        intArrayOf(2, 7, 9, 3, 8, 0, 6, 4, 1, 5),
        intArrayOf(7, 0, 4, 6, 9, 1, 3, 2, 5, 8)
    )

    // Static salt used for compliance hash generation (enforcing client-side data minimisation)
    private const val STATIC_SALT = "EnforceUIDAIComplianceSalt2026_NilgarCommunity"

    /**
     * Standard Verhoeff Checksum validation helper for 12-digit Aadhaar numbers.
     * Rejects numbers starting with '0' or '1' as per standard Aadhaar specs.
     */
    fun validateVerhoeff(aadhaar: String): Boolean {
        val digitsOnly = aadhaar.replace("\\D".toRegex(), "")
        if (digitsOnly.length != 12) return false
        
        // Aadhaar numbers never start with '0' or '1'
        if (digitsOnly.startsWith('0') || digitsOnly.startsWith('1')) return false

        var c = 0
        val digits = digitsOnly.map { it.toString().toInt() }
        val reversed = digits.reversed()

        for (i in reversed.indices) {
            c = d[c][p[i % 8][reversed[i]]]
        }

        return c == 0
    }

    /**
     * Secure SHA-256 Hashing with client-side static salt for secure deduplication storage.
     */
    fun generateSecureHash(aadhaar: String): String {
        val digitsOnly = aadhaar.replace("\\D".toRegex(), "")
        val saltedInput = digitsOnly + STATIC_SALT
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(saltedInput.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Masking function converting 12-digit Aadhaar block into "XXXX-XXXX-9815".
     */
    fun maskAadhaar(aadhaar: String): String {
        val digitsOnly = aadhaar.replace("\\D".toRegex(), "")
        if (digitsOnly.length != 12) return "XXXX-XXXX-XXXX"
        val lastFour = digitsOnly.takeLast(4)
        return "XXXX-XXXX-$lastFour"
    }

    /**
     * Verification checker to see if a date of birth indicates the user is under 18 years old.
     * Handles current local date: 2026-06-14T02:22:53-07:00.
     */
    fun isMinor(birthDateStr: String): Boolean {
        // Expected format: YYYY-MM-DD
        return try {
            val parts = birthDateStr.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()

            // Current date components as of local machine time June 14, 2026
            val currentYear = 2026
            val currentMonth = 6
            val currentDay = 14

            val age = currentYear - year
            if (age < 18) {
                true
            } else if (age == 18) {
                if (month > currentMonth) {
                    true
                } else if (month == currentMonth) {
                    day > currentDay
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
