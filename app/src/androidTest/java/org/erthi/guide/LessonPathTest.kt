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

        // Step 1 of 3: first prayer activity — wait until lesson mode activates
        val step1Text = rule.activity.getString(R.string.lesson_step, 1, 3)
        waitForText(step1Text)
        scrollTo(step1Text)
        rule.onNodeWithText(step1Text).assertExists()
        // Activity title: JSON ar[0]="تهيّأ للصلاة" en[2]="Prepare for prayer"
        val step1Title = if (arabic) "تهيّأ للصلاة" else "Prepare for prayer"
        rule.onNodeWithText(step1Title).assertExists()

        // Step 2 of 3: Quranic reading with source attribution
        val next = rule.activity.getString(R.string.next)
        scrollTo(next)
        rule.onNodeWithText(next).performClick()
        val step2Text = rule.activity.getString(R.string.lesson_step, 2, 3)
        waitForText(step2Text)
        rule.onNodeWithText(step2Text).assertExists()
        rule.onNodeWithText("2:43").assertExists()
        rule.onNodeWithText("Tanzil Project · https://tanzil.net").assertExists()

        // Step 3 of 3: second prayer activity
        scrollTo(next)
        rule.onNodeWithText(next).performClick()
        val step3Text = rule.activity.getString(R.string.lesson_step, 3, 3)
        waitForText(step3Text)
        rule.onNodeWithText(step3Text).assertExists()
        // Activity title: JSON ar[0]="تدرّب ثم اسأل" en[2]="Practise and ask"
        val step2Title = if (arabic) "تدرّب ثم اسأل" else "Practise and ask"
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
        // Wait for the second toggle to commit before reading completion state
        rule.waitForIdle()

        // Start lesson: both activities already done, banner appears immediately
        val startLesson = rule.activity.getString(R.string.lesson_start)
        waitForText(startLesson)
        scrollTo(startLesson)
        rule.onNodeWithText(startLesson).performClick()
        val allDone = rule.activity.getString(R.string.lesson_all_done)
        waitForText(allDone)
        rule.onNodeWithText(allDone).assertExists()
    }
}
