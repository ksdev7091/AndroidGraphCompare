package io.github.ksdev7091.graphcompare.performance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import io.github.ksdev7091.graphcompare.common.applyChartSpec
import io.github.ksdev7091.graphcompare.common.applyRange
import io.github.ksdev7091.graphcompare.common.applyThemeColors
import io.github.ksdev7091.graphcompare.core.ChartFormat
import io.github.ksdev7091.graphcompare.core.LazyListSpec
import io.github.ksdev7091.graphcompare.core.ui.ChartColors

/**
 * MPAndroidChart 4.0.1 のチャートを LazyColumn に並べる。
 * chart composable の中身は AndroidView で包んだ View なので、行ごとに View が作られる。
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
            val data = remember(index) { lineData(LazyListSpec.series(index)) }
            Column {
                Text(LazyListSpec.title(index), style = MaterialTheme.typography.titleSmall)
                LineChart(
                    data = data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LazyListSpec.CHART_HEIGHT_DP.dp),
                    setup = {
                        applyChartSpec()
                        // リストのスクロールを邪魔しないよう、チャートはタッチを受け取らない
                        isTouchEnabled = false
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
                    },
                    update = { applyThemeColors(textColor, gridColor) },
                )
            }
        }
    }
}

private fun lineData(values: List<Float>): LineData = LineData(
    LineDataSet(values.mapIndexed { x, y -> Entry(x.toFloat(), y) }, "").apply {
        color = ChartColors.series[0].toArgb()
        lineWidth = 2f
        isDrawCirclesEnabled = false
        isDrawValuesEnabled = false
    },
)
