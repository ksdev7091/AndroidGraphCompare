package io.github.winschneid.graphcompare.performance

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import io.github.winschneid.graphcompare.common.applyChartSpec
import io.github.winschneid.graphcompare.common.applyRange
import io.github.winschneid.graphcompare.common.applyThemeColors
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.LazyListSpec
import io.github.winschneid.graphcompare.core.ui.ChartColors

/**
 * MPAndroidChart 3.2.0 のチャートを LazyColumn に並べる。各行で LineChart（View）を AndroidView で包む。
 * MPChartCompose と同じく AndroidView の onReset は渡さないので、View は行ごとに作られる（使い回されない）。
 */
@Composable
fun LazyListScreen(modifier: Modifier = Modifier) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(LazyListSpec.ITEM_COUNT) { index ->
            Column {
                Text(LazyListSpec.title(index), style = MaterialTheme.typography.titleSmall)
                AndroidView(
                    factory = { context -> createSmallChart(context) },
                    update = { chart ->
                        if (chart.tag != index) {
                            chart.tag = index
                            chart.data = lineData(LazyListSpec.series(index))
                        }
                        chart.applyThemeColors(textColor, gridColor)
                        chart.invalidate()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LazyListSpec.CHART_HEIGHT_DP.dp),
                )
            }
        }
    }
}

private fun createSmallChart(context: Context): LineChart = LineChart(context).apply {
    applyChartSpec()
    // リストのスクロールを邪魔しないよう、チャートはタッチを受け取らない
    setTouchEnabled(false)
    axisRight.isEnabled = false
    legend.isEnabled = false

    xAxis.apply {
        granularity = LazyListSpec.DAY_LABEL_STEP.toFloat()
        axisMinimum = 0f
        axisMaximum = 29f
        valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.day(value) }
    }
    axisLeft.apply {
        applyRange(LazyListSpec.range)
        valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.integer(value) }
    }
}

private fun lineData(values: List<Float>): LineData = LineData(
    LineDataSet(values.mapIndexed { x, y -> Entry(x.toFloat(), y) }, "").apply {
        color = ChartColors.series[0].toArgb()
        lineWidth = 2f
        setDrawCircles(false)
        setDrawValues(false)
    },
)
