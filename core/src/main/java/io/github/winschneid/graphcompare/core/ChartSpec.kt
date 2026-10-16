package io.github.winschneid.graphcompare.core

/**
 * 全グラフ共通の見た目の基準。既定値に任せるとライブラリごとに違ってしまうので、3 実装ともここに合わせる。
 * 大きさの単位はすべて dp（文字は sp）。
 */
object ChartSpec {
    /** y 軸の目盛りの数。左右の軸がある場合も同じ数にして、補助線の高さを揃える。 */
    const val LABEL_COUNT = 5

    const val AXIS_TEXT_SIZE_SP = 10f
    const val GRID_LINE_WIDTH_DP = 1f

    /**
     * y 軸の幅。ライブラリごとに自動で決まる幅を固定し、描画領域の左右の端を揃える。
     * 凡例も描画領域の左端から並べるので、これで凡例の位置も揃う。
     */
    const val Y_AXIS_WIDTH_DP = 40f

    /** 右に軸がないときの右余白。MPAndroidChart の既定の最小余白（minOffset）と同じ値。 */
    const val NO_AXIS_SIDE_PADDING_DP = 15f

    // 凡例：描画領域の左端から、四角いアイコンと文字を横に並べる
    const val LEGEND_ICON_SIZE_DP = 8f
    const val LEGEND_ICON_LABEL_SPACING_DP = 6f
    const val LEGEND_ITEM_SPACING_DP = 16f
    const val LEGEND_TEXT_SIZE_SP = 11f
}

/** 固定した軸の範囲。[step] ごとに目盛りを置く（目盛りの数は [ChartSpec.LABEL_COUNT] 個になるように決める）。 */
data class AxisRange(val min: Float, val max: Float, val step: Float)
