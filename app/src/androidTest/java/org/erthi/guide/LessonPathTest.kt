package org.erthi.guide

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Emulator evidence for the prayer learning path; not a TalkBack or physical-device study. */
@RunWith(AndroidJUnit4::class)
class LessonPathTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun clearLearningState() {
        InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("guide-learning-v1", 0).edit().clear().commit()
    }

    private fun scrollTo(text: String) {
        try {
            rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(text))
        } catch (_: Throwable) { /* already visible or scroll unavailable */ }
    }

    private fun waitForText(text: String, timeoutMs: Long = 10000) {
        rule.waitUntil(timeoutMs) {
            rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitReady() {
        rule.waitUntil(30000) {
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
        // Wait until detail renders the Start Lesson button, then scroll to it and click
        waitForText(startLesson)
        scrollTo(startLesson)
        rule.onNodeWithText(startLesson).assertIsDisplayed().performClick()

        // Wait until lesson mode activates (step counter appears in composition tree)
        val step1Text = rule.activity.getString(R.string.lesson_step, 1, 3)
        waitForText(step1Text)
        scrollTo(step1Text)
        rule.onNodeWithText(step1Text).assertIsDisplayed()
        val step1Title = if (arabic) "تهيّأ للصلاة" else "Prepare for prayer"
        scrollTo(step1Title)
        rule.onNodeWithText(step1Title).assertExists()

        // Step 2 of 3: Quranic reading with source attribution
        val next = rule.activity.getString(R.string.next)
        scrollTo(next)
        rule.onNodeWithText(next).performClick()
        val step2Text = rule.activity.getString(R.string.lesson_step, 2, 3)
        waitForText(step2Text)
        scrollTo(step2Text)
        rule.onNodeWithText(step2Text).assertIsDisplayed()
        scrollTo("2:43")
        rule.onNodeWithText("2:43").assertExists()
        rule.onNodeWithText("Tanzil Project · https://tanzil.net").assertExists()

        // Step 3 of 3: second prayer activity
        scrollTo(next)
        rule.onNodeWithText(next).performClick()
        val step3Text = rule.activity.getString(R.string.lesson_step, 3, 3)
        waitForText(step3Text)
        scrollTo(step3Text)
        rule.onNodeWithText(step3Text).assertIsDisplayed()
        val step2Title = if (arabic) "تدرّب وسأل" else "Practise and ask"
        scrollTo(step2Title)
        rule.onNodeWithText(step2Title).assertExists()

        // Module title button exits lesson mode; Start Lesson reappears
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

        // Complete both prayer activities from the full module detail view
        val activityFinished = rule.activity.getString(R.string.activity_finished)
        waitForText(activityFinished)
        scrollTo(activityFinished)
        rule.onAllNodesWithText(activityFinished)[0].assertIsOff().performClick()
        rule.waitForIdle()
        scrollTo(activityFinished)
        rule.onAllNodesWithText(activityFinished)[1].assertIsOff().performClick()

        // Start lesson: both activities already done, banner appears immediately
        val startLesson = rule.activity.getString(R.string.lesson_start)
        waitForText(startLesson)
        scrollTo(startLesson)
        rule.onNodeWithText(startLesson).performClick()
        val allDone = rule.activity.getString(R.string.lesson_all_done)
        waitForText(allDone)
        scrollTo(allDone)
        rule.onNodeWithText(allDone).assertExists()
    }
}
