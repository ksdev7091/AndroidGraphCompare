package io.github.winschneid.graphcompare.performance

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.compose.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import io.github.winschneid.graphcompare.common.applyChartSpec
import io.github.winschneid.graphcompare.common.applyRange
import io.github.winschneid.graphcompare.common.applyThemeColors
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.LargeDataSpec
import io.github.winschneid.graphcompare.core.ui.ChartColors

/**
 * MPAndroidChart 4.0.1 で 10,000 点の折れ線を描き、x 方向のピンチズームとドラッグを有効にする。
 * 4.x の間引き（isDecimationEnabled）は既定の off のまま比べる。
 */
@Composable
fun LargeDataScreen(modifier: Modifier = Modifier) {
    val series = LargeDataSpec.series
    val data = remember(series) {
        LineData(
            LineDataSet(series.mapIndexed { x, y -> Entry(x.toFloat(), y) }, "").apply {
                color = ChartColors.series[0].toArgb()
                lineWidth = LargeDataSpec.LINE_WIDTH_DP
                isDrawCirclesEnabled = false
                isDrawValuesEnabled = false
            },
        )
    }
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    LineChart(
        data = data,
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        contentDescription = "10,000 点の折れ線",
        setup = {
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
        },
        update = { applyThemeColors(textColor, gridColor) },
    )
}
