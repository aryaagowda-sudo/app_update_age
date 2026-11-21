package com.example.appup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.appup.notification.AppGuardMessagingService
import com.example.appup.notification.NotificationHelper
import com.example.appup.ui.AppGuardApp
import com.example.appup.ui.AppGuardViewModel
import com.example.appup.ui.theme.AppUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannels()
        notificationHelper.requestNotificationPermission(this)
        AppGuardMessagingService.initializeFirebaseSilently(this)

        val viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[AppGuardViewModel::class.java]

        setContent {
            AppUpTheme {
                AppGuardApp(viewModel = viewModel)
            }
        }
    }
}
