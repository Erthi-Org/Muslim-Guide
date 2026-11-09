package org.erthi.guide

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/** Stable identifiers survive locale changes; no reviewed religious lessons are fabricated. */
data class LearningModule(val id: String, val title: Int)
val modules=listOf(
    LearningModule("child",R.string.child),LearningModule("prayer",R.string.learn_prayer),
    LearningModule("hisn",R.string.hisn),LearningModule("prophets",R.string.prophets),
    LearningModule("manners",R.string.manners),LearningModule("creed",R.string.creed),
    LearningModule("seerah",R.string.seerah),LearningModule("supplications",R.string.supplications),
    LearningModule("zakat",R.string.zakat),LearningModule("fasting",R.string.fasting),LearningModule("hajj",R.string.hajj))

class GuideModel(app: Application): AndroidViewModel(app) {
    private val prefs=app.getSharedPreferences("guide-learning-v1",0)
    var favorites by mutableStateOf(prefs.getStringSet("favorites",emptySet())!!.toSet())
        private set
    var goals by mutableStateOf(prefs.getStringSet("goals",emptySet())!!.toSet())
        private set
    var finished by mutableStateOf(prefs.getStringSet("finished", emptySet()).orEmpty().toSet())
        private set

    fun toggleFinished(id: String) {
        if (id !in goals) return
        val updated = if (id in finished) finished - id else finished + id
        if (prefs.edit().putStringSet("finished", updated).commit()) finished = updated
    }

    fun favorite(id:String) {
        if(modules.none { it.id==id }) return
        val updated = if (id in favorites) favorites - id else favorites + id
        // Publish the selection only after it can survive a process stop.
        if (prefs.edit().putStringSet("favorites", updated).commit()) {
            favorites = updated
        }
    }
    fun goal(id:String) {
        if(modules.none { it.id==id }) return
        if (id in goals) return
        val updated = goals + id
        if (prefs.edit().putStringSet("goals", updated).commit()) {
            goals = updated
        }
    }
}


private data class Art(val x: Int, val y: Int, val w: Int, val h: Int, val color: Long = 0xFF0C3464)

// Source rectangles contain illustrations only. Text and controls are native Compose elements.
private val moduleArt = mapOf(
    "child" to Art(34, 405, 244, 211, 0xFF40217C),
    "prayer" to Art(320, 403, 231, 209, 0xFF094D81),
    "hisn" to Art(591, 401, 238, 213, 0xFF075B49),
    "prophets" to Art(35, 799, 169, 110, 0xFF754717),
    "manners" to Art(244, 800, 163, 108, 0xFF075847),
    "creed" to Art(453, 800, 166, 107),
    "seerah" to Art(659, 799, 167, 111, 0xFF263A83),
    "supplications" to Art(36, 1010, 167, 112, 0xFF552A79),
    "zakat" to Art(244, 1011, 164, 108, 0xFF0C564F),
    "fasting" to Art(453, 1009, 166, 110, 0xFF30317C),
    "hajj" to Art(660, 1009, 166, 111)
)

