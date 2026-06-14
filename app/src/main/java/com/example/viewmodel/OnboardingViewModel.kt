package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ProfileEntity
import com.example.data.ProfileRepository
import com.example.util.AadhaarUtils
import com.example.util.OnboardingStrings
import com.example.util.OnboardingStrings.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OnboardingViewModel(
    application: Application,
    private val repository: ProfileRepository
) : AndroidViewModel(application) {

    // Onboarding UI State
    data class OnboardingState(
        val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
        val currentStep: Int = 0, // 0: Welcome, 1: DPDP & DOB, 2: Profile Particulars, 3: ID Verification, 4: Dashboard
        val registeredProfiles: List<ProfileEntity> = emptyList(),
        val activeProfile: ProfileEntity? = null,
        
        // Step 1: Consent & Minors Check
        val birthDate: String = "", // YYYY-MM-DD
        val dpdpConsented: Boolean = false,
        val parentGuardianName: String = "",
        val parentGuardianConsented: Boolean = false,
        val isMinor: Boolean = false,
        
        // Step 2: Personal Particulars
        val firstName: String = "",
        val lastName: String = "",
        val gender: String = "Male", // Male, Female, Other
        val bloodGroup: String = "A+", // Default
        val isActiveBloodDonor: Boolean = false,
        val cityName: String = "",
        val stateName: String = "",
        
        // Step 3: Secure Identity
        val aadhaarNumber: String = "", // 12 Digits raw
        val samagraId: String = "", // 9 Digits optional
        
        // Flow helpers
        val aadhaarErrorType: AadhaarErrorType? = null,
        val isSaving: Boolean = false,
        val hasConsentWithdrawn: Boolean = false,
        val validationErrorMessage: String? = null
    )

    enum class AadhaarErrorType {
        EMPTY,
        STARTS_WITH_0_OR_1,
        NOT_12_DIGITS,
        CHECKSUM_FAILED,
        VALID
    }

    private val _uiState = MutableStateFlow(OnboardingState())
    val uiState: StateFlow<OnboardingState> = _uiState.asStateFlow()

    init {
        // Observe database profiles to automatically log in the registered user
        viewModelScope.launch {
            repository.allProfiles.collectLatest { profiles ->
                // Filter profiles that are active and have not withdrawn consent
                val active = profiles.firstOrNull { it.consentWithdrawnAt == null }
                _uiState.value = _uiState.value.copy(
                    registeredProfiles = profiles,
                    activeProfile = active,
                    currentStep = if (active != null) 4 else _uiState.value.currentStep
                )
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _uiState.value = _uiState.value.copy(currentLanguage = lang)
        validateAadhaarRealTime()
    }

    fun nextStep() {
        val state = _uiState.value
        _uiState.value = state.copy(validationErrorMessage = null)
        
        when (state.currentStep) {
            0 -> {
                _uiState.value = state.copy(currentStep = 1)
            }
            1 -> {
                // Validate Step 1 input
                if (state.birthDate.isEmpty() || !isValidDatePickerFormat(state.birthDate)) {
                    _uiState.value = state.copy(validationErrorMessage = "Please enter a valid birth date (YYYY-MM-DD)")
                    return
                }
                
                val isUserMinor = AadhaarUtils.isMinor(state.birthDate)
                _uiState.value = state.copy(isMinor = isUserMinor)
                
                // DPDP consent check
                if (!state.dpdpConsented) {
                    _uiState.value = state.copy(validationErrorMessage = "You must agree to the DPDP consent notice to proceed.")
                    return
                }
                
                // If minor, parent guardian info is mandatory
                if (isUserMinor) {
                    if (state.parentGuardianName.trim().isEmpty()) {
                        _uiState.value = state.copy(validationErrorMessage = "Parent/Guardian name is required for minors.")
                        return
                    }
                    if (!state.parentGuardianConsented) {
                        _uiState.value = state.copy(validationErrorMessage = "Parent/Guardian consent is required.")
                        return
                    }
                }
                
                _uiState.value = _uiState.value.copy(currentStep = 2)
            }
            2 -> {
                // Validate Step 2 input
                if (state.firstName.trim().isEmpty()) {
                    _uiState.value = state.copy(validationErrorMessage = "First name is required.")
                    return
                }
                if (state.lastName.trim().isEmpty()) {
                    _uiState.value = state.copy(validationErrorMessage = "Last name is required.")
                    return
                }
                if (state.cityName.trim().isEmpty()) {
                    _uiState.value = state.copy(validationErrorMessage = "City Name is required.")
                    return
                }
                if (state.stateName.trim().isEmpty()) {
                    _uiState.value = state.copy(validationErrorMessage = "State Name is required.")
                    return
                }
                
                _uiState.value = state.copy(currentStep = 3)
            }
            3 -> {
                // Validate Aadhaar with Verhoeff
                validateAadhaarRealTime()
                val errorType = _uiState.value.aadhaarErrorType
                if (errorType != AadhaarErrorType.VALID) {
                    _uiState.value = _uiState.value.copy(
                        validationErrorMessage = getAadhaarErrorMessage(errorType, state.currentLanguage)
                    )
                    return
                }
                
                // Optional Samagra check (must be 9 chars if entered)
                val cleanSamagra = state.samagraId.trim()
                if (cleanSamagra.isNotEmpty() && cleanSamagra.length != 9) {
                    _uiState.value = _uiState.value.copy(validationErrorMessage = "Samagra Member ID must be exactly 9 characters/digits.")
                    return
                }
                
                // Auto complete and save
                completeOnboarding()
            }
        }
    }

    fun previousStep() {
        val state = _uiState.value
        if (state.currentStep > 0) {
            _uiState.value = state.copy(
                currentStep = state.currentStep - 1,
                validationErrorMessage = null
            )
        }
    }

    // Capture Inputs
    fun updateBirthDate(dateStr: String) {
        val cleanDate = dateStr.replace(" ", "") // standard clean
        val isUserMinor = if (cleanDate.length == 10 && isValidDatePickerFormat(cleanDate)) {
            AadhaarUtils.isMinor(cleanDate)
        } else {
            false
        }
        _uiState.value = _uiState.value.copy(
            birthDate = cleanDate,
            isMinor = isUserMinor,
            validationErrorMessage = null
        )
    }

    fun updateDpdpConsent(checked: Boolean) {
        _uiState.value = _uiState.value.copy(dpdpConsented = checked, validationErrorMessage = null)
    }

    fun updateParentGuardianConsent(checked: Boolean) {
        _uiState.value = _uiState.value.copy(parentGuardianConsented = checked, validationErrorMessage = null)
    }

    fun updateParentGuardianName(name: String) {
        _uiState.value = _uiState.value.copy(parentGuardianName = name, validationErrorMessage = null)
    }

    fun updateFirstName(name: String) {
        _uiState.value = _uiState.value.copy(firstName = name, validationErrorMessage = null)
    }

    fun updateLastName(name: String) {
        _uiState.value = _uiState.value.copy(lastName = name, validationErrorMessage = null)
    }

    fun updateGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateBloodGroup(group: String) {
        _uiState.value = _uiState.value.copy(bloodGroup = group)
    }

    fun updateActiveBloodDonor(active: Boolean) {
        _uiState.value = _uiState.value.copy(isActiveBloodDonor = active)
    }

    fun updateCityName(city: String) {
        _uiState.value = _uiState.value.copy(cityName = city, validationErrorMessage = null)
    }

    fun updateStateName(state: String) {
        _uiState.value = _uiState.value.copy(stateName = state, validationErrorMessage = null)
    }

    fun updateAadhaarNumber(num: String) {
        val digits = num.replace("\\s".toRegex(), "").take(12)
        _uiState.value = _uiState.value.copy(aadhaarNumber = digits, validationErrorMessage = null)
        validateAadhaarRealTime()
    }

    fun updateSamagraId(id: String) {
        val trimmed = id.trim().take(9)
        _uiState.value = _uiState.value.copy(samagraId = trimmed, validationErrorMessage = null)
    }

    private fun validateAadhaarRealTime() {
        val aadhaar = _uiState.value.aadhaarNumber
        if (aadhaar.isEmpty()) {
            _uiState.value = _uiState.value.copy(aadhaarErrorType = AadhaarErrorType.EMPTY)
            return
        }
        if (aadhaar.length != 12) {
            _uiState.value = _uiState.value.copy(aadhaarErrorType = AadhaarErrorType.NOT_12_DIGITS)
            return
        }
        if (aadhaar.startsWith("0") || aadhaar.startsWith("1")) {
            _uiState.value = _uiState.value.copy(aadhaarErrorType = AadhaarErrorType.STARTS_WITH_0_OR_1)
            return
        }
        if (!AadhaarUtils.validateVerhoeff(aadhaar)) {
            _uiState.value = _uiState.value.copy(aadhaarErrorType = AadhaarErrorType.CHECKSUM_FAILED)
            return
        }
        _uiState.value = _uiState.value.copy(aadhaarErrorType = AadhaarErrorType.VALID)
    }

    private fun completeOnboarding() {
        val state = _uiState.value
        _uiState.value = state.copy(isSaving = true)
        
        viewModelScope.launch {
            try {
                // local client side deduplication compliance hashing & masking
                val secureHash = AadhaarUtils.generateSecureHash(state.aadhaarNumber)
                val maskedId = AadhaarUtils.maskAadhaar(state.aadhaarNumber)
                
                val currentProfiles = state.registeredProfiles
                // Check if this secure hash is already registered in our local cache db!
                val isDuplicate = currentProfiles.any { it.govIdSecureHash == secureHash && it.consentWithdrawnAt == null }
                if (isDuplicate) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        validationErrorMessage = "Registration failed: A community profile is already linked with this Government ID checksum hash."
                    )
                    return@launch
                }

                val profile = ProfileEntity(
                    firstName = state.firstName.trim(),
                    lastName = state.lastName.trim(),
                    gender = state.gender,
                    birthDate = state.birthDate,
                    bloodGroup = state.bloodGroup,
                    isActiveBloodDonor = state.isActiveBloodDonor,
                    cityName = state.cityName.trim(),
                    stateName = state.stateName.trim(),
                    maskedGovId = maskedId,
                    govIdSecureHash = secureHash,
                    samagraId = if (state.samagraId.trim().isEmpty()) null else state.samagraId.trim(),
                    dpdpConsented = state.dpdpConsented,
                    consentTimestamp = System.currentTimeMillis(),
                    consentVersion = "v1.0",
                    isMinor = state.isMinor,
                    parentGuardianName = if (state.isMinor) state.parentGuardianName.trim() else null,
                    parentGuardianConsented = if (state.isMinor) state.parentGuardianConsented else false,
                    isVerified = true // Mark as verified local setup
                )

                repository.insert(profile)
                
                // Clear state inputs
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    currentStep = 4, // Success / Dashboard panel
                    birthDate = "",
                    dpdpConsented = false,
                    parentGuardianName = "",
                    parentGuardianConsented = false,
                    firstName = "",
                    lastName = "",
                    cityName = "",
                    stateName = "",
                    aadhaarNumber = "",
                    samagraId = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    validationErrorMessage = "An database error occurred: ${e.message}"
                )
            }
        }
    }

    /**
     * Withdraw Consent UI operation as per DPDP compliance directive.
     * Restricted visibility to deactivated state instantly.
     */
    fun withdrawConsent(profile: ProfileEntity) {
        viewModelScope.launch {
            val updated = profile.copy(
                consentWithdrawnAt = System.currentTimeMillis(),
                isActiveBloodDonor = false,
                isVerified = false
            )
            repository.update(updated)
            _uiState.value = _uiState.value.copy(
                hasConsentWithdrawn = true,
                currentStep = 0 // back to welcome cover with success warning
            )
        }
    }

    fun acknowledgeConsentWithdrawn() {
        _uiState.value = _uiState.value.copy(hasConsentWithdrawn = false)
    }

    fun restartOnboardingForDemo() {
        viewModelScope.launch {
            repository.clear()
            _uiState.value = OnboardingState(
                currentLanguage = _uiState.value.currentLanguage,
                currentStep = 0
            )
        }
    }

    private fun isValidDatePickerFormat(dateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.isLenient = false
            val d = sdf.parse(dateStr)
            d != null && dateStr.length == 10
        } catch (e: Exception) {
            false
        }
    }

    private fun getAadhaarErrorMessage(type: AadhaarErrorType?, lang: AppLanguage): String? {
        val strings = OnboardingStrings.getContent(lang)
        return when (type) {
            AadhaarErrorType.EMPTY -> null
            AadhaarErrorType.STARTS_WITH_0_OR_1 -> strings.aadhaarErrorStarts01
            AadhaarErrorType.NOT_12_DIGITS -> strings.aadhaarErrorLength
            AadhaarErrorType.CHECKSUM_FAILED -> strings.aadhaarErrorChecksum
            else -> null
        }
    }

    class Factory(
        private val application: Application,
        private val repository: ProfileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
                return OnboardingViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
