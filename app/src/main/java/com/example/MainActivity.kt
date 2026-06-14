package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.ProfileRepository
import com.example.ui.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.OnboardingViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room details securely
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = ProfileRepository(database.profileDao())
    val factory = OnboardingViewModel.Factory(application, repository)
    val viewModel = ViewModelProvider(this, factory)[OnboardingViewModel::class.java]

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          OnboardingScreen(viewModel = viewModel)
        }
      }
    }
  }
}


