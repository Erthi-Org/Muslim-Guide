package org.erthi.guide

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Emulator evidence is not a TalkBack user study or physical-device approval. */
@RunWith(AndroidJUnit4::class)
class FoundationUiTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private fun waitReady() { rule.waitUntil(30000) { rule.onAllNodesWithContentDescription(rule.activity.getString(R.string.settings)).fetchSemanticsNodes().isNotEmpty() } }
    @Test fun languageChangePreservesAppearanceAndDirection() {
        waitReady()
        val prefs=rule.activity.getSharedPreferences("appearance",0)
        rule.onNodeWithContentDescription(rule.activity.getString(R.string.settings)).performClick()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(rule.activity.getString(R.string.high_contrast)))
        val before=prefs.getBoolean("contrast",false)
        rule.onNode(isToggleable()).performClick()
        rule.waitForIdle()
        assertEquals(!before,prefs.getBoolean("contrast",false))
        // Exercise real application locale recreation, including Urdu RTL and long German UI.
        for(code in listOf("en","ur","de","ar")) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
            }
            rule.waitUntil(30000) { rule.activity.resources.configuration.locales[0].language==code }
            rule.waitForIdle()
            val direction=rule.activity.resources.configuration.layoutDirection
            assertEquals(if(code in listOf("ar","ur")) 1 else 0,direction)
            val title = rule.activity.getString(R.string.settings)
            rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(title))
            rule.onNodeWithText(title).assertIsDisplayed()
            val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            val dir=java.io.File(rule.activity.getExternalFilesDir(null),"screenshots").apply { mkdirs() }
            java.io.File(dir,"settings-$code.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }
            bitmap.recycle()
        }
        assertEquals(!before,prefs.getBoolean("contrast",false))
    }
    @Test fun appHasNoSensitivePermissionsOrBackup() {
        val app=rule.activity.applicationInfo
        assertEquals(0,app.flags and android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP)
        val info=rule.activity.packageManager.getPackageInfo(rule.activity.packageName,android.content.pm.PackageManager.GET_PERMISSIONS)
        assertTrue(info.requestedPermissions.orEmpty().all { it == rule.activity.packageName+".DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION" })
    }
}
