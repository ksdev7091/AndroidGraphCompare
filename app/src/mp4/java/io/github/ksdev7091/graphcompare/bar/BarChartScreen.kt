package io.github.ksdev7091.graphcompare.bar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import io.github.ksdev7091.graphcompare.common.applyChartSpec
import io.github.ksdev7091.graphcompare.common.applyRange
import io.github.ksdev7091.graphcompare.common.applyThemeColors
import io.github.ksdev7091.graphcompare.core.BarChartSpec
import io.github.ksdev7091.graphcompare.core.BarMode
import io.github.ksdev7091.graphcompare.core.ChartFormat
import io.github.ksdev7091.graphcompare.core.MonthlySales
import io.github.ksdev7091.graphcompare.core.ui.BarModeSelector
import io.github.ksdev7091.graphcompare.core.ui.ChartColors

/**
 * MPAndroidChart 4.0.1 の棒グラフ。グループと積み上げをボタンで切り替える。
 * 4.x では barCornerRadius で角丸にでき、積み上げでは一番上の区画だけが丸くなる。
 */
@Composable
fun BarChartScreen(modifier: Modifier = Modifier) {
    var mode by rememberSaveable { mutableStateOf(BarMode.Grouped) }
    // 新しい BarData を渡すと、chart composable がグラフに反映する
    val data = remember(mode) { barData(BarChartSpec.sales, mode) }
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    Column(modifier) {
        BarModeSelector(mode, onModeChange = { mode = it })
        BarChart(
            data = data,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentDescription = "上半期の月別、製品別の売上",
            setup = {
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
            },
            // update は mode（data）が変わるたびに呼ばれる。軸の範囲を変えたら目盛りを計算し直させる
            update = {
                axisLeft.applyRange(BarChartSpec.range(mode))
                notifyDataSetChanged()
                applyThemeColors(textColor, gridColor)
            },
        )
    }
}

private fun barData(sales: List<MonthlySales>, mode: BarMode): BarData = when (mode) {
    // グループ：製品ごとに BarDataSet を作り、groupBars で x 方向に並べ直す（3.x と同じ）
    BarMode.Grouped -> BarData(
        ChartFormat.PRODUCT_LABELS.mapIndexed { product, label ->
            BarDataSet(
                sales.mapIndexed { month, monthly -> BarEntry(month.toFloat(), monthly.values[product]) },
                label,
            ).apply {
                color = ChartColors.series[product].toArgb()
                barCornerRadius = BarChartSpec.CORNER_RADIUS_DP
                isDrawValuesEnabled = false
            }
        },
    ).apply {
        barWidth = BarChartSpec.GROUPED_BAR_WIDTH
        // 群の左端を -0.5 にすると、群の中心が整数の x（月）に来る
        groupBars(-0.5f, BarChartSpec.GROUP_SPACE, BarChartSpec.BAR_SPACE)
    }

    // 積み上げ：1 つの BarDataSet に、値をリストで持つ BarEntry を入れる。凡例の文字は stackLabels で付ける
    BarMode.Stacked -> BarData(
        BarDataSet(
            sales.mapIndexed { month, monthly -> BarEntry(month.toFloat(), monthly.values) },
            "",
        ).apply {
            colors = ChartFormat.PRODUCT_LABELS.indices.map { ChartColors.series[it].toArgb() }
            stackLabels = ChartFormat.PRODUCT_LABELS
            barCornerRadius = BarChartSpec.CORNER_RADIUS_DP
            isDrawValuesEnabled = false
        },
    ).apply { barWidth = BarChartSpec.STACKED_BAR_WIDTH }
}
