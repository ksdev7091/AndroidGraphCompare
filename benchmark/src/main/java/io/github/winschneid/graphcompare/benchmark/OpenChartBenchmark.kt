package io.github.winschneid.graphcompare.benchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import io.github.winschneid.graphcompare.core.ChartScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** 各グラフ画面を Intent で直接開き、表示されるまでの時間とフレーム時間を測る。 */
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
        startActivityAndWait { intent ->
            intent.putExtra(ChartScreen.EXTRA_SCREEN, screen.route)
        }
        check(device.wait(Until.hasObject(By.res(screen.testTag)), 5_000)) {
            "${screen.testTag} が表示されませんでした"
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun screens(): List<ChartScreen> = ChartScreen.entries
    }
}
