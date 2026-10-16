package io.github.winschneid.graphcompare.performance

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
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
 * MPAndroidChart 3.2.0 でデータを差し替えてアニメーションさせる。
 * 3.x には「前の値から新しい値へ」動かす機能がないので、差し替えたあと animateY で 0 から伸ばし直す。
 */
@Composable
fun UpdateAnimationScreen(modifier: Modifier = Modifier) {
    var generation by rememberSaveable { mutableLongStateOf(1L) }
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    Column(modifier) {
        UpdateDataButton(onClick = { generation++ })
        AndroidView(
            factory = { context -> createBarChart(context) },
            // View は Compose の state を知らないので、generation が変わったかを自分で見てデータを差し替える
            update = { chart ->
                val previous = chart.tag as Long?
                if (previous != generation) {
                    chart.tag = generation
                    chart.data = barData(UpdateAnimationSpec.values(generation))
                    // 最初の表示はアニメーションさせない（4.x と Vico に合わせる）
                    if (previous != null) chart.animateY(UpdateAnimationSpec.DURATION_MS)
                }
                chart.applyThemeColors(textColor, gridColor)
                chart.invalidate()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
        )
    }
}

private fun createBarChart(context: Context): BarChart = BarChart(context).apply {
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
}

private fun barData(values: List<Float>): BarData = BarData(
    BarDataSet(values.mapIndexed { month, value -> BarEntry(month.toFloat(), value) }, "").apply {
        color = ChartColors.series[0].toArgb()
        setDrawValues(false)
    },
).apply { barWidth = ChartSpec.BAR_WIDTH_RATIO }
