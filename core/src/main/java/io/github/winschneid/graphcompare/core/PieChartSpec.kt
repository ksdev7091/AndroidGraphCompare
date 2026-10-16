package io.github.winschneid.graphcompare.core

import kotlin.math.atan2
import kotlin.math.hypot

/**
 * 円（ドーナツ）グラフ固有の見た目の基準。
 * 凡例は出さず、スライスの上に割合と項目名を描く（凡例の位置はライブラリごとの差が大きいため）。
 */
object PieChartSpec {
    const val HEIGHT_DP = 300f

    /** タップしたスライスを外へずらす量。MPAndroidChart はこの分だけ円を小さくして、ずらしても収まるようにする。 */
    const val SELECTION_SHIFT_DP = 8f

    /** 円の直径。MPAndroidChart は「グラフの高さ − ずらす量 × 2」になるので、Vico もこの値に固定する。 */
    const val OUTER_DIAMETER_DP = HEIGHT_DP - SELECTION_SHIFT_DP * 2

    /** 穴の直径（円の直径に対する割合）。MPAndroidChart の holeRadius（%）と同じ考え方。 */
    const val HOLE_RATIO = 0.5f

    const val SLICE_SPACE_DP = 2f
    const val LABEL_TEXT_SIZE_SP = 12f
    const val CENTER_TEXT_SIZE_SP = 14f

    val slices: List<PieSlice> = SampleData.pie
    val total: Float = slices.sumOf { it.value.toDouble() }.toFloat()

    /**
     * 円の中心から ([dx], [dy]) の位置にあるスライスの番号。穴の中や円の外なら null。
     * 12 時の位置から時計回りに並べる前提。Vico にはタップ判定がないので、この計算で自前に判定する。
     */
    fun sliceIndexAt(dx: Float, dy: Float, outerRadius: Float, innerRadius: Float): Int? {
        val distance = hypot(dx, dy)
        if (distance < innerRadius || distance > outerRadius) return null
        // atan2 は 3 時の位置が 0 度で、画面座標（y が下向き）では時計回りに増える。12 時の位置を 0 度にする
        val angle = (Math.toDegrees(atan2(dy, dx).toDouble()).toFloat() + 90f + 360f) % 360f
        var end = 0f
        slices.forEachIndexed { index, slice ->
            end += slice.value / total * 360f
            if (angle < end) return index
        }
        return slices.lastIndex
    }
}
