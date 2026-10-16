package io.github.ksdev7091.graphcompare.pie

import android.content.Context
import android.graphics.Color
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
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import io.github.ksdev7091.graphcompare.core.ChartFormat
import io.github.ksdev7091.graphcompare.core.PieChartSpec
import io.github.ksdev7091.graphcompare.core.PieSlice
import io.github.ksdev7091.graphcompare.core.ui.ChartColors
import io.github.ksdev7091.graphcompare.core.ui.PieSelectionText

/**
 * MPAndroidChart 3.2.0 のドーナツグラフ。中央の文字、割合の表示、タップでのスライスの強調はライブラリの機能で作れる。
 */
@Composable
fun PieChartScreen(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf<PieSlice?>(null) }
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Column(modifier) {
        AndroidView(
            factory = { context -> createPieChart(context, PieChartSpec.slices) { selected = it } },
            update = { chart ->
                chart.setCenterTextColor(textColor)
                chart.invalidate()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(PieChartSpec.HEIGHT_DP.dp),
        )
        PieSelectionText(selected, PieChartSpec.total)
    }
}

private fun createPieChart(
    context: Context,
    slices: List<PieSlice>,
    onSelect: (PieSlice?) -> Unit,
): PieChart = PieChart(context).apply {
    description.isEnabled = false
    // 凡例は出さず、スライスの上に割合と項目名を描く
    legend.isEnabled = false
    // 既定では指でなぞると円が回る。この比較ではタップだけにする
    isRotationEnabled = false

    // 穴はテーマの背景が見えるよう透明にし、穴の外側の半透明の輪は消す
    holeRadius = PieChartSpec.HOLE_RATIO * 100f
    setHoleColor(Color.TRANSPARENT)
    transparentCircleRadius = 0f
    centerText = ChartFormat.pieCenterText(PieChartSpec.total)
    setCenterTextSize(PieChartSpec.CENTER_TEXT_SIZE_SP)

    // 割合はライブラリが計算する（setUsePercentValues）。書式だけ 3 実装で揃える
    setUsePercentValues(true)
    setEntryLabelColor(Color.WHITE)
    setEntryLabelTextSize(PieChartSpec.LABEL_TEXT_SIZE_SP)

    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry, h: Highlight) = onSelect(e.data as? PieSlice)
        override fun onNothingSelected() = onSelect(null)
    })

    data = PieData(
        PieDataSet(slices.map { PieEntry(it.value, it.label, it) }, "").apply {
            colors = slices.indices.map { ChartColors.series[it].toArgb() }
            sliceSpace = PieChartSpec.SLICE_SPACE_DP
            // 3.x の既定は 18（px のまま）、4.x の既定は 9dp。3 実装で揃える
            selectionShift = PieChartSpec.SELECTION_SHIFT_DP
            valueTextColor = Color.WHITE
            valueTextSize = PieChartSpec.LABEL_TEXT_SIZE_SP
            valueFormatter = IValueFormatter { value, _, _, _ -> ChartFormat.percent(value) }
        },
    )
}
