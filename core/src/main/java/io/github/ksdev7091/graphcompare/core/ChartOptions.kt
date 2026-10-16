package io.github.ksdev7091.graphcompare.core

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 計測・撮影用の切り替え。ベンチマークや adb から Intent extra で渡し、MainActivity が CompositionLocal で配る。
 *
 * @property decimationEnabled MPAndroidChart 4.x の isDecimationEnabled（同じ画素の列に重なる点を間引く）。
 *   4.x の大量データの画面だけが使い、3.x と Vico には同じ機能がないので無視する。
 */
data class ChartOptions(val decimationEnabled: Boolean = false) {
    companion object {
        const val EXTRA_DECIMATION = "decimation"

        /** テーマを端末の設定によらず固定する。"dark" か "light"。なければ端末の設定に従う。 */
        const val EXTRA_THEME = "theme"
    }
}

val LocalChartOptions = staticCompositionLocalOf { ChartOptions() }
