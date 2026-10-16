package io.github.winschneid.graphcompare.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 系列の色。見た目の比較が公平になるよう、3 実装ともこの色を使う。
 * View ベースの実装では `toArgb()` で Int に変換して渡す。
 */
object ChartColors {
    val series: List<Color> = listOf(
        Color(0xFF2E6BE6),
        Color(0xFFF08A24),
        Color(0xFF2FA37A),
        Color(0xFFD64560),
        Color(0xFF8A5CD6),
    )

    // 3.x の MarkerView は XML なので、app/src/mp3/res/drawable/marker_background.xml にも同じ値を書いている
    val markerBackground = Color(0xE6303030)
    val markerText = Color.White
}

// 端末ごとに色が変わらないよう dynamic color は使わない
private val LightColors = lightColorScheme(primary = Color(0xFF2E6BE6))
private val DarkColors = darkColorScheme(primary = Color(0xFF8AB0FF))

@Composable
fun GraphCompareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
