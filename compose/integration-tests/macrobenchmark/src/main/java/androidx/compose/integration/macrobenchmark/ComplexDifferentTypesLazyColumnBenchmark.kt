/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.integration.macrobenchmark

import android.content.Intent
import android.graphics.Point
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingGfxInfoMetric
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class ComplexDifferentTypesLazyColumnBenchmark {
    @get:Rule val benchmarkRule = MacrobenchmarkRule()

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
    }

    @OptIn(ExperimentalMetricApi::class)
    @Test
    fun start() {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric(), FrameTimingGfxInfoMetric()),
            compilationMode = CompilationMode.Full(),
            iterations = 5,
            setupBlock = {
                val intent = Intent()
                intent.action = ACTION
                startActivityAndWait(intent)
            }
        ) {
            val lazyColumn = device.findObject(By.desc(CONTENT_DESCRIPTION))
            // Setting a gesture margin is important otherwise gesture nav is triggered.
            lazyColumn.setGestureMargin(device.displayWidth / 5)
            for (i in 1..8) {
                // From center we scroll 2/3 of it which is 1/3 of the screen.
                lazyColumn.drag(Point(lazyColumn.visibleCenter.x, lazyColumn.visibleCenter.y / 3))
                device.wait(Until.findObject(By.desc(COMPOSE_IDLE)), 3000)
            }
        }
    }

    companion object {
        private const val PACKAGE_NAME = "androidx.compose.integration.macrobenchmark.target"
        private const val ACTION =
            "androidx.compose.integration.macrobenchmark.target.COMPLEX_DIFFERENT_TYPES_LAZY_COLUMN"
        private const val CONTENT_DESCRIPTION = "IamLazy"

        private const val COMPOSE_IDLE = "COMPOSE-IDLE"
    }
}
