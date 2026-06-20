package com.trian0.viary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.trian0.viary.ui.theme.ViaryTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.core.graphics.toColorInt

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            viewModel.keepSplashOn.value
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.BLACK,
                darkScrim = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.BLACK,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            ViaryTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}