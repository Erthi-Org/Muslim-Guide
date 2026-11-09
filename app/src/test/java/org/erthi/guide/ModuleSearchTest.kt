package org.erthi.guide

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleSearchTest {
    @Test fun unaccentedQueryFindsLocalizedTitles() {
        assertTrue(matchesModuleQuery("Éthique", "  ethique "))
        assertTrue(matchesModuleQuery("الصَّلَاة", "الصلاة"))
    }

    @Test fun unrelatedQueryDoesNotMatch() {
        assertFalse(matchesModuleQuery("Éthique", "zakat"))
    }
}
