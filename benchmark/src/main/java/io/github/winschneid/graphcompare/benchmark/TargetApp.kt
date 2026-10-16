package io.github.winschneid.graphcompare.benchmark

import androidx.test.platform.app.InstrumentationRegistry

/** 計測対象の applicationId。flavor ごとに benchmark/build.gradle.kts で渡している。 */
val targetAppId: String
    get() = requireNotNull(InstrumentationRegistry.getArguments().getString("targetAppId")) {
        "instrumentation argument 'targetAppId' がありません"
    }
