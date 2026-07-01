package com.erdevelopments.powerpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.data.prefs.PrefsRepository
import com.erdevelopments.powerpath.data.prefs.ThemeMode
import com.erdevelopments.powerpath.ui.navigation.PowerPathAppRoot
import com.erdevelopments.powerpath.ui.theme.PowerPathTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: PrefsRepository

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val themeMode by prefs.themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)
            PowerPathTheme(themeMode = themeMode) {
                PowerPathAppRoot()
            }
        }
    }
}
