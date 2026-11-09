package org.erthi.guide

import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*

class AppearanceModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("appearance", 0)
    var appearance by mutableStateOf(prefs.getString("mode", "system") ?: "system")
        private set
    var contrast by mutableStateOf(prefs.getBoolean("contrast", false))
        private set
    fun appearance(value: String) { appearance = value; prefs.edit().putString("mode", value).apply() }
    fun contrast(value: Boolean) { contrast = value; prefs.edit().putBoolean("contrast", value).apply() }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ErthiApp() }
    }
}

@Composable fun ErthiApp(appearance: AppearanceModel = viewModel()) {
    val dark = when (appearance.appearance) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    val colors = if (dark) darkColorScheme(
        primary = Color(0xFFE8CE88), onPrimary = Color(0xFF241B04), background = Color(0xFF0B1526),
        surface = Color(0xFF111E33), onSurface = Color(0xFFF6F3EA), onBackground = Color(0xFFF6F3EA),
        secondary = Color(0xFF78DDD0), surfaceVariant = Color(0xFF25334B), onSurfaceVariant = Color(0xFFF1EBDD)
    ) else lightColorScheme(primary = Color(0xFF69500C), background = Color(0xFFFAF7EF), surface = Color(0xFFFAF7EF),
        onSurface = Color(0xFF111827), onBackground = Color(0xFF111827), secondary = Color(0xFF006A61))
    val contrasted = if (!appearance.contrast) colors else colors.copy(
        background = if (dark) Color.Black else Color.White,
        surface = if (dark) Color.Black else Color.White,
        onBackground = if (dark) Color.White else Color.Black,
        onSurface = if (dark) Color.White else Color.Black,
        onSurfaceVariant = if (dark) Color.White else Color.Black,
        surfaceVariant = if (dark) Color.Black else Color.White)
    MaterialTheme(colorScheme = contrasted) { Surface(Modifier.fillMaxSize()) {
        val nav = rememberNavController()
        NavHost(nav, startDestination = "home") {
            composable("home") { HomeScreen(onSettings = { nav.navigate("settings") }) }
            composable("settings") { SettingsScreen(appearance, { nav.popBackStack() }) }
        }
    } }
}

@Composable fun PageTitle(text: String) { Text(text, style = MaterialTheme.typography.headlineMedium,
    modifier = Modifier.semantics { heading() }.padding(vertical = 8.dp)) }

@Composable fun SettingsScreen(model: AppearanceModel, back: () -> Unit) {
    val localeNames = listOf("العربية", "English", "Français", "Türkçe", "اردو", "Bahasa Indonesia", "Deutsch", "Español")
    val localeCodes = listOf("ar", "en", "fr", "tr", "ur", "id", "de", "es")
    val chosen = AppCompatDelegate.getApplicationLocales().toLanguageTags().ifBlank {
        androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language
    }.substringBefore('-')
    LazyColumn(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { TextButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null); Text(stringResource(R.string.back)) } }
        item { PageTitle(stringResource(R.string.settings)); Text(stringResource(R.string.identity)) }
        item { PageTitle(stringResource(R.string.language)) }
        items(localeCodes.indices.toList()) { index ->
            val code = localeCodes[index]
            val text = localeNames[index]
            OutlinedButton(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .semantics { role = Role.RadioButton; selected = chosen == code },
                onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code)) }) {
                RadioButton(selected = chosen == code, onClick = null); Spacer(Modifier.width(8.dp)); Text(text)
            }
        }
        item { PageTitle(stringResource(R.string.appearance)) }
        items(listOf("system" to R.string.system, "dark" to R.string.dark, "light" to R.string.light)) { (value, res) ->
            OutlinedButton(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .semantics { role = Role.RadioButton; selected = model.appearance == value },
                onClick = { model.appearance(value) }) {
                RadioButton(model.appearance == value, null); Text(stringResource(res))
            }
        }
        item { Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .toggleable(value = model.contrast, role = Role.Switch, onValueChange = model::contrast),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.high_contrast), Modifier.weight(1f))
            Switch(checked = model.contrast, onCheckedChange = null)
        } }
        item { Text(stringResource(R.string.review_notice)); Spacer(Modifier.height(20.dp)) }
    }
}
