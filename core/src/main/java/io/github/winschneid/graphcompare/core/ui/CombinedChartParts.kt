package io.github.winschneid.graphcompare.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.CombinedPoint

/**
 * Compose でマーカーを描ける実装（MPAndroidChart 4.x）が使うマーカー。
 * View の MarkerView（3.x）と Vico の TextComponent も同じ色・角丸・文字サイズに揃えている。
 */
@Composable
fun MarkerBubble(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = ChartColors.markerText,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        modifier = modifier
            .background(ChartColors.markerBackground, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

/** グラフの下に出す選択中の値。グラフの選択状態を Compose の state に取り出せているかを見せる。 */
@Composable
fun CombinedSelectionText(point: CombinedPoint?, modifier: Modifier = Modifier) {
    Text(
        text = if (point == null) {
            "棒か点をタップすると、その月の値を表示します"
        } else {
            "選択中: ${ChartFormat.markerText(point).replace("\n", "　")}"
        },
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.padding(top = 12.dp),
    )
}
