package org.erthi.guide

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

/** Emulator evidence for the prayer learning path; not a TalkBack or physical-device study. */
@RunWith(AndroidJUnit4::class)
class LessonPathTest {

    @get:Rule(order = 0)
    val clearPrefsRule: TestRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                InstrumentationRegistry.getInstrumentation().targetContext
                    .getSharedPreferences("guide-learning-v1", 0).edit().clear().commit()
                base.evaluate()
            }
        }
    }

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    private fun scrollTo(text: String) {
        try {
            rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(text))
        } catch (_: Throwable) { /* already visible or scroll unavailable */ }
    }

    private fun waitForText(text: String, timeoutMs: Long = 20000) {
        rule.waitUntil(timeoutMs) {
            rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitReady() {
        rule.waitUntil(60000) {
            rule.onAllNodesWithContentDescription(rule.activity.getString(R.string.settings))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun prayerLessonPathShowsThreeStepsWithSourceAttribution() {
        waitReady()
        val arabic = rule.activity.resources.configuration.locales[0].language == "ar"
        rule.onNodeWithText(rule.activity.getString(R.string.library)).performClick()
        val prayerTitle = rule.activity.getString(R.string.learn_prayer)
        scrollTo(prayerTitle)
        rule.onNodeWithText(prayerTitle).performClick()
        val startLesson = rule.activity.getString(R.string.lesson_start)
        waitForText(startLesson)
        scrollTo(startLesson)
        rule.onNodeWithText(startLesson).assertIsDisplayed().performClick()
        val step1Text = rule.activity.getString(R.string.lesson_step, 1, 3)
        waitForText(step1Text)
        scrollTo(step1Text)
        rule.onNodeWithText(step1Text).assertExists()
        val step1Title = if (arabic) "تهيّأ للصلاة" else "Prepare for prayer"
        rule.onNodeWithText(step1Title).assertExists()
        val next = rule.activity.getString(R.string.next)
        scrollTo(next)
        rule.onNodeWithText(next).performClick()
        val step2Text = rule.activity.getString(R.string.lesson_step, 2, 3)
        waitForText(step2Text)
        rule.onNodeWithText(step2Text).assertExists()
        rule.onNodeWithText("2:43").assertExists()
        rule.onNodeWithText("Tanzil Project · https://tanzil.net").assertExists()
        scrollTo(next)
        rule.onNodeWithText(next).performClick()
        val step3Text = rule.activity.getString(R.string.lesson_step, 3, 3)
        waitForText(step3Text)
        rule.onNodeWithText(step3Text).assertExists()
        val step2Title = if (arabic) "تدرّب ثم اسأل" else "Practise and ask"
        rule.onNodeWithText(step2Title).assertExists()
        rule.onNodeWithText(prayerTitle).performClick()
        waitForText(startLesson)
        scrollTo(startLesson)
        rule.onNodeWithText(startLesson).assertIsDisplayed()
    }

    @Test fun lessonAllDoneBannerShowsWhenBothActivitiesCompleted() {
        waitReady()
        rule.onNodeWithText(rule.activity.getString(R.string.library)).performClick()
        val prayerTitle = rule.activity.getString(R.string.learn_prayer)
        scrollTo(prayerTitle)
        rule.onNodeWithText(prayerTitle).performClick()

        val activityFinished = rule.activity.getString(R.string.activity_finished)
        scrollTo(activityFinished)
        rule.onAllNodesWithText(activityFinished)[0].assertIsOff().performClick()
        rule.waitForIdle()
        // Diagnostic: first toggle must be ON before proceeding
        rule.onAllNodesWithText(activityFinished)[0].assertIsOn()
        scrollTo(activityFinished)
        rule.onAllNodesWithText(activityFinished)[1].assertIsOff().performClick()
        rule.waitForIdle()
        // Diagnostic: second toggle must be ON before proceeding
        rule.onAllNodesWithText(activityFinished)[1].assertIsOn()

        val startLesson = rule.activity.getString(R.string.lesson_start)
        scrollTo(startLesson)
        rule.onNodeWithText(startLesson).performClick()
        rule.waitForIdle()
        // Diagnostic: dump full semantic tree to logcat so CI artifact reveals the composition
        // state (overview vs lesson mode, allActivitiesDone true/false) on API 35.
        rule.onRoot().printToLog("GuideTest_tree")
        val allDone = rule.activity.getString(R.string.lesson_all_done)
        scrollTo(allDone)
        rule.onNodeWithText(allDone).assertExists()
    }
}
