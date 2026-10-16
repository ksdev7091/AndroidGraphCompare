package io.github.winschneid.graphcompare.core

import kotlin.math.ceil
import kotlin.math.floor

/**
 * 大量データの画面。ピンチズームとスクロールの滑らかさを比べる。
 * 描画の負荷をそろえるため、点の印は描かず、線は直線でつなぐ（曲線は計算が重く、ライブラリごとの差が性能に混ざる）。
 */
object LargeDataSpec {
    val series: List<Float> = SampleData.largeSeries()

    /** データが収まり、目盛りが 25 の倍数になる範囲。 */
    val range: AxisRange = run {
        val unit = 25f
        val min = floor(series.min() / unit) * unit
        val step = ceil((series.max() - min) / (ChartSpec.LABEL_COUNT - 1) / unit) * unit
        AxisRange(min, min + step * (ChartSpec.LABEL_COUNT - 1), step)
    }

    const val LINE_WIDTH_DP = 1f
}

/**
 * データ更新アニメーションの画面。ボタンで 12 か月分の値を差し替え、棒が新しい高さへ動く様子を比べる。
 * 時間は MPAndroidChart 4.x の chart composable（animateChanges）の固定値 300ms に合わせ、等速にする。
 */
object UpdateAnimationSpec {
    const val DURATION_MS = 300

    /** 何回目の更新か（[generation]）ごとに値が変わる。 */
    fun values(generation: Long): List<Float> = SampleData.monthlySales(generation).map { it.productA }

    val range = AxisRange(min = 0f, max = 100f, step = 25f)

    /** x 軸のラベルは 2 か月おき（1月、3月、…）。 */
    const val MONTH_LABEL_STEP = 2

    const val UPDATE_BUTTON_TAG = "update_data"
}

/** LazyColumn に小さなチャートを並べる画面。スクロール中のフレームの安定性を比べる。 */
object LazyListSpec {
    const val ITEM_COUNT = SampleData.LAZY_LIST_CHART_COUNT
    const val CHART_HEIGHT_DP = 160f

    val range = AxisRange(min = 0f, max = 80f, step = 20f)

    /** x 軸のラベルは 10 日おき（1日、11日、21日）。 */
    const val DAY_LABEL_STEP = 10

    fun series(index: Int): List<Float> = SampleData.lazyListSeries(index)

    fun title(index: Int): String = "店舗 ${index + 1}"
}