@Composable
private fun ReferenceArt(bitmap: ImageBitmap, art: Art, modifier: Modifier) {
    val painter = remember(bitmap, art) {
        BitmapPainter(bitmap, IntOffset(art.x, art.y), IntSize(art.w, art.h))
    }
    Image(painter, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
}

@Composable
fun HomeScreen(onSettings: () -> Unit, model: GuideModel = viewModel()) {
    var section by rememberSaveable { mutableStateOf("home") }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedActivity by rememberSaveable { mutableStateOf<Int?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val browseListState = rememberLazyListState()
    val searchListState = rememberLazyListState()
    val detailListState = rememberLazyListState()
    val bitmap = ImageBitmap.imageResource(R.drawable.guide_reference)
    val detail = modules.firstOrNull { it.id == selected }
    val assets = LocalContext.current.assets
    val arabic = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == "ar"
    val activities = remember(assets, arabic) {
        val data = assets.open("learning-activities.json").bufferedReader().use {
            org.json.JSONObject(it.readText())
        }
        modules.associate { module ->
            val entries = data.getJSONArray(module.id)
            module.id to List(entries.length()) { index ->
                val entry = entries.getJSONArray(index)
                val offset = if (arabic) 0 else 2
                LearningActivity(entry.getString(offset), entry.getString(offset + 1))
            }
        }
    }
    val visible = modules.filter { matchesModuleQuery(stringResource(it.title), query) }
    val activityMatches = remember(query, activities) {
        if (query.isBlank()) emptyList() else modules.flatMap { module ->
            activities.getValue(module.id).mapIndexedNotNull { index, activity ->
                if (matchesModuleQuery(activity.title, query) || matchesModuleQuery(activity.body, query)) {
                    ActivityMatch(module, index, activity)
                } else null
            }
        }
    }
    fun closeDetail() {
        selected = null
        selectedActivity = null
    }
    androidx.activity.compose.BackHandler(selected != null || section != "home") {
        if (selected != null) closeDetail() else section = "home"
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("guide-root").safeDrawingPadding().imePadding()) {
        BoxWithConstraints(Modifier.weight(1f)) {
            val compact = maxWidth < 320.dp || LocalDensity.current.fontScale >= 1.35f
            val extraArt = if (compact || query.isNotBlank()) 0.dp
                else ((maxHeight - 420.dp).coerceIn(0.dp, 180.dp) / 3f)
            LazyColumn(
                state = when {
                    detail != null -> detailListState
                    section == "home" && query.isNotBlank() -> searchListState
                    else -> browseListState
                },
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (detail != null) {
                    item(key = "detail") {
                        val done = detail.id in model.finished
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(onClick = ::closeDetail) { Text(stringResource(R.string.back), color = MaterialTheme.colorScheme.primary) }
                            PageTitle(stringResource(detail.title))
                            val lessons = activities.getValue(detail.id)
                            val focused = selectedActivity?.let(lessons::getOrNull)
                            if (focused != null) {
                                LearningActivityCard(focused)
                                TextButton(onClick = { selectedActivity = null }) {
                                    Text(stringResource(detail.title))
                                }
                                QuranReading(detail.id)
                                return@Column
                            }
                            lessons.forEach { LearningActivityCard(it) }

                            Button(onClick = { model.goal(detail.id) }, enabled = detail.id !in model.goals) {
                                Text(stringResource(if (detail.id in model.goals) R.string.target_saved else R.string.goal))
                            }
                            if (detail.id in model.goals) {
                                GoalCompletion(done) { model.toggleFinished(detail.id) }
                            }
                            FavoriteButton(detail, model)
                            QuranReading(detail.id)
                        }
                    }
                } else if (section == "home") {
                    if (query.isBlank()) {
                        item(key = "hero") { GuideHero(bitmap, onSettings) }
                        moduleRows(visible.take(3), if (compact) 2 else 3, bitmap, true, extraArt) { selected = it.id }
                        moduleRows(visible.drop(3), if (compact) 2 else 4, bitmap, false, extraArt) { selected = it.id }
                    } else {
                        moduleRows(visible, if (compact) 2 else 3, bitmap, false) { selected = it.id }
                        items(activityMatches, key = { "activity-${it.module.id}-${it.index}" }) { result ->
                            Surface(
                                onClick = {
                                    selected = result.module.id
                                    selectedActivity = result.index
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(result.activity.title, style = MaterialTheme.typography.titleMedium)
                                    Text(stringResource(result.module.title), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    if (visible.isEmpty() && activityMatches.isEmpty()) item {
                        Text(stringResource(R.string.no_search_results), color = MaterialTheme.colorScheme.onSurface)
                    }
                } else {
                    val title = when (section) {
                        "favorites" -> R.string.favorites
                        "progress" -> R.string.progress
                        else -> R.string.library
                    }
                    item(key = "section-title") {
                        Text(stringResource(title), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.semantics { heading() })
                        if (section == "progress") Text(
                            stringResource(R.string.goal_progress, model.finished.size, model.goals.size),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    val list = when (section) {
                        "favorites" -> modules.filter { it.id in model.favorites }
                        "progress" -> modules.filter { it.id in model.goals }
                        else -> modules
                    }
                    if (list.isEmpty()) item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(if (section == "favorites") R.string.favorites_hint else R.string.goals_hint))
                            Button(onClick = { section = "library" }) { Text(stringResource(R.string.library)) }
                        }
                    }
                    if (section == "library") {
                        moduleRows(list, if (compact) 2 else 4, bitmap, false) { selected = it.id }
                    } else {
                        items(list, key = { it.id }) { module ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                IllustratedModule(module, bitmap, Modifier.fillMaxWidth(), false) { selected = module.id }
                                if (section == "progress") GoalCompletion(module.id in model.finished) {
                                    model.toggleFinished(module.id)
                                } else FavoriteButton(module, model)
                            }
                        }
                    }
                }
            }
        }
        if (section == "home" && detail == null) {
            val focusManager = LocalFocusManager.current
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), singleLine = true,
                label = { Text(stringResource(R.string.search)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (query.isEmpty()) null else ({
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Close, stringResource(R.string.clear_search))
                    }
                }),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary, unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary, unfocusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    focusedTrailingIconColor = MaterialTheme.colorScheme.primary, unfocusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
        GuideNavigation(section, onSettings) { section = it; closeDetail() }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.moduleRows(
    modules: List<LearningModule>, columns: Int, bitmap: ImageBitmap,
    featured: Boolean, extraArt: Dp = 0.dp, open: (LearningModule) -> Unit
) {
    items(modules.chunked(columns), key = { row -> "row-" + row.first().id }) { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.asReversed().forEach { module ->
                IllustratedModule(module, bitmap, Modifier.weight(1f), featured, extraArt) { open(module) }
            }
            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun GuideHero(bitmap: ImageBitmap, onSettings: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.background)))
    ) {
        ReferenceArt(bitmap, Art(0, 152, 177, 227),
            Modifier.align(Alignment.BottomEnd).width(77.dp).height(104.dp))
        ReferenceArt(bitmap, Art(715, 152, 147, 227),
            Modifier.align(Alignment.BottomStart).width(67.dp).height(104.dp))
        IconButton(onClick = onSettings, modifier = Modifier.align(Alignment.TopStart)) {
            Icon(Icons.Default.Settings, stringResource(R.string.settings), tint = MaterialTheme.colorScheme.primary)
        }
        Column(Modifier.align(Alignment.TopCenter).padding(horizontal = 48.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            ReferenceArt(bitmap, Art(327, 43, 212, 158), Modifier.size(width = 88.dp, height = 66.dp))
            Text(stringResource(R.string.app_name), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() })
            Text(stringResource(R.string.guide_tagline), color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.width(74.dp).height(2.dp).background(MaterialTheme.colorScheme.primary))
        }
    }
}

@Composable
private fun IllustratedModule(
    module: LearningModule, bitmap: ImageBitmap, modifier: Modifier, featured: Boolean,
    extraArt: Dp = 0.dp, onClick: (() -> Unit)? = null
) {
    val art = moduleArt.getValue(module.id)
    val shape = RoundedCornerShape(if (featured) 20.dp else 14.dp)
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < .5f
    val color = lerp(scheme.surface, Color(art.color), if (dark) .7f else .15f)
    val interactive = if (onClick == null) Modifier else Modifier.clickable(role = Role.Button, onClick = onClick)
    Column(
        modifier.clip(shape).background(Brush.verticalGradient(listOf(color, scheme.surface)))
            .border(1.dp, color.copy(alpha = .86f), shape).then(interactive),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ReferenceArt(bitmap, art, Modifier.fillMaxWidth().height((if (featured) 88.dp else 48.dp) + extraArt))
        Box(Modifier.fillMaxWidth().heightIn(min = if (featured) 44.dp else 36.dp).padding(4.dp),
            contentAlignment = Alignment.Center) {
            Text(stringResource(module.title), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, style = if (featured) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelMedium)
        }
    }
}

private data class GuideTab(val id: String, val label: Int, val icon: ImageVector)
private val tabs = listOf(
    GuideTab("progress", R.string.progress, Icons.Default.BarChart),
    GuideTab("favorites", R.string.favorites, Icons.Default.BookmarkBorder),
    GuideTab("home", R.string.home, Icons.Default.Home),
    GuideTab("library", R.string.library, Icons.AutoMirrored.Filled.MenuBook),
    GuideTab("settings", R.string.more, Icons.Default.Apps)
)

@Composable
private fun GuideNavigation(section: String, onSettings: () -> Unit, navigate: (String) -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp).clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant).border(1.dp, MaterialTheme.colorScheme.outline, shape).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val active = tab.id == section
            val tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            Column(
                Modifier.weight(1f).heightIn(min = 52.dp).semantics { selected = active }
                    .clickable(role = Role.Tab) { if (tab.id == "settings") onSettings() else navigate(tab.id) }
                    .padding(horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(Modifier.size(32.dp).background(if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center) {
                    Icon(tab.icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                }
                Text(stringResource(tab.label), color = tint, style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

private data class LearningActivity(val title: String, val body: String)
private data class ActivityMatch(val module: LearningModule, val index: Int, val activity: LearningActivity)

@Composable
private fun LearningActivityCard(activity: LearningActivity) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(activity.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            Text(activity.body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private val readingReferences = mapOf(
    "child" to listOf("31:13"),
    "prayer" to listOf("2:43"),
    "hisn" to listOf("13:28"),
    "prophets" to listOf("12:111"),
    "manners" to listOf("49:11"),
    "creed" to listOf("112:1", "112:2", "112:3", "112:4"),
    "seerah" to listOf("33:21"),
    "supplications" to listOf("2:201", "7:23", "14:40"),
    "zakat" to listOf("2:110"),
    "fasting" to listOf("2:183"),
    "hajj" to listOf("22:27")
)

@Composable
private fun QuranReading(moduleId: String) {
    val assets = LocalContext.current.assets
    val verses = remember(assets) {
        assets.open("quran/dua-uthmani-1.1.txt").bufferedReader().useLines { lines ->
            lines.filter { it.firstOrNull()?.isDigit() == true }
                .map { it.split('|', limit = 3) }.toList()
        }
    }
    val byReference = verses.associateBy { "${it[0]}:${it[1]}" }
    Text(stringResource(if (moduleId == "supplications") R.string.quran_supplications else R.string.quran_reading), color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
    readingReferences.getValue(moduleId).map(byReference::getValue).forEach { verse ->
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectionContainer {
                    Text(verse[2], color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                }
                Text("${verse[0]}:${verse[1]}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
    Text(stringResource(R.string.pending), color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text("Tanzil Project · https://tanzil.net", color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium)
}

@Composable
private fun GoalCompletion(done: Boolean, change: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .toggleable(value = done, role = Role.Checkbox, onValueChange = { change() }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = done, onCheckedChange = null, modifier = Modifier.padding(end = 8.dp))
        Text(stringResource(R.string.goal_finished), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable private fun FavoriteButton(module:LearningModule,model:GuideModel) {
    val checked=module.id in model.favorites
    OutlinedButton(onClick={model.favorite(module.id)},modifier=Modifier.fillMaxWidth().semantics { selected = checked },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
        Icon(if(checked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,null)
        Spacer(Modifier.width(8.dp));Text(stringResource(if(checked) R.string.remove_favorite else R.string.add_favorite))
    }
}
