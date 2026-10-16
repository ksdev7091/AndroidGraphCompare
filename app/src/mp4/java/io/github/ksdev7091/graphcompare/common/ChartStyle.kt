package io.github.ksdev7091.graphcompare.common

import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import io.github.ksdev7091.graphcompare.core.AxisRange
import io.github.ksdev7091.graphcompare.core.ChartSpec

/**
 * 全グラフ共通の見た目（[ChartSpec]）を MPAndroidChart 4.0.1 の View に当てる。chart composable の setup から呼ぶ。
 * この比較ではタップ以外の操作をそろえるため、ズームとドラッグは止める。
 */
fun BarLineChartBase<*>.applyChartSpec() {
    description.isEnabled = false
    isScaleEnabled = false
    isDragEnabled = false

    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        textSize = ChartSpec.AXIS_TEXT_SIZE_SP
        isDrawGridLinesEnabled = false
    }
    axisLeft.apply {
        textSize = ChartSpec.AXIS_TEXT_SIZE_SP
        gridLineWidth = ChartSpec.GRID_LINE_WIDTH_DP
        applyFixedWidth()
    }
    axisRight.apply {
        textSize = ChartSpec.AXIS_TEXT_SIZE_SP
        isDrawGridLinesEnabled = false
        applyFixedWidth()
    }
    legend.applyChartSpec()
}

/** 軸の範囲と目盛りは自動計算に任せず、3 実装で揃える。 */
fun YAxis.applyRange(range: AxisRange) {
    axisMinimum = range.min
    axisMaximum = range.max
    labelCount = ChartSpec.LABEL_COUNT
    isForceLabelsEnabled = true
}

/** 軸の幅は既定ではラベルの長さで決まる。3 実装で描画領域の左右の端を揃えるために固定する。 */
private fun YAxis.applyFixedWidth() {
    minWidth = ChartSpec.Y_AXIS_WIDTH_DP
    maxWidth = ChartSpec.Y_AXIS_WIDTH_DP
}

/** 凡例：描画領域の左端から、四角いアイコンと文字を横に並べる。 */
private fun Legend.applyChartSpec() {
    form = Legend.LegendForm.SQUARE
    formSize = ChartSpec.LEGEND_ICON_SIZE_DP
    formToTextSpace = ChartSpec.LEGEND_ICON_LABEL_SPACING_DP
    xEntrySpace = ChartSpec.LEGEND_ITEM_SPACING_DP
    textSize = ChartSpec.LEGEND_TEXT_SIZE_SP
    horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
    orientation = Legend.LegendOrientation.HORIZONTAL
    xOffset = 0f
}

/**
 * テーマ（ライト / ダーク）に合わせて文字と補助線の色を塗り直す。chart composable の update から呼ぶ。
 * 中身は View なので、Compose の色は自動では反映されない。
 */
fun BarLineChartBase<*>.applyThemeColors(textColor: Int, gridColor: Int) {
    xAxis.textColor = textColor
    axisLeft.textColor = textColor
    axisRight.textColor = textColor
    legend.textColor = textColor
    axisLeft.gridColor = gridColor
}
