package io.github.winschneid.graphcompare.core

/** 複合グラフ固有の見た目の基準。共通の値は [ChartSpec]。 */
object CombinedChartSpec {
    val sales = AxisRange(min = 0f, max = 1_600f, step = 400f)
    val growth = AxisRange(min = -20f, max = 20f, step = 10f)

    /** x 軸のラベルは 2 か月おき（1月、3月、…）。 */
    const val MONTH_LABEL_STEP = 2

    /** 棒の太さ（x の 1 区間に対する割合）。MPAndroidChart の barWidth と同じ考え方。 */
    const val BAR_WIDTH_RATIO = 0.6f

    /** Vico は棒の太さを dp で指定するので、太さと間隔をこの値と [BAR_WIDTH_RATIO] から決める。 */
    const val BAR_THICKNESS_DP = 16f
    const val BAR_SPACING_DP = BAR_THICKNESS_DP * (1f - BAR_WIDTH_RATIO) / BAR_WIDTH_RATIO
}
