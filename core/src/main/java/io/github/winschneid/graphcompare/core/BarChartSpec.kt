package io.github.winschneid.graphcompare.core

/** 棒グラフの表示方法。画面のボタンで切り替える。 */
enum class BarMode(val label: String) {
    Grouped("グループ"),
    Stacked("積み上げ"),
}

/** 棒グラフ固有の見た目の基準。共通の値は [ChartSpec]。 */
object BarChartSpec {
    /** 12 か月 × 3 製品だと棒が細くなりすぎるので、上半期（1〜6 月）だけを出す。 */
    val sales: List<MonthlySales> = SampleData.monthlySales.take(6)

    fun range(mode: BarMode): AxisRange = when (mode) {
        BarMode.Grouped -> AxisRange(min = 0f, max = 100f, step = 25f)
        BarMode.Stacked -> AxisRange(min = 0f, max = 240f, step = 60f)
    }

    // 太さと間隔は MPAndroidChart と同じく「x の 1 区間に対する割合」で決める。
    // グループ：1 区間 = 群の間隔 0.4 + 3 本 ×（棒 0.18 + 棒の間隔 0.02）
    const val GROUP_SPACE = 0.4f
    const val BAR_SPACE = 0.02f
    const val GROUPED_BAR_WIDTH = (1f - GROUP_SPACE) / 3 - BAR_SPACE

    /** 積み上げ：1 本の太さ。複合グラフの棒と同じ。 */
    const val STACKED_BAR_WIDTH = 0.6f

    /**
     * Vico は太さと間隔を dp で決めるので、x の 1 区間をこの dp とみなして割合から換算する。
     * 全体が画面に収まる倍率（Zoom.Content）で拡大縮小されるので、比率さえ合っていれば値そのものは何でもよい。
     */
    const val X_UNIT_DP = 64f

    /** 棒の角丸（MPAndroidChart 4.x の barCornerRadius に合わせる）。グループは 4 隅、積み上げは一番上の区画の上側だけを丸める。 */
    const val CORNER_RADIUS_DP = 4f
}
