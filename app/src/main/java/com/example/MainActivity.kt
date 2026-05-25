package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.db.StampDatabase
import com.example.data.repository.StampRepository
import com.example.ui.StampViewModel
import com.example.ui.StampViewModelFactory
import com.example.ui.screens.MainAppLayout
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val db by lazy { StampDatabase.getDatabase(this) }
    private val repository by lazy { StampRepository(db.stampDao(), db.categoryDao(), db.albumDao()) }
    
    // Modern ViewModel instantiation
    private val viewModel: StampViewModel by viewModels {
        StampViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val isDark by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppLayout(viewModel = viewModel)
                }
            }
        }
    }
}
