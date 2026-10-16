package io.github.ksdev7091.graphcompare.combined

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import io.github.ksdev7091.graphcompare.common.applyChartSpec
import io.github.ksdev7091.graphcompare.common.applyRange
import io.github.ksdev7091.graphcompare.common.applyThemeColors
import io.github.ksdev7091.graphcompare.core.ChartFormat
import io.github.ksdev7091.graphcompare.core.ChartSpec
import io.github.ksdev7091.graphcompare.core.CombinedChartSpec
import io.github.ksdev7091.graphcompare.core.CombinedPoint
import io.github.ksdev7091.graphcompare.core.SampleData
import io.github.ksdev7091.graphcompare.core.ui.ChartColors
import io.github.ksdev7091.graphcompare.core.ui.CombinedSelectionText

/**
 * MPAndroidChart 3.2.0 の複合グラフ。View の CombinedChart を AndroidView で包む。
 * 選択状態はリスナーで受け取り、Compose の state に書き戻す。
 */
@Composable
fun CombinedChartScreen(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf<CombinedPoint?>(null) }
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    Column(modifier) {
        AndroidView(
            factory = { context ->
                createCombinedChart(context, SampleData.combined) { selected = it }
            },
            update = { chart ->
                chart.applyThemeColors(textColor, gridColor)
                chart.invalidate()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
        )
        CombinedSelectionText(selected)
    }
}

private fun createCombinedChart(
    context: Context,
    points: List<CombinedPoint>,
    onSelect: (CombinedPoint?) -> Unit,
): CombinedChart = CombinedChart(context).apply {
    applyChartSpec()
    // 3.x は「なぞってハイライト」をドラッグが有効なときにしか行わない（ドラッグも拡大縮小も無効だと移動を無視する）。
    // 拡大縮小は無効のままなので全体表示から動かず、なぞるとマーカーが指に合わせて動くようになる（4.x と同じ動き）
    isDragEnabled = true
    drawOrder = arrayOf(CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE)

    xAxis.apply {
        granularity = CombinedChartSpec.MONTH_LABEL_STEP.toFloat()
        // 棒の幅の半分だけ両端を広げないと、最初と最後の棒が切れる
        axisMinimum = -0.5f
        axisMaximum = points.size - 0.5f
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

    marker = CombinedMarkerView(context).also { it.chartView = this }
    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry, h: Highlight) = onSelect(e.data as? CombinedPoint)
        override fun onNothingSelected() = onSelect(null)
    })

    data = CombinedData().apply {
        setData(BarData(salesDataSet(points)).apply { barWidth = ChartSpec.BAR_WIDTH_RATIO })
        setData(LineData(growthDataSet(points)))
    }
}

private fun salesDataSet(points: List<CombinedPoint>): BarDataSet =
    BarDataSet(
        points.mapIndexed { index, point -> BarEntry(index.toFloat(), point.sales, point) },
        ChartFormat.SALES_LABEL,
    ).apply {
        color = ChartColors.series[0].toArgb()
        axisDependency = YAxis.AxisDependency.LEFT
        setDrawValues(false)
    }

private fun growthDataSet(points: List<CombinedPoint>): LineDataSet =
    LineDataSet(
        points.mapIndexed { index, point -> Entry(index.toFloat(), point.growthRatePercent, point) },
        ChartFormat.GROWTH_LABEL,
    ).apply {
        val lineColor = ChartColors.series[1].toArgb()
        color = lineColor
        setCircleColor(lineColor)
        lineWidth = 2.5f
        circleRadius = 4f
        axisDependency = YAxis.AxisDependency.RIGHT
        setDrawValues(false)
        highLightColor = lineColor
        setDrawHorizontalHighlightIndicator(false)
    }
