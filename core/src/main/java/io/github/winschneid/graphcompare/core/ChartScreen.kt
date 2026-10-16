package io.github.winschneid.graphcompare.core

/**
 * 3 つの実装（flavor）で共通の画面一覧。
 * [route] はベンチマークから画面を直接開くときの Intent extra と、UiAutomator で探す testTag に使う。
 */
enum class ChartScreen(
    val route: String,
    val title: String,
    val description: String,
) {
    Pie(
        route = "pie",
        title = "円 / ドーナツ",
        description = "中央テキスト、パーセント表示、スライスのタップでハイライト",
    ),
    Bar(
        route = "bar",
        title = "棒（グループ・積み上げ）",
        description = "月別 × 3 製品の売上。角丸・グラデーション",
    ),
    Line(
        route = "line",
        title = "折れ線（エリア・曲線）",
        description = "2 系列、エリア塗り、曲線補間",
    ),
    Combined(
        route = "combined",
        title = "複合（棒 + 折れ線 + マーカー）",
        description = "売上（棒・左軸）と前年比（折れ線・右軸）。タップでマーカーと文字を表示",
    ),
    LargeData(
        route = "large_data",
        title = "大量データ（ズーム・スクロール）",
        description = "${SampleData.LARGE_SERIES_SIZE} 点の折れ線をピンチズーム・スクロール",
    ),
    Animation(
        route = "animation",
        title = "データ更新アニメーション",
        description = "ボタンでデータを差し替えてアニメーションさせる",
    ),
    LazyList(
        route = "lazy_list",
        title = "LazyColumn に多数のチャート",
        description = "${SampleData.LAZY_LIST_CHART_COUNT} 個のチャートを並べてスクロール",
    ),
    ;

    val testTag: String get() = "chart_$route"

    companion object {
        const val EXTRA_SCREEN = "screen"

        fun fromRoute(route: String?): ChartScreen? = entries.firstOrNull { it.route == route }
    }
}
