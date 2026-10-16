package io.github.ksdev7091.graphcompare.benchmark

import androidx.test.platform.app.InstrumentationRegistry

/** 計測対象の applicationId。flavor ごとに benchmark/build.gradle.kts で渡している。 */
val targetAppId: String
    get() = requireNotNull(InstrumentationRegistry.getArguments().getString("targetAppId")) {
        "instrumentation argument 'targetAppId' がありません"
    }

/** 大量データのベンチマークの回数。`-e largeDataIterations 15` のように渡す（既定 5）。 */
val largeDataIterations: Int
    get() = InstrumentationRegistry.getArguments().getString("largeDataIterations")?.toInt() ?: 5

/** MPAndroidChart 4.x の間引き（isDecimationEnabled）を on にするか。`-e decimation true` で渡す（4.x 以外は無視される）。 */
val decimationEnabled: Boolean
    get() = InstrumentationRegistry.getArguments().getString("decimation").toBoolean()
