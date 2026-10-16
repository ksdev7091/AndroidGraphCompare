package io.github.ksdev7091.graphcompare

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import io.github.ksdev7091.graphcompare.core.ChartOptions
import io.github.ksdev7091.graphcompare.core.ChartScreen
import io.github.ksdev7091.graphcompare.core.LocalChartOptions
import io.github.ksdev7091.graphcompare.core.ui.GraphCompareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val themeOverride = when (intent.getStringExtra(ChartOptions.EXTRA_THEME)) {
            "dark" -> true
            "light" -> false
            else -> null
        }
        // テーマを固定したときは、システムバーのアイコンと背景もそれに合わせる（既定は端末の設定に従う）
        val barStyle = when (themeOverride) {
            true -> SystemBarStyle.dark(Color.TRANSPARENT)
            false -> SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            null -> SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        super.onCreate(savedInstanceState)
        // ベンチマークは Intent extra で画面を指定して直接開く
        val initialScreen = ChartScreen.fromRoute(intent.getStringExtra(ChartScreen.EXTRA_SCREEN))
        val options = ChartOptions(decimationEnabled = intent.getBooleanExtra(ChartOptions.EXTRA_DECIMATION, false))
        setContent {
            GraphCompareTheme(darkTheme = themeOverride ?: isSystemInDarkTheme()) {
                CompositionLocalProvider(LocalChartOptions provides options) {
                    GraphCompareApp(initialScreen)
                }
            }
        }
    }
}
