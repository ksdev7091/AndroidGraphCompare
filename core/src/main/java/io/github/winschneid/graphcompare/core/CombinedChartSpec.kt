package io.github.winschneid.graphcompare.core

/**
 * 複合グラフの見た目の基準。自動計算や既定値に任せるとライブラリごとに違ってしまうので、3 実装ともここに合わせる。
 * 大きさの単位はすべて dp（文字は sp）。
 */
object CombinedChartSpec {
    // 軸の範囲と目盛り。左右の目盛りを同じ数（LABEL_COUNT）にして、補助線の高さを揃える
    const val SALES_MIN = 0f
    const val SALES_MAX = 1_600f
    const val SALES_STEP = 400f

    const val GROWTH_MIN = -20f
    const val GROWTH_MAX = 20f
    const val GROWTH_STEP = 10f

    const val LABEL_COUNT = 5

    /** x 軸のラベルは 2 か月おき（1月、3月、…）。 */
    const val MONTH_LABEL_STEP = 2

    const val GRID_LINE_WIDTH_DP = 1f

    /**
     * 左右の y 軸の幅。ライブラリごとに自動で決まる幅を固定し、描画領域の左右の端を揃える。
     * 凡例も描画領域の左端から並べるので、これで凡例の位置も揃う。
     */
    const val Y_AXIS_WIDTH_DP = 40f

    /** 棒の太さ（x の 1 区間に対する割合）。MPAndroidChart の barWidth と同じ考え方。 */
    const val BAR_WIDTH_RATIO = 0.6f

    /** Vico は棒の太さを dp で指定するので、太さと間隔をこの値と [BAR_WIDTH_RATIO] から決める。 */
    const val BAR_THICKNESS_DP = 16f
    const val BAR_SPACING_DP = BAR_THICKNESS_DP * (1f - BAR_WIDTH_RATIO) / BAR_WIDTH_RATIO

    // 凡例：描画領域の左端から、四角いアイコンと文字を横に並べる
    const val LEGEND_ICON_SIZE_DP = 8f
    const val LEGEND_ICON_LABEL_SPACING_DP = 6f
    const val LEGEND_ITEM_SPACING_DP = 16f
    const val LEGEND_TEXT_SIZE_SP = 11f
}
