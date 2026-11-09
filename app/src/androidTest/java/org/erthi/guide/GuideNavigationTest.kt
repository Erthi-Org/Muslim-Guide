package org.erthi.guide

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class GuideNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun scrollTo(label: String) {
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(label))
    }

    @Test fun searchDoesNotHideFavoritesAfterReturningFromDetail() {
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
        val favorite = rule.activity.getString(R.string.add_favorite)
        scrollTo(favorite)
        rule.onNodeWithText(favorite).performClick()
        rule.activityRule.scenario.recreate()
        val remove = rule.activity.getString(R.string.remove_favorite)
        scrollTo(remove)
        rule.onNodeWithText(remove).assertIsDisplayed()
        val back = rule.activity.getString(R.string.back)
        scrollTo(back)
        rule.onNodeWithText(back).performClick()
        scrollTo(search)
        rule.onNode(hasSetTextAction()).performTextReplacement("zz-nonexistent")
        val favorites = rule.activity.getString(R.string.favorites)
        scrollTo(favorites)
        rule.onNodeWithText(favorites).performClick()
        scrollTo(title)
        rule.onNodeWithText(title).assertIsDisplayed()
        scrollTo(remove)
        rule.onNodeWithText(remove).performClick()
    }
}
