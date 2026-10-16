package io.github.ksdev7091.graphcompare.core

import kotlin.random.Random

data class PieSlice(val label: String, val value: Float)

data class MonthlySales(val month: Int, val productA: Float, val productB: Float, val productC: Float) {
    /** 製品 A・B・C の順の値。積み上げでは下から A・B・C の順に重ねる。 */
    val values: List<Float> get() = listOf(productA, productB, productC)
}

data class CombinedPoint(val month: Int, val sales: Float, val growthRatePercent: Float)

data class Candle(val x: Int, val open: Float, val high: Float, val low: Float, val close: Float)

/**
 * 3 実装で同じ見た目・同じ負荷になるよう、データはすべて固定シードで生成する。
 */
object SampleData {
    const val LARGE_SERIES_SIZE = 10_000
    const val LAZY_LIST_CHART_COUNT = 30
    private const val SEED = 42L

    val months: List<String> = (1..12).map { "${it}月" }

    val pie: List<PieSlice> = listOf(
        PieSlice("食費", 42_000f),
        PieSlice("住居", 85_000f),
        PieSlice("交通", 12_000f),
        PieSlice("娯楽", 18_000f),
        PieSlice("その他", 9_000f),
    )

    val monthlySales: List<MonthlySales> = monthlySales(SEED)

    /** 折れ線用の 2 系列（30 点）。 */
    val lineSeries: List<List<Float>> = listOf(
        randomWalk(size = 30, start = 50f, step = 6f, seed = SEED),
        randomWalk(size = 30, start = 35f, step = 4f, seed = SEED + 1),
    )

    val combined: List<CombinedPoint> = run {
        val random = Random(SEED)
        var previous = 1_000f
        (1..12).map { month ->
            val sales = previous * (0.85f + random.nextFloat() * 0.35f)
            CombinedPoint(month, sales, (sales / previous - 1f) * 100f).also { previous = sales }
        }
    }

    val candles: List<Candle> = run {
        val random = Random(SEED)
        var close = 100f
        (0 until 60).map { x ->
            val open = close
            close = open + (random.nextFloat() - 0.5f) * 8f
            val high = maxOf(open, close) + random.nextFloat() * 3f
            val low = minOf(open, close) - random.nextFloat() * 3f
            Candle(x, open, high, low, close)
        }
    }

    fun largeSeries(size: Int = LARGE_SERIES_SIZE): List<Float> =
        randomWalk(size = size, start = 0f, step = 1f, seed = SEED)

    /** LazyColumn に並べる [index] 番目のチャートのデータ（30 点）。軸を揃えるため 10〜70 に収める。 */
    fun lazyListSeries(index: Int): List<Float> =
        randomWalk(size = 30, start = 40f, step = 6f, seed = SEED + 100 + index).map { it.coerceIn(10f, 70f) }

    /** アニメーション画面で差し替える月別売上。[generation] ごとに値が変わる。 */
    fun monthlySales(generation: Long): List<MonthlySales> {
        val random = Random(generation)
        return (1..12).map { month ->
            MonthlySales(
                month = month,
                productA = 40f + random.nextFloat() * 60f,
                productB = 20f + random.nextFloat() * 50f,
                productC = 10f + random.nextFloat() * 30f,
            )
        }
    }

    private fun randomWalk(size: Int, start: Float, step: Float, seed: Long): List<Float> {
        val random = Random(seed)
        var value = start
        return List(size) {
            value += (random.nextFloat() - 0.5f) * 2f * step
            value
        }
    }
}
