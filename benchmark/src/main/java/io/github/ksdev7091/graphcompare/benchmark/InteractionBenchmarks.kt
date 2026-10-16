package io.github.ksdev7091.graphcompare.benchmark

import android.os.SystemClock
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.Direction
import io.github.ksdev7091.graphcompare.core.ChartOptions
import io.github.ksdev7091.graphcompare.core.ChartScreen
import io.github.ksdev7091.graphcompare.core.UpdateAnimationSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ITERATIONS = 5

/** 10,000 点の折れ線をピンチで拡大し、左右にスクロールして、縮小する間のフレーム時間。 */
@RunWith(AndroidJUnit4::class)
class LargeDataBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun zoomAndScroll() = rule.measureRepeated(
        packageName = targetAppId,
        metrics = listOf(FrameTimingMetric()),
        iterations = largeDataIterations,
        setupBlock = {
            openScreen(ChartScreen.LargeData) { it.putExtra(ChartOptions.EXTRA_DECIMATION, decimationEnabled) }
        },
    ) {
        val chart = waitForObject(ChartScreen.LargeData.testTag)
        chart.pinchOpen(0.8f)
        repeat(2) { chart.swipe(Direction.LEFT, 0.8f) }
        repeat(2) { chart.swipe(Direction.RIGHT, 0.8f) }
        chart.pinchClose(0.8f)
        device.waitForIdle()
    }
}

/** ボタンでデータを 10 回差し替え、そのたびにアニメーションが終わるまで待つ間のフレーム時間。 */
@RunWith(AndroidJUnit4::class)
class UpdateAnimationBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun updateTenTimes() = rule.measureRepeated(
        packageName = targetAppId,
        metrics = listOf(FrameTimingMetric()),
        iterations = ITERATIONS,
        setupBlock = { openScreen(ChartScreen.Animation) },
    ) {
        val button = waitForObject(UpdateAnimationSpec.UPDATE_BUTTON_TAG)
        repeat(10) {
            button.click()
            // アニメーション（300ms）が終わるまで待ってから次を押す
            SystemClock.sleep(UpdateAnimationSpec.DURATION_MS + 200L)
        }
    }
}

/** 30 個のチャートを並べた LazyColumn を下へ 3 回フリックする間のフレーム時間。 */
@RunWith(AndroidJUnit4::class)
class LazyListBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun flingDown() = rule.measureRepeated(
        packageName = targetAppId,
        metrics = listOf(FrameTimingMetric()),
        iterations = ITERATIONS,
        setupBlock = { openScreen(ChartScreen.LazyList) },
    ) {
        val list = waitForObject(ChartScreen.LazyList.testTag)
        // 画面の端からのジェスチャーはシステムの戻る操作になるので、端を避ける
        list.setGestureMargin(device.displayWidth / 5)
        repeat(3) {
            list.fling(Direction.DOWN)
            device.waitForIdle()
        }
    }
}
