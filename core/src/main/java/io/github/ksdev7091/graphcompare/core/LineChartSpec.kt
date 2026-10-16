package io.github.ksdev7091.graphcompare.core

/** 折れ線グラフ固有の見た目の基準。共通の値は [ChartSpec]。 */
object LineChartSpec {
    /** データ（27〜61 くらい）が収まり、目盛りがきりのよい値になる範囲。 */
    val visitors = AxisRange(min = 0f, max = 80f, step = 20f)

    /** x 軸のラベルは 5 日おき（1日、6日、…）。 */
    const val DAY_LABEL_STEP = 5

    const val LINE_WIDTH_DP = 2.5f

    /** エリア塗りのグラデーション。線の色をこの不透明度から下に向かって透明にする。 */
    const val FILL_TOP_ALPHA = 0.35f
}
