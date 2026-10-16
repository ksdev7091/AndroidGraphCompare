package io.github.winschneid.graphcompare.performance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.compose.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import io.github.winschneid.graphcompare.common.applyChartSpec
import io.github.winschneid.graphcompare.common.applyRange
import io.github.winschneid.graphcompare.common.applyThemeColors
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.ChartSpec
import io.github.winschneid.graphcompare.core.UpdateAnimationSpec
import io.github.winschneid.graphcompare.core.ui.ChartColors
import io.github.winschneid.graphcompare.core.ui.UpdateDataButton

/**
 * MPAndroidChart 4.0.1 でデータを差し替えてアニメーションさせる。
 * animateChanges = true にすると、新しい BarData を渡すたびに前の値から新しい値へ 300ms で動く（時間は固定）。
 */
@Composable
fun UpdateAnimationScreen(modifier: Modifier = Modifier) {
    var generation by rememberSaveable { mutableLongStateOf(1L) }
    val data = remember(generation) { barData(UpdateAnimationSpec.values(generation)) }
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    Column(modifier) {
        UpdateDataButton(onClick = { generation++ })
        BarChart(
            data = data,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            animateChanges = true,
            contentDescription = "月別の売上",
            setup = {
                applyChartSpec()
                isHighlightPerTapEnabled = false
                isHighlightPerDragEnabled = false
                axisRight.isEnabled = false
                legend.isEnabled = false

                xAxis.apply {
                    granularity = UpdateAnimationSpec.MONTH_LABEL_STEP.toFloat()
                    // 棒の幅の半分だけ両端を広げないと、最初と最後の棒が切れる
                    axisMinimum = -0.5f
                    axisMaximum = 11.5f
                    valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.month(value) }
                }
                axisLeft.apply {
                    applyRange(UpdateAnimationSpec.range)
                    valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.integer(value) }
                }
            },
            update = { applyThemeColors(textColor, gridColor) },
        )
    }
}

private fun barData(values: List<Float>): BarData = BarData(
    BarDataSet(values.mapIndexed { month, value -> BarEntry(month.toFloat(), value) }, "").apply {
        color = ChartColors.series[0].toArgb()
        isDrawValuesEnabled = false
    },
).apply { barWidth = ChartSpec.BAR_WIDTH_RATIO }
