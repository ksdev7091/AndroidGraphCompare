package io.github.winschneid.graphcompare.core

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/** 軸ラベルやマーカーの文字。3 実装で同じ文字列になるよう、書式はここにまとめる。 */
object ChartFormat {
    const val SALES_LABEL = "売上（左軸）"
    const val GROWTH_LABEL = "前年比（右軸）"
    val LINE_SERIES_LABELS = listOf("店舗A", "店舗B")
    val PRODUCT_LABELS = listOf("製品A", "製品B", "製品C")

    private val integerFormat = NumberFormat.getIntegerInstance(Locale.JAPAN)

    /** x 座標（0 始まりの月インデックス）を「1月」のような文字にする。範囲外は空文字。 */
    fun month(x: Number): String = SampleData.months.getOrElse(x.toFloat().roundToInt()) { "" }

    /** x 座標（0 始まりの日インデックス）を「1日」のような文字にする。 */
    fun day(x: Number): String = "${x.toFloat().roundToInt() + 1}日"

    fun integer(value: Number): String = integerFormat.format(value.toFloat().roundToInt())

    fun sales(value: Number): String = integer(value)

    fun percent(value: Number): String = "${value.toFloat().roundToInt()}%"

    fun signedPercent(value: Float): String = String.format(Locale.JAPAN, "%+.1f%%", value)

    /** 円グラフのスライスに描く文字。MPAndroidChart と同じく、割合の下に項目名を置く。 */
    fun pieSliceText(slice: PieSlice, total: Float): String = "${percent(slice.value / total * 100f)}\n${slice.label}"

    fun pieCenterText(total: Float): String = "合計\n${integer(total)}円"

    fun pieSelectionText(slice: PieSlice, total: Float): String =
        "${slice.label} ${integer(slice.value)}円（${percent(slice.value / total * 100f)}）"

    fun markerText(point: CombinedPoint): String =
        "${month(point.month - 1)}\n売上 ${sales(point.sales)}\n前年比 ${signedPercent(point.growthRatePercent)}"
}
