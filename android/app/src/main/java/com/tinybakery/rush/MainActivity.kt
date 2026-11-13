package com.tinybakery.rush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tinybakery.rush.core.di.ServiceLocator
import com.tinybakery.rush.core.theme.AppTheme
import com.tinybakery.rush.presentation.app.AppRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ServiceLocator.initialize(applicationContext)
        setContent { AppTheme { AppRoot() } }
    }
}

