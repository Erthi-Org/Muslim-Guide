package org.erthi.guide

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Each phase runs in a separate instrumentation invocation on a disposable emulator. */
class ProcessRestartTest {
    @Test fun persistAcrossRestart() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val phase = InstrumentationRegistry.getArguments().getString("restartPhase")
        val app = instrumentation.targetContext.applicationContext as Application
        val prefs = app.getSharedPreferences("guide-learning-v1", 0)
        when (phase) {
            "seed" -> {
                assertTrue(prefs.edit().clear().commit())
                instrumentation.runOnMainSync {
                    GuideModel(app).apply {
                        favorite("prayer")
                        goal("fasting")
                    }
                }
            }
            "verify" -> {
                val model = GuideModel(app)
                assertEquals(setOf("prayer"), model.favorites)
                assertEquals(setOf("fasting"), model.goals)
            }
            else -> error("Missing restart phase")
        }
    }
}
