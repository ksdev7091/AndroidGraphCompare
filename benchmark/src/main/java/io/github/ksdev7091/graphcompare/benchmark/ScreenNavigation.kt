package io.github.ksdev7091.graphcompare.benchmark

import android.content.Intent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import io.github.ksdev7091.graphcompare.core.ChartScreen

private const val WAIT_TIMEOUT_MS = 5_000L

/** [screen] を Intent extra で直接開き、グラフが表示されるまで待つ。[extras] で計測用の extra を足せる。 */
fun MacrobenchmarkScope.openScreen(screen: ChartScreen, extras: (Intent) -> Unit = {}) {
    startActivityAndWait { intent ->
        intent.putExtra(ChartScreen.EXTRA_SCREEN, screen.route)
        extras(intent)
    }
    waitForObject(screen.testTag)
}

/** testTag（resource id として公開している）で要素を探し、出てくるまで待つ。 */
fun MacrobenchmarkScope.waitForObject(testTag: String): UiObject2 =
    checkNotNull(device.wait(Until.findObject(By.res(testTag)), WAIT_TIMEOUT_MS)) { "$testTag が表示されませんでした" }
