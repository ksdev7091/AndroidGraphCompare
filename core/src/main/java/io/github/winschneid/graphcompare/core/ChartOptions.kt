package io.github.winschneid.graphcompare.core

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 計測用の切り替え。ベンチマークから Intent extra で渡し、MainActivity が CompositionLocal で配る。
 *
 * @property decimationEnabled MPAndroidChart 4.x の isDecimationEnabled（同じ画素の列に重なる点を間引く）。
 *   4.x の大量データの画面だけが使い、3.x と Vico には同じ機能がないので無視する。
 */
data class ChartOptions(val decimationEnabled: Boolean = false) {
    companion object {
        const val EXTRA_DECIMATION = "decimation"
    }
}

val LocalChartOptions = staticCompositionLocalOf { ChartOptions() }
