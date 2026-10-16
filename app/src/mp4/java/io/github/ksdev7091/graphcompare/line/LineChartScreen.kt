package io.github.ksdev7091.graphcompare.line

import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import io.github.ksdev7091.graphcompare.core.LineChartSpec
import io.github.ksdev7091.graphcompare.core.SampleData
import io.github.ksdev7091.graphcompare.core.ui.ChartColors

/** MPAndroidChart 4.0.1 の折れ線グラフ。2 系列を曲線でつなぎ、下をグラデーションで塗る。 */
@Composable
fun LineChartScreen(modifier: Modifier = Modifier) {
    val series = SampleData.lineSeries
    val data = remember(series) {
        LineData(
            series.mapIndexed { index, values ->
                lineDataSet(values, ChartFormat.LINE_SERIES_LABELS[index], ChartColors.series[index])
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
        contentDescription = "2 店舗の 30 日間の来店者数",
        setup = {
            applyChartSpec()
            // この画面は見た目の比較なので、タップでの選択は止める
            isHighlightPerTapEnabled = false
            isHighlightPerDragEnabled = false
            axisRight.isEnabled = false

            xAxis.apply {
                granularity = LineChartSpec.DAY_LABEL_STEP.toFloat()
                axisMinimum = 0f
                axisMaximum = series.first().size - 1f
                valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.day(value) }
            }
            axisLeft.apply {
                applyRange(LineChartSpec.visitors)
                valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.integer(value) }
            }
        },
        update = { applyThemeColors(textColor, gridColor) },
    )
}

private fun lineDataSet(values: List<Float>, label: String, color: Color): LineDataSet<Nothing> =
    LineDataSet(values.mapIndexed { x, y -> Entry(x.toFloat(), y) }, label).apply {
        this.color = color.toArgb()
        lineWidth = LineChartSpec.LINE_WIDTH_DP
        mode = LineDataSet.Mode.CUBIC_BEZIER
        isDrawCirclesEnabled = false
        isDrawValuesEnabled = false
        // グラデーションは 3.x と同じく Drawable で渡す（単色なら fillColor と fillAlpha）
        isDrawFilledEnabled = true
        fillDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(color.copy(alpha = LineChartSpec.FILL_TOP_ALPHA).toArgb(), Color.Transparent.toArgb()),
        )
    }
