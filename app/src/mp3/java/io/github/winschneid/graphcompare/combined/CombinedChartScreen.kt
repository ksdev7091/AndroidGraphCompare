package io.github.winschneid.graphcompare.combined

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
import com.github.mikephil.charting.components.XAxis
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
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.CombinedAxisSpec
import io.github.winschneid.graphcompare.core.CombinedPoint
import io.github.winschneid.graphcompare.core.SampleData
import io.github.winschneid.graphcompare.core.ui.ChartColors
import io.github.winschneid.graphcompare.core.ui.CombinedSelectionText

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
            // テーマ（ライト / ダーク）が変わったら文字色を塗り直す
            update = { chart ->
                chart.xAxis.textColor = textColor
                chart.axisLeft.textColor = textColor
                chart.axisRight.textColor = textColor
                chart.legend.textColor = textColor
                chart.axisLeft.gridColor = gridColor
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
    description.isEnabled = false
    // この画面ではタップの比較に絞るため、ズームとドラッグは止める
    setScaleEnabled(false)
    isDragEnabled = false
    drawOrder = arrayOf(CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE)

    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        granularity = CombinedAxisSpec.MONTH_LABEL_STEP.toFloat()
        // 棒の幅の半分だけ両端を広げないと、最初と最後の棒が切れる
        axisMinimum = -0.5f
        axisMaximum = points.size - 0.5f
        setDrawGridLines(false)
        valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.month(value) }
    }
    // 軸の範囲と目盛りは自動計算に任せず、3 実装で揃える
    axisLeft.apply {
        axisMinimum = CombinedAxisSpec.SALES_MIN
        axisMaximum = CombinedAxisSpec.SALES_MAX
        setLabelCount(CombinedAxisSpec.LABEL_COUNT, true)
        gridLineWidth = CombinedAxisSpec.GRID_LINE_WIDTH_DP
        valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.sales(value) }
    }
    axisRight.apply {
        axisMinimum = CombinedAxisSpec.GROWTH_MIN
        axisMaximum = CombinedAxisSpec.GROWTH_MAX
        setLabelCount(CombinedAxisSpec.LABEL_COUNT, true)
        setDrawGridLines(false)
        valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.percent(value) }
    }

    marker = CombinedMarkerView(context).also { it.chartView = this }
    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry, h: Highlight) = onSelect(e.data as? CombinedPoint)
        override fun onNothingSelected() = onSelect(null)
    })

    data = CombinedData().apply {
        setData(BarData(salesDataSet(points)).apply { barWidth = 0.6f })
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
