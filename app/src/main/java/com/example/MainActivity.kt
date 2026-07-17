package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.DeenTokRepository
import com.example.ui.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DeenTokViewModel
import com.example.viewmodel.DeenTokViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Local Database persistence and repositories
    val database = AppDatabase.getDatabase(this)
    val repository = DeenTokRepository(database.deenTokDao())
    val factory = DeenTokViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[DeenTokViewModel::class.java]

    setContent {
      MyApplicationTheme {
        MainAppScreen(viewModel = viewModel)
      }
    }
  }
}

