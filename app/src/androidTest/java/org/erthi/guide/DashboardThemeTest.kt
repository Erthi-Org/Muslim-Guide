package org.erthi.guide

import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardThemeTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun chooseTheme(option: Int) {
        rule.onNodeWithContentDescription(rule.activity.getString(R.string.settings)).performClick()
        val label = rule.activity.getString(option)
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(label))
        rule.onNodeWithText(label).performClick()
        val back = rule.activity.getString(R.string.back)
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(back))
        rule.onNodeWithText(back).performClick()
    }

    private fun backgroundLuminance(): Float {
        val pixels = rule.onNodeWithTag("guide-root").captureToImage().toPixelMap()
        return pixels[1, pixels.height / 2].luminance()
    }

    @Test fun homeActuallyChangesBetweenLightAndDark() {
        chooseTheme(R.string.light)
        assertTrue(backgroundLuminance() > .7f)
        chooseTheme(R.string.dark)
        assertTrue(backgroundLuminance() < .3f)
    }
}
