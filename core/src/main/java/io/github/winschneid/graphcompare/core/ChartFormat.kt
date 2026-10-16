package io.github.winschneid.graphcompare.core

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/** 軸ラベルやマーカーの文字。3 実装で同じ文字列になるよう、書式はここにまとめる。 */
object ChartFormat {
    const val SALES_LABEL = "売上（左軸）"
    const val GROWTH_LABEL = "前年比（右軸）"

    private val integerFormat = NumberFormat.getIntegerInstance(Locale.JAPAN)

    /** x 座標（0 始まりの月インデックス）を「1月」のような文字にする。範囲外は空文字。 */
    fun month(x: Number): String = SampleData.months.getOrElse(x.toFloat().roundToInt()) { "" }

    fun sales(value: Number): String = integerFormat.format(value.toFloat().roundToInt())

    fun percent(value: Number): String = "${value.toFloat().roundToInt()}%"

    fun signedPercent(value: Float): String = String.format(Locale.JAPAN, "%+.1f%%", value)

    fun markerText(point: CombinedPoint): String =
        "${month(point.month - 1)}\n売上 ${sales(point.sales)}\n前年比 ${signedPercent(point.growthRatePercent)}"
}
