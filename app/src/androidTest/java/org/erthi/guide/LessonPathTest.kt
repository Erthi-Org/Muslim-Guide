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
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(text))
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
        scrollTo(startLesson)
        rule.onNodeWithText(startLesson).assertIsDisplayed().performClick()

        // Step 1 of 3: first prayer activity
        rule.onNodeWithText(rule.activity.getString(R.string.lesson_step, 1, 3)).assertIsDisplayed()
        val step1Title = if (arabic) "تهيّأ للصلاة" else "Prepare for prayer"
        scrollTo(step1Title)
        rule.onNodeWithText(step1Title).assertIsDisplayed()

        // Step 2 of 3: Quranic reading with source attribution
        val next = rule.activity.getString(R.string.next)
        scrollTo(next)
        rule.onNodeWithText(next).performClick()
        rule.onNodeWithText(rule.activity.getString(R.string.lesson_step, 2, 3)).assertIsDisplayed()
        scrollTo("2:43")
        rule.onNodeWithText("2:43").assertIsDisplayed()
        rule.onNodeWithText("Tanzil Project · https://tanzil.net").assertIsDisplayed()

        // Step 3 of 3: second prayer activity
        scrollTo(next)
        rule.onNodeWithText(next).performClick()
        rule.onNodeWithText(rule.activity.getString(R.string.lesson_step, 3, 3)).assertIsDisplayed()
        val step2Title = if (arabic) "تدرّب ثم اسأل" else "Practise and ask"
        scrollTo(step2Title)
        rule.onNodeWithText(step2Title).assertIsDisplayed()

        // Module title button exits lesson mode; Start Lesson reappears
        rule.onNodeWithText(prayerTitle).performClick()
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
        scrollTo(activityFinished)
        rule.onAllNodesWithText(activityFinished)[0].assertIsOff().performClick()
        rule.onAllNodesWithText(activityFinished)[1].assertIsOff().performClick()

        // Start lesson: both activities already done, banner appears immediately
        val startLesson = rule.activity.getString(R.string.lesson_start)
        scrollTo(startLesson)
        rule.onNodeWithText(startLesson).performClick()
        rule.onNodeWithText(rule.activity.getString(R.string.lesson_all_done)).assertIsDisplayed()
    }
}
