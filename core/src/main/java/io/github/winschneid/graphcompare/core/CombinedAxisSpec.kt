package io.github.winschneid.graphcompare.core

/**
 * 複合グラフの軸の範囲と目盛り。自動計算に任せるとライブラリごとに区切りが変わるので、3 実装とも固定する。
 * 左右の軸の目盛りを同じ数（[LABEL_COUNT]）にして、補助線の高さを揃える。
 */
object CombinedAxisSpec {
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
}
