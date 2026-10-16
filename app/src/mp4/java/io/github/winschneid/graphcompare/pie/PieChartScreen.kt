package io.github.winschneid.graphcompare.pie

import android.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.compose.PieChart
import com.github.mikephil.charting.compose.rememberChartState
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IValueFormatter
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.PieChartSpec
import io.github.winschneid.graphcompare.core.PieSlice
import io.github.winschneid.graphcompare.core.ui.ChartColors
import io.github.winschneid.graphcompare.core.ui.PieSelectionText

/**
 * MPAndroidChart 4.0.1 のドーナツグラフ。設定は 3.x とほぼ同じで、選択は ChartState から読める。
 */
@Composable
fun PieChartScreen(modifier: Modifier = Modifier) {
    val state = rememberChartState()
    val slices = PieChartSpec.slices
    val data = remember(slices) { pieData(slices) }
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Column(modifier) {
        PieChart(
            data = data,
            modifier = Modifier
                .fillMaxWidth()
                .height(PieChartSpec.HEIGHT_DP.dp),
            state = state,
            contentDescription = "1 か月の支出の内訳",
            setup = {
                description.isEnabled = false
                // 凡例は出さず、スライスの上に割合と項目名を描く
                legend.isEnabled = false
                // 既定では指でなぞると円が回る。この比較ではタップだけにする
                isRotationEnabled = false

                // 穴はテーマの背景が見えるよう透明にし、穴の外側の半透明の輪は消す
                holeRadius = PieChartSpec.HOLE_RATIO * 100f
                holeColor = Color.TRANSPARENT
                transparentCircleRadius = 0f
                centerText = ChartFormat.pieCenterText(PieChartSpec.total)
                centerTextSize = PieChartSpec.CENTER_TEXT_SIZE_SP

                // 割合はライブラリが計算する（isUsePercentValuesEnabled）。書式だけ 3 実装で揃える
                isUsePercentValuesEnabled = true
                entryLabelColor = Color.WHITE
                entryLabelTextSize = PieChartSpec.LABEL_TEXT_SIZE_SP
            },
            update = { centerTextColor = textColor },
        )
        PieSelectionText(state.selectedEntry?.data as? PieSlice, PieChartSpec.total)
    }
}

private fun pieData(slices: List<PieSlice>): PieData = PieData(
    PieDataSet(slices.map { PieEntry(it.value, it.label, data = it) }, "").apply {
        colors = slices.indices.map { ChartColors.series[it].toArgb() }
        sliceSpace = PieChartSpec.SLICE_SPACE_DP
        // 4.x の既定は 9dp（3.x は 18 を px のまま使う）。3 実装で揃える
        selectionShift = PieChartSpec.SELECTION_SHIFT_DP
        valueTextColor = Color.WHITE
        valueTextSize = PieChartSpec.LABEL_TEXT_SIZE_SP
        valueFormatter = IValueFormatter { value, _, _, _ -> ChartFormat.percent(value) }
    },
)
