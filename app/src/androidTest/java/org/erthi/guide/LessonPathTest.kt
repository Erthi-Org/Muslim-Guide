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

    // Clear SharedPreferences BEFORE the Activity (and its ViewModel) are created.
    // createAndroidComposeRule launches the Activity during Rule setup, which runs
    // before @Before. Without this, the ViewModel reads stale SharedPreferences from
    // the previous test and caches completedActivities, causing allActivitiesDone=true
    // and making lesson_all_done appear instead of the step counter.
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

        // Step 1 of 3: first prayer activity — waitForText confirms lessonStep = 0
        // and allActivitiesDone = false (ViewModel starts clean via clearPrefsRule)
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

        // Exact v3 flow preserved (the version that passed on both API 26 and API 35):
        // • No waitForText(activityFinished) guard — polling waitUntil altered Compose
        //   test-clock state in v5-v8 so that the second toggle's completedActivities
        //   update was not merged before the allActivitiesDone check at lessonStep=0.
        // • waitForIdle() after FIRST click only (lets recomposition settle so scrollTo
        //   finds the second occurrence reliably).
        // • waitForIdle() AFTER clicking Start Lesson flushes pending frames so the
        //   combined allActivitiesDone=true + lessonStep=0 composition runs before the
        //   assertExists check.
        // • scrollTo(allDone) + assertExists (immediate check, not a 20 s poll).
        val activityFinished = rule.activity.getString(R.string.activity_finished)
        scrollTo(activityFinished)
        rule.onAllNodesWithText(activityFinished)[0].assertIsOff().performClick()
        rule.waitForIdle()
        scrollTo(activityFinished)
        rule.onAllNodesWithText(activityFinished)[1].assertIsOff().performClick()

        val startLesson = rule.activity.getString(R.string.lesson_start)
        scrollTo(startLesson)
        rule.onNodeWithText(startLesson).performClick()
        rule.waitForIdle()
        val allDone = rule.activity.getString(R.string.lesson_all_done)
        scrollTo(allDone)
        rule.onNodeWithText(allDone).assertExists()
    }
}
