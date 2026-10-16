package io.github.winschneid.graphcompare.bar

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import io.github.winschneid.graphcompare.core.BarChartSpec
import io.github.winschneid.graphcompare.core.BarMode
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.MonthlySales
import io.github.winschneid.graphcompare.core.ui.BarModeSelector
import io.github.winschneid.graphcompare.core.ui.ChartColors

/**
 * MPAndroidChart 3.2.0 の棒グラフ。グループと積み上げをボタンで切り替える。
 * 3.x には角丸の設定がないので、棒は四角のまま（角丸にするには BarChartRenderer を自作する）。
 */
@Composable
fun BarChartScreen(modifier: Modifier = Modifier) {
    var mode by rememberSaveable { mutableStateOf(BarMode.Grouped) }
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    Column(modifier) {
        BarModeSelector(mode, onModeChange = { mode = it })
        AndroidView(
            factory = { context -> createBarChart(context) },
            // View は Compose の state を知らないので、モードが変わったらデータと軸を差し替えて計算し直させる
            update = { chart ->
                if (chart.tag != mode) {
                    chart.tag = mode
                    chart.data = barData(BarChartSpec.sales, mode)
                    chart.axisLeft.applyRange(BarChartSpec.range(mode))
                    chart.notifyDataSetChanged()
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
    // この画面は見た目の比較なので、タップでの選択は止める
    isHighlightPerTapEnabled = false
    isHighlightPerDragEnabled = false
    axisRight.isEnabled = false

    xAxis.apply {
        granularity = 1f
        // 棒（群）の幅の半分だけ両端を広げないと、最初と最後の棒が切れる
        axisMinimum = -0.5f
        axisMaximum = BarChartSpec.sales.size - 0.5f
        valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.month(value) }
    }
    axisLeft.valueFormatter = IAxisValueFormatter { value, _ -> ChartFormat.integer(value) }
}

private fun barData(sales: List<MonthlySales>, mode: BarMode): BarData = when (mode) {
    // グループ：製品ごとに BarDataSet を作り、groupBars で x 方向に並べ直す
    BarMode.Grouped -> BarData(
        ChartFormat.PRODUCT_LABELS.mapIndexed { product, label ->
            BarDataSet(
                sales.mapIndexed { month, monthly -> BarEntry(month.toFloat(), monthly.values[product]) },
                label,
            ).apply {
                color = ChartColors.series[product].toArgb()
                setDrawValues(false)
            }
        },
    ).apply {
        barWidth = BarChartSpec.GROUPED_BAR_WIDTH
        // 群の左端を -0.5 にすると、群の中心が整数の x（月）に来る
        groupBars(-0.5f, BarChartSpec.GROUP_SPACE, BarChartSpec.BAR_SPACE)
    }

    // 積み上げ：1 つの BarDataSet に、値を配列で持つ BarEntry を入れる。凡例の文字は stackLabels で付ける
    BarMode.Stacked -> BarData(
        BarDataSet(
            sales.mapIndexed { month, monthly -> BarEntry(month.toFloat(), monthly.values.toFloatArray()) },
            "",
        ).apply {
            colors = ChartFormat.PRODUCT_LABELS.indices.map { ChartColors.series[it].toArgb() }
            stackLabels = ChartFormat.PRODUCT_LABELS.toTypedArray()
            setDrawValues(false)
        },
    ).apply { barWidth = BarChartSpec.STACKED_BAR_WIDTH }
}
