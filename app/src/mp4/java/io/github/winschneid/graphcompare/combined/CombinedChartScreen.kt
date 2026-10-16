package io.github.winschneid.graphcompare.combined

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.charts.CombinedChart as CombinedChartView
import com.github.mikephil.charting.compose.CombinedChart
import com.github.mikephil.charting.compose.rememberChartState
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import io.github.winschneid.graphcompare.common.applyChartSpec
import io.github.winschneid.graphcompare.common.applyRange
import io.github.winschneid.graphcompare.common.applyThemeColors
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.CombinedChartSpec
import io.github.winschneid.graphcompare.core.CombinedPoint
import io.github.winschneid.graphcompare.core.SampleData
import io.github.winschneid.graphcompare.core.ui.ChartColors
import io.github.winschneid.graphcompare.core.ui.CombinedSelectionText
import io.github.winschneid.graphcompare.core.ui.MarkerBubble

/**
 * MPAndroidChart 4.0.1 の複合グラフ。MPChartCompose の CombinedChart を使う。
 * 中身は View の CombinedChart で、設定は setup / update で View のプロパティに書く。
 * マーカーは Composable で書け、選択状態は ChartState から読める。
 */
@Composable
fun CombinedChartScreen(modifier: Modifier = Modifier) {
    val state = rememberChartState()
    val data = remember { combinedData(SampleData.combined) }
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    Column(modifier) {
        CombinedChart(
            data = data,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            state = state,
            contentDescription = "月別の売上と前年比の複合グラフ",
            marker = { entry, _ ->
                (entry.data as? CombinedPoint)?.let { MarkerBubble(ChartFormat.markerText(it)) }
            },
            setup = { configure(SampleData.combined.size) },
            update = { applyThemeColors(textColor, gridColor) },
        )
        CombinedSelectionText(state.selectedEntry?.data as? CombinedPoint)
    }
}

private fun CombinedChartView.configure(pointCount: Int) {
    applyChartSpec()
    drawOrder = listOf(CombinedChartView.DrawOrder.BAR, CombinedChartView.DrawOrder.LINE)

    xAxis.apply {
        granularity = CombinedChartSpec.MONTH_LABEL_STEP.toFloat()
        // 棒の幅の半分だけ両端を広げないと、最初と最後の棒が切れる
        axisMinimum = -0.5f
        axisMaximum = pointCount - 0.5f
        valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.month(value) }
    }
    axisLeft.apply {
        applyRange(CombinedChartSpec.sales)
        valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.sales(value) }
    }
    axisRight.apply {
        applyRange(CombinedChartSpec.growth)
        valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.percent(value) }
    }
    // 凡例の並び順は CombinedData の既定で 折れ線 → 棒 になる
}

private fun combinedData(points: List<CombinedPoint>): CombinedData = CombinedData().apply {
    barData = BarData(salesDataSet(points)).apply { barWidth = CombinedChartSpec.BAR_WIDTH_RATIO }
    lineData = LineData(growthDataSet(points))
}

// 4.x の Entry は payload の型を持てる（ここでは Entry<CombinedPoint>）
private fun salesDataSet(points: List<CombinedPoint>): BarDataSet<CombinedPoint> =
    BarDataSet(
        points.mapIndexed { index, point -> BarEntry(index.toFloat(), point.sales, data = point) },
        ChartFormat.SALES_LABEL,
    ).apply {
        color = ChartColors.series[0].toArgb()
        axisDependency = YAxis.AxisDependency.LEFT
        isDrawValuesEnabled = false
    }

private fun growthDataSet(points: List<CombinedPoint>): LineDataSet<CombinedPoint> =
    LineDataSet(
        points.mapIndexed { index, point -> Entry(index.toFloat(), point.growthRatePercent, data = point) },
        ChartFormat.GROWTH_LABEL,
    ).apply {
        val lineColor = ChartColors.series[1].toArgb()
        color = lineColor
        circleColor = lineColor
        lineWidth = 2.5f
        circleRadius = 4f
        axisDependency = YAxis.AxisDependency.RIGHT
        isDrawValuesEnabled = false
        highlightColor = lineColor
        isHorizontalHighlightIndicatorEnabled = false
    }
