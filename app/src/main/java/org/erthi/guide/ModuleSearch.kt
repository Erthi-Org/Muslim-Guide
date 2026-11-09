package org.erthi.guide

import java.text.Normalizer

private val marks = Regex("\\p{M}+")

private fun searchable(text: String): String =
    Normalizer.normalize(text.trim(), Normalizer.Form.NFD).replace(marks, "")

internal fun matchesModuleQuery(title: String, query: String): Boolean =
    searchable(title).contains(searchable(query), ignoreCase = true)
