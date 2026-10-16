package io.github.winschneid.graphcompare.core

/** 複合グラフ固有の見た目の基準。共通の値は [ChartSpec]。 */
object CombinedChartSpec {
    val sales = AxisRange(min = 0f, max = 1_600f, step = 400f)
    val growth = AxisRange(min = -20f, max = 20f, step = 10f)

    /** x 軸のラベルは 2 か月おき（1月、3月、…）。 */
    const val MONTH_LABEL_STEP = 2
}
