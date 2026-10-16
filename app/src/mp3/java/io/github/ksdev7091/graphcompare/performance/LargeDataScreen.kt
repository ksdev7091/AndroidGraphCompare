package io.github.ksdev7091.graphcompare.performance

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import io.github.ksdev7091.graphcompare.common.applyChartSpec
import io.github.ksdev7091.graphcompare.common.applyRange
import io.github.ksdev7091.graphcompare.common.applyThemeColors
import io.github.ksdev7091.graphcompare.core.ChartFormat
import io.github.ksdev7091.graphcompare.core.LargeDataSpec
import io.github.ksdev7091.graphcompare.core.ui.ChartColors

/** MPAndroidChart 3.2.0 で 10,000 点の折れ線を描き、x 方向のピンチズームとドラッグを有効にする。 */
@Composable
fun LargeDataScreen(modifier: Modifier = Modifier) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    AndroidView(
        factory = { context -> createLargeDataChart(context, LargeDataSpec.series) },
        update = { chart ->
            chart.applyThemeColors(textColor, gridColor)
            chart.invalidate()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
    )
}

private fun createLargeDataChart(context: Context, series: List<Float>): LineChart = LineChart(context).apply {
    applyChartSpec()
    // この画面だけ、x 方向のピンチズームとドラッグを有効にする（Vico のズームも x 方向だけ）
    isDragEnabled = true
    isScaleXEnabled = true
    isScaleYEnabled = false
    isHighlightPerTapEnabled = false
    isHighlightPerDragEnabled = false
    axisRight.isEnabled = false
    legend.isEnabled = false

    xAxis.valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.integer(value) }
    axisLeft.apply {
        applyRange(LargeDataSpec.range)
        valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.integer(value) }
    }

    data = LineData(
        LineDataSet(series.mapIndexed { x, y -> Entry(x.toFloat(), y) }, "").apply {
            color = ChartColors.series[0].toArgb()
            lineWidth = LargeDataSpec.LINE_WIDTH_DP
            setDrawCircles(false)
            setDrawValues(false)
        },
    )
}
