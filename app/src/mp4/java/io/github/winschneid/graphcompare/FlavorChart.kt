package io.github.winschneid.graphcompare

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.winschneid.graphcompare.combined.CombinedChartScreen
import io.github.winschneid.graphcompare.core.ChartScreen
import io.github.winschneid.graphcompare.core.ui.ChartPlaceholder

/** MPAndroidChart 4.0.1（MPChartCompose） による実装。 */
@Composable
fun FlavorChart(screen: ChartScreen, modifier: Modifier = Modifier) {
    when (screen) {
        ChartScreen.Combined -> CombinedChartScreen(modifier)
        else -> ChartPlaceholder(screen, modifier)
    }
}
