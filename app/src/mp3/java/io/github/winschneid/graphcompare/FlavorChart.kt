package io.github.winschneid.graphcompare

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.winschneid.graphcompare.bar.BarChartScreen
import io.github.winschneid.graphcompare.combined.CombinedChartScreen
import io.github.winschneid.graphcompare.core.ChartScreen
import io.github.winschneid.graphcompare.line.LineChartScreen
import io.github.winschneid.graphcompare.performance.LargeDataScreen
import io.github.winschneid.graphcompare.performance.LazyListScreen
import io.github.winschneid.graphcompare.performance.UpdateAnimationScreen
import io.github.winschneid.graphcompare.pie.PieChartScreen

/** MPAndroidChart 3.2.0（View を AndroidView で包む） による実装。 */
@Composable
fun FlavorChart(screen: ChartScreen, modifier: Modifier = Modifier) {
    when (screen) {
        ChartScreen.Pie -> PieChartScreen(modifier)
        ChartScreen.Bar -> BarChartScreen(modifier)
        ChartScreen.Line -> LineChartScreen(modifier)
        ChartScreen.Combined -> CombinedChartScreen(modifier)
        ChartScreen.LargeData -> LargeDataScreen(modifier)
        ChartScreen.Animation -> UpdateAnimationScreen(modifier)
        ChartScreen.LazyList -> LazyListScreen(modifier)
    }
}
