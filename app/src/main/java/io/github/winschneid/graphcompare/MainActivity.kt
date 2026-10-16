package io.github.winschneid.graphcompare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import io.github.winschneid.graphcompare.core.ChartOptions
import io.github.winschneid.graphcompare.core.ChartScreen
import io.github.winschneid.graphcompare.core.LocalChartOptions
import io.github.winschneid.graphcompare.core.ui.GraphCompareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // ベンチマークは Intent extra で画面を指定して直接開く
        val initialScreen = ChartScreen.fromRoute(intent.getStringExtra(ChartScreen.EXTRA_SCREEN))
        val options = ChartOptions(decimationEnabled = intent.getBooleanExtra(ChartOptions.EXTRA_DECIMATION, false))
        setContent {
            GraphCompareTheme {
                CompositionLocalProvider(LocalChartOptions provides options) {
                    GraphCompareApp(initialScreen)
                }
            }
        }
    }
}
