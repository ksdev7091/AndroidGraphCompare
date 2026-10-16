package io.github.ksdev7091.graphcompare.benchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import io.github.ksdev7091.graphcompare.core.ChartScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** 各グラフ画面をコールドスタートで直接開き、表示されるまでの時間とフレーム時間を測る。 */
@RunWith(Parameterized::class)
class OpenChartBenchmark(private val screen: ChartScreen) {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun openChart() = rule.measureRepeated(
        packageName = targetAppId,
        metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        openScreen(screen)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun screens(): List<ChartScreen> = ChartScreen.entries
    }
}
