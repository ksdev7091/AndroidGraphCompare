package io.github.winschneid.graphcompare

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.winschneid.graphcompare.core.ChartScreen
import io.github.winschneid.graphcompare.core.ui.ChartPlaceholder

/** MPAndroidChart 4.0.1（MPChartCompose） による実装。各画面は今後ここから振り分ける。 */
@Composable
fun FlavorChart(screen: ChartScreen, modifier: Modifier = Modifier) {
    ChartPlaceholder(screen, modifier)
}
