package com.trian0.viary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.trian0.viary.ui.navigation.AppNavigation
import com.trian0.viary.ui.theme.TrippyTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            viewModel.keepSplashOn.value
        }

        super.onCreate(savedInstanceState)

        setContent {
            TrippyTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}