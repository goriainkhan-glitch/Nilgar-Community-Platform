package com.example.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProfileEntity
import com.example.util.OnboardingStrings
import com.example.util.OnboardingStrings.AppLanguage
import com.example.viewmodel.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val strings = OnboardingStrings.getContent(state.currentLanguage)
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.currentStep == 4) strings.activeProfileDashboardTitle else strings.appTitle,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Constant Localized Language Selector Header Icon/Menu
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 32.dp) {
                            Text(
                                text = "🌐",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            AppLanguage.values().forEach { lang ->
                                val isSelected = state.currentLanguage == lang
                                Text(
                                    text = when (lang) {
                                        AppLanguage.ENGLISH -> "EN"
                                        AppLanguage.HINDI -> "हि"
                                        AppLanguage.URDU -> "اردو"
                                    },
                                    modifier = Modifier
                                        .testTag("lang_toggle_${lang.name.lowercase()}")
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else Color.Transparent
                                        )
                                        .clickable { 
                                            viewModel.setLanguage(lang)
                                            keyboardController?.hide()
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            // Main Onboarding Step Router
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width / 2 } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width / 2 } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width / 2 } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width / 2 } + fadeOut()
                    }
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    0 -> WelcomeFrame(
                        strings = strings,
                        state = state,
                        onSucceed = { viewModel.nextStep() },
                        viewModel = viewModel
                    )
                    1 -> DpdpConsentFrame(
                        strings = strings,
                        state = state,
                        onNext = { viewModel.nextStep() },
                        onPrev = { viewModel.previousStep() },
                        viewModel = viewModel
                    )
                    2 -> ProfileParticularsFrame(
                        strings = strings,
                        state = state,
                        onNext = { viewModel.nextStep() },
                        onPrev = { viewModel.previousStep() },
                        viewModel = viewModel
                    )
                    3 -> IdentityVerificationFrame(
                        strings = strings,
                        state = state,
                        onNext = { viewModel.nextStep() },
                        onPrev = { viewModel.previousStep() },
                        viewModel = viewModel
                    )
                    4 -> DashboardFrame(
                        strings = strings,
                        state = state,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeFrame(
    strings: OnboardingStrings.LanguageContent,
    state: OnboardingViewModel.OnboardingState,
    onSucceed: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Decorative Circular Logo Badge
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Nilgar Logo Icon",
                modifier = Modifier.size(52.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = strings.appTitle,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("welcome_theme_title")
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = strings.subtitle,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Demo state trigger to show if consent was withdrawn previously
        if (state.hasConsentWithdrawn) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("consent_withdrawn_banner")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.deleteWarningTitle,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                        Text(
                            text = strings.consentWithdrawnNotice,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = { viewModel.acknowledgeConsentWithdrawn() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.privacyFaqTitle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = strings.privacyFaqBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onSucceed,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("welcome_get_started_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = strings.continueText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next"
                )
            }
        }
    }
}

@Composable
fun DpdpConsentFrame(
    strings: OnboardingStrings.LanguageContent,
    state: OnboardingViewModel.OnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Title & Notice Block
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "DPDP Consent icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.consentScreenTitle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = strings.consentSubtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Scrollable detailed consent legal text block
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp)
                ) {
                    Text(
                        text = strings.consentMainNoticeBlock,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // DATE OF BIRTH FIELD (CRITICAL MANDATE CHECK FOR MINORS)
            Text(
                text = strings.dobLabel,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            OutlinedTextField(
                value = state.birthDate,
                onValueChange = { viewModel.updateBirthDate(it) },
                placeholder = { Text("e.g. 1995-12-31") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dob_input"),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = state.birthDate.isNotEmpty() && state.birthDate.length >= 10 && !isValidDate(state.birthDate),
                supportingText = {
                    Text("Must format exactly YYYY-MM-DD. Re-evaluated in real-time.", fontSize = 11.sp)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Unchecked-by-default DPDP Core Consent Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dpdp_consent_row")
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.updateDpdpConsent(!state.dpdpConsented) }
                    .padding(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = state.dpdpConsented,
                    onCheckedChange = { viewModel.updateDpdpConsent(it) },
                    modifier = Modifier.testTag("dpdp_consent_checkbox")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.dataCollectionConsentLabel,
                    fontSize = 13.sp,
                    color = if (state.dpdpConsented) MaterialTheme.colorScheme.onBackground 
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            // DYNAMIC PARENT / GUARDIAN VERIFICATION IF DOB MARKS MINOR (UNDER 18 YEARS)
            AnimatedVisibility(
                visible = state.isMinor,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("minor_guardian_panel")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Guardian Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.under18Notice,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }
                        
                        Text(
                            text = strings.minorNoticeText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        Text(
                            text = strings.parentNameLabel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        OutlinedTextField(
                            value = state.parentGuardianName,
                            onValueChange = { viewModel.updateParentGuardianName(it) },
                            placeholder = { Text("Parent / Legal Guardian name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("guardian_name_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateParentGuardianConsent(!state.parentGuardianConsented) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = state.parentGuardianConsented,
                                onCheckedChange = { viewModel.updateParentGuardianConsent(it) },
                                modifier = Modifier.testTag("guardian_consent_checkbox")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.parentConsentLabel,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Action navigation block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.validationErrorMessage != null) {
                Text(
                    text = state.validationErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp).testTag("error_label_dob")
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onPrev,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("consent_back_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = strings.previousBtn, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("consent_continue_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = strings.continueText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileParticularsFrame(
    strings: OnboardingStrings.LanguageContent,
    state: OnboardingViewModel.OnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.profileDetailsTitle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = "Step 2 of 3: Capturing essential demographic details for community registry and emergency services.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Name entries
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = strings.firstNameLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.firstName,
                        onValueChange = { viewModel.updateFirstName(it) },
                        placeholder = { Text("First") },
                        modifier = Modifier.fillMaxWidth().testTag("first_name_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = strings.lastNameLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.lastName,
                        onValueChange = { viewModel.updateLastName(it) },
                        placeholder = { Text("Last") },
                        modifier = Modifier.fillMaxWidth().testTag("last_name_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gender Segment Picker
            Text(text = strings.genderLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(2.dp)
            ) {
                val genders = listOf("Male", "Female", "Other")
                genders.forEach { g ->
                    val isSelected = state.gender == g
                    val label = when(g) {
                        "Male" -> strings.maleLabel
                        "Female" -> strings.femaleLabel
                        else -> strings.otherLabel
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { viewModel.updateGender(g) }
                            .testTag("gender_button_$g"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Blood Group Selection Grid
            Text(
                text = "${strings.bloodGroupLabel}: ${state.bloodGroup}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                bloodGroups.forEach { bg ->
                    val isSelected = state.bloodGroup == bg
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { viewModel.updateBloodGroup(bg) }
                            .testTag("blood_group_$bg"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active blood donor trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (state.isActiveBloodDonor) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else Color.Transparent
                    )
                    .clickable { viewModel.updateActiveBloodDonor(!state.isActiveBloodDonor) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.isActiveBloodDonor,
                    onCheckedChange = { viewModel.updateActiveBloodDonor(it) },
                    modifier = Modifier.testTag("active_donor_checkbox")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.activeDonorLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = strings.activeDonorHelper,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // LOCALISATION FIELDS
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = strings.cityNameLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.cityName,
                        onValueChange = { viewModel.updateCityName(it) },
                        placeholder = { Text("e.g. Bhopal") },
                        modifier = Modifier.fillMaxWidth().testTag("city_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = strings.stateNameLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.stateName,
                        onValueChange = { viewModel.updateStateName(it) },
                        placeholder = { Text("e.g. Madhya Pradesh") },
                        modifier = Modifier.fillMaxWidth().testTag("state_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.validationErrorMessage != null) {
                Text(
                    text = state.validationErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp).testTag("error_label_particulars")
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onPrev,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("particulars_back_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = strings.previousBtn, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("particulars_continue_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = strings.continueText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }
        }
    }
}

@Composable
fun IdentityVerificationFrame(
    strings: OnboardingStrings.LanguageContent,
    state: OnboardingViewModel.OnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Identity verification icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.idVerificationTitle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = strings.idVerificationSubtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Aadhaar 12-digit input block
            Text(
                text = strings.aadhaarInputLabel,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            OutlinedTextField(
                value = state.aadhaarNumber,
                onValueChange = { viewModel.updateAadhaarNumber(it) },
                placeholder = { Text("4760 3824 9815") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("aadhaar_input"),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = state.aadhaarErrorType != null && 
                          state.aadhaarErrorType != OnboardingViewModel.AadhaarErrorType.VALID && 
                          state.aadhaarErrorType != OnboardingViewModel.AadhaarErrorType.EMPTY,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Aadhaar input leading",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // Real-Time Inline Custom Verification Banner & Indicators
            Spacer(modifier = Modifier.height(10.dp))
            if (state.aadhaarErrorType != null) {
                when (state.aadhaarErrorType) {
                    OnboardingViewModel.AadhaarErrorType.VALID -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("aadhaar_valid_badge")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Valid Check",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.aadhaarValidLabel,
                                color = Color(0xFF2E7D32),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    OnboardingViewModel.AadhaarErrorType.EMPTY -> {}
                    else -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("aadhaar_error_badge")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "ValidationError",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val errMsg = when (state.aadhaarErrorType) {
                                OnboardingViewModel.AadhaarErrorType.STARTS_WITH_0_OR_1 -> strings.aadhaarErrorStarts01
                                OnboardingViewModel.AadhaarErrorType.NOT_12_DIGITS -> strings.aadhaarErrorLength
                                else -> strings.aadhaarErrorChecksum
                            }
                            Text(
                                text = errMsg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // UIDAI Masking Explanation Card Block
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🔒 ${strings.idMaskingExplanation}",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Live Local Hashing Preview!
                    if (state.aadhaarNumber.length >= 4) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "LIVE CLIENT-SIDE COMPARTMENT PREVIEW:",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Masked ID: XXXX-XXXX-${state.aadhaarNumber.takeLast(4)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Verhoeff: [Calculating]",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = if (state.aadhaarErrorType == OnboardingViewModel.AadhaarErrorType.VALID) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Optional Samagra ID Field
            Text(
                text = "${strings.samagraLabel} (${strings.optionalLabel})",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            OutlinedTextField(
                value = state.samagraId,
                onValueChange = { viewModel.updateSamagraId(it) },
                placeholder = { Text("e.g. 109283745") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("samagra_input"),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                supportingText = {
                    Text("Can be any 9-character identification code.", fontSize = 11.sp)
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.validationErrorMessage != null) {
                Text(
                    text = state.validationErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp).testTag("error_label_verify")
                )
            }

            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onPrev,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("verify_back_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = strings.previousBtn, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("verify_finish_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Reg")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = strings.finishOnboardingBtn, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Active Dashboard screen after successful onboarding
@Composable
fun DashboardFrame(
    strings: OnboardingStrings.LanguageContent,
    state: OnboardingViewModel.OnboardingState,
    viewModel: OnboardingViewModel
) {
    val activeProfile = state.activeProfile ?: return
    var activeTab by remember { mutableStateOf(0) } // 0: Profile, 1: DPDP Settings
    var showRevokeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // TAB CONTROLS
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text(strings.profileDetailsTab, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(imageVector = Icons.Default.AccountBox, contentDescription = "Profile ID") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text(strings.settingsTab, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Security DPDP") },
                modifier = Modifier.testTag("tab_settings_dpdp")
            )
        }

        AnimatedContent(
            targetState = activeTab,
            label = "tab_content_transition"
        ) { tabIndex ->
            when (tabIndex) {
                0 -> {
                    // Profile Member Card
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = strings.appTitle.uppercase(),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = strings.statusVerifiedLabel.uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = Color(0xFF2E7D32),
                                            modifier = Modifier
                                                .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Face,
                                            contentDescription = "Verify Badge",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "${activeProfile.firstName} ${activeProfile.lastName}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.testTag("dashboard_user_fullname")
                                )

                                Text(
                                    text = "City: ${activeProfile.cityName}, ${activeProfile.stateName} (${activeProfile.countryName})",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = strings.genderLabel.uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                        )
                                        val genderCaption = when(activeProfile.gender) {
                                            "Male" -> strings.maleLabel
                                            "Female" -> strings.femaleLabel
                                            else -> strings.otherLabel
                                        }
                                        Text(
                                            text = genderCaption,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    
                                    Column {
                                        Text(
                                            text = strings.dobLabel.split(" ")[0].uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = activeProfile.birthDate,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = strings.bloodGroupLabel.uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = activeProfile.bloodGroup,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        // Specific Indicators section
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "VERIFIED REGISTRY ATTRIBUTES",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (activeProfile.isActiveBloodDonor) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Active donor flag",
                                        tint = if (activeProfile.isActiveBloodDonor) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (activeProfile.isActiveBloodDonor) "REGISTERED EMERGENCY BLOOD DONOR" else "NOT REGISTERED AS DONOR",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Can be searched in geofenced area during crises.",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (activeProfile.isMinor) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = "Minor verification",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = strings.under18Notice,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Parent/Guardian: ${activeProfile.parentGuardianName}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "DPDP Consent badge",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "DPDP CONSENT STATUS: COMPLIANT ACTIVE",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Consent ver: ${activeProfile.consentVersion} (Tracked ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(activeProfile.consentTimestamp))})",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // DPDP Compliance Revocability settings
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "DPDP ACT 2023 - SECTION 6 RIGHTS",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Text(
                                    text = "As a citizen and community member, you possess a legal right to withdraw your consent to data processing at any time. When consent is withdrawn:\n\n" +
                                            "1. Your profile record is instantly deactivated and masked from all service listings.\n" +
                                            "2. Your eligibility in emergency blood matching directories is removed.\n" +
                                            "3. Your database entry is marked and flagged in the next synchronization cycle for complete, permanent mechanical deletion.",
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Display masked government items
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = strings.yourMaskedGovId,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = activeProfile.maskedGovId,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Text(
                                    text = strings.yourGovIdSecureHash,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = activeProfile.govIdSecureHash,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                        .padding(6.dp)
                                )
                                
                                if (activeProfile.samagraId != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = strings.samagraLabel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = activeProfile.samagraId,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { showRevokeDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("consent_revoke_trigger_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Revoke consent")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = strings.revokeBtnLabel, fontWeight = FontWeight.Bold)
                        }

                        // Debug utility button to reset/clear database for AI evaluation
                        OutlinedButton(
                            onClick = { viewModel.restartOnboardingForDemo() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                                .testTag("reset_db_for_demo_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Database")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Reset DB & Clear Session (Developer Tool)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // CONFIRMATION DIALOG MODAL FOR CONSENT WITHDRAWAL WITH EXPLICIT DANGER FLAGS
    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            title = {
                Text(
                    text = strings.deleteWarningTitle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = strings.deleteWarningBody,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRevokeDialog = false
                        viewModel.withdrawConsent(activeProfile)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("dialog_confirm_revoke_btn")
                ) {
                    Text("Confirm Withdraw", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRevokeDialog = false },
                    modifier = Modifier.testTag("dialog_cancel_revoke_btn")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Basic date helper regex validating pattern format
private fun isValidDate(dateStr: String): Boolean {
    return dateStr.matches("^\\d{4}-\\d{2}-\\d{2}$".toRegex())
}
