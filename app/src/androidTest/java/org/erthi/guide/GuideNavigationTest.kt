package org.erthi.guide

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class GuideNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun scrollTo(label: String) {
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(label))
    }

    @Test fun searchDoesNotHideFavoritesAfterReturningFromDetail() {
        rule.onNodeWithText(rule.activity.getString(R.string.app_name)).assertIsDisplayed()
        runCatching {
            val capture = InstrumentationRegistry.getInstrumentation().uiAutomation
                .executeShellCommand("screencap -p /data/local/tmp/erthi-screenshots/guide-home.png")
            android.os.ParcelFileDescriptor.AutoCloseInputStream(capture).use { it.readBytes() }
        }.onFailure { android.util.Log.w("ErthiUiTest", "Home screenshot capture failed", it) }
        val title = rule.activity.getString(R.string.child)
        val search = rule.activity.getString(R.string.search)
        scrollTo(search)
        rule.onNode(hasSetTextAction()).performTextInput(title)
        val moduleButton = hasText(title) and hasClickAction() and !hasSetTextAction()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(moduleButton)
        rule.onNode(moduleButton).performClick()
        val pending = rule.activity.getString(R.string.pending)
        scrollTo(pending)
        rule.onNodeWithText(pending).assertIsDisplayed()
        val goal = rule.activity.getString(R.string.goal)
        scrollTo(goal)
        rule.onNodeWithText(goal).performClick()
        val finished = rule.activity.getString(R.string.goal_finished)
        scrollTo(finished)
        rule.onNodeWithText(finished).performClick()
        rule.onNodeWithText(finished).assertIsOn()
        val favorite = rule.activity.getString(R.string.add_favorite)
        scrollTo(favorite)
        rule.onNodeWithText(favorite).performClick()
        rule.activityRule.scenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        rule.waitUntil(30000) {
            rule.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }
        val remove = rule.activity.getString(R.string.remove_favorite)
        scrollTo(remove)
        rule.onNodeWithText(remove).assertIsDisplayed()
        val back = rule.activity.getString(R.string.back)
        scrollTo(back)
        rule.onNodeWithText(back).performClick()
        scrollTo(search)
        rule.onNode(hasSetTextAction()).performTextReplacement("zz-nonexistent")
        val favorites = rule.activity.getString(R.string.favorites)
        rule.onNodeWithText(favorites).performClick()
        scrollTo(title)
        rule.onNodeWithText(title).assertIsDisplayed()
        scrollTo(remove)
        rule.onNodeWithText(remove).performClick()
        rule.onNodeWithText(rule.activity.getString(R.string.progress)).performClick()
        scrollTo(finished)
        rule.onNodeWithText(finished).assertIsOn()
        rule.onNodeWithText(rule.activity.getString(R.string.goal_progress, 1, 1)).assertExists()
    }
}
