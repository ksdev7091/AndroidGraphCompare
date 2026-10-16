package io.github.ksdev7091.graphcompare

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ksdev7091.graphcompare.bar.BarChartScreen
import io.github.ksdev7091.graphcompare.combined.CombinedChartScreen
import io.github.ksdev7091.graphcompare.core.ChartScreen
import io.github.ksdev7091.graphcompare.line.LineChartScreen
import io.github.ksdev7091.graphcompare.performance.LargeDataScreen
import io.github.ksdev7091.graphcompare.performance.LazyListScreen
import io.github.ksdev7091.graphcompare.performance.UpdateAnimationScreen
import io.github.ksdev7091.graphcompare.pie.PieChartScreen

/** MPAndroidChart 4.0.1（MPChartCompose） による実装。 */
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
