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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
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

private val night = Color(0xFF071528)
private val gold = Color(0xFFFFD581)

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
    var query by rememberSaveable { mutableStateOf("") }
    val bitmap = ImageBitmap.imageResource(R.drawable.guide_reference)
    val detail = modules.firstOrNull { it.id == selected }
    val searchable = modules.map { it to stringResource(it.title) }
    androidx.activity.compose.BackHandler(selected != null || section != "home") {
        if (selected != null) selected = null else section = "home"
    }
    Column(Modifier.fillMaxSize().background(night).safeDrawingPadding().imePadding()) {
        BoxWithConstraints(Modifier.weight(1f)) {
            val compact = maxWidth < 320.dp || LocalDensity.current.fontScale >= 1.35f
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (detail != null) {
                    item(key = "detail") {
                        val done = detail.id in model.finished
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(onClick = { selected = null }) { Text(stringResource(R.string.back), color = gold) }
                            IllustratedModule(detail, bitmap, Modifier.fillMaxWidth(), true)
                            if (detail.id == "supplications") QuranSupplications()
                            else Text(stringResource(R.string.pending), color = Color.White)
                            Button(onClick = { model.goal(detail.id) }, enabled = detail.id !in model.goals) {
                                Text(stringResource(if (detail.id in model.goals) R.string.target_saved else R.string.goal))
                            }
                            if (detail.id in model.goals) {
                                GoalCompletion(done) { model.toggleFinished(detail.id) }
                            }
                            FavoriteButton(detail, model)
                        }
                    }
                } else if (section == "home") {
                    item(key = "hero") { GuideHero(bitmap, onSettings) }
                    val visible = searchable.filter { matchesModuleQuery(it.second, query) }.map { it.first }
                    if (query.isBlank()) {
                        moduleRows(visible.take(3), if (compact) 2 else 3, bitmap, true) { selected = it.id }
                        moduleRows(visible.drop(3), if (compact) 2 else 4, bitmap, false) { selected = it.id }
                    } else {
                        moduleRows(visible, if (compact) 2 else 3, bitmap, false) { selected = it.id }
                    }
                    item(key = "search") {
                        OutlinedTextField(
                            value = query, onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            label = { Text(stringResource(R.string.search)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = if (query.isEmpty()) null else ({
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Close, stringResource(R.string.clear_search))
                                }
                            }),
                            shape = RoundedCornerShape(28.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedLabelColor = gold, unfocusedLabelColor = Color(0xFFD7E6FB),
                                focusedLeadingIconColor = gold, unfocusedLeadingIconColor = gold,
                                focusedTrailingIconColor = gold, unfocusedTrailingIconColor = gold,
                                focusedBorderColor = gold, unfocusedBorderColor = Color(0xFF526C9B)
                            )
                        )
                        if (visible.isEmpty()) Text(stringResource(R.string.no_search_results), color = Color.White)
                    }
                } else {
                    val title = when (section) {
                        "favorites" -> R.string.favorites
                        "progress" -> R.string.progress
                        else -> R.string.library
                    }
                    item(key = "section-title") {
                        Text(stringResource(title), color = gold, style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.semantics { heading() })
                        if (section == "progress") Text(
                            stringResource(R.string.goal_progress, model.finished.size, model.goals.size),
                            color = Color.White
                        )
                    }
                    val list = when (section) {
                        "favorites" -> modules.filter { it.id in model.favorites }
                        "progress" -> modules.filter { it.id in model.goals }
                        else -> modules
                    }
                    if (list.isEmpty()) item { Text(stringResource(R.string.empty), color = Color.White) }
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
        GuideNavigation(section, onSettings) { section = it; selected = null }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.moduleRows(
    modules: List<LearningModule>, columns: Int, bitmap: ImageBitmap,
    featured: Boolean, open: (LearningModule) -> Unit
) {
    items(modules.chunked(columns), key = { row -> "row-" + row.first().id }) { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.asReversed().forEach { module ->
                IllustratedModule(module, bitmap, Modifier.weight(1f), featured) { open(module) }
            }
            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun GuideHero(bitmap: ImageBitmap, onSettings: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF061020), Color(0xFF10274B), night)))
    ) {
        ReferenceArt(bitmap, Art(0, 152, 177, 227),
            Modifier.align(Alignment.BottomEnd).width(77.dp).height(104.dp))
        ReferenceArt(bitmap, Art(715, 152, 147, 227),
            Modifier.align(Alignment.BottomStart).width(67.dp).height(104.dp))
        IconButton(onClick = onSettings, modifier = Modifier.align(Alignment.TopStart)) {
            Icon(Icons.Default.Settings, stringResource(R.string.settings), tint = gold)
        }
        Column(Modifier.align(Alignment.TopCenter).padding(horizontal = 48.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            ReferenceArt(bitmap, Art(327, 43, 212, 158), Modifier.size(width = 88.dp, height = 66.dp))
            Text(stringResource(R.string.app_name), color = gold, fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() })
            Text(stringResource(R.string.guide_tagline), color = Color(0xFFDEE8FA),
                style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.width(74.dp).height(2.dp).background(gold))
        }
    }
}

@Composable
private fun IllustratedModule(
    module: LearningModule, bitmap: ImageBitmap, modifier: Modifier, featured: Boolean,
    onClick: (() -> Unit)? = null
) {
    val art = moduleArt.getValue(module.id)
    val shape = RoundedCornerShape(if (featured) 20.dp else 14.dp)
    val color = Color(art.color)
    val interactive = if (onClick == null) Modifier else Modifier.clickable(role = Role.Button, onClick = onClick)
    Column(
        modifier.clip(shape).background(Brush.verticalGradient(listOf(color, Color(0xFF101B36))))
            .border(1.dp, color.copy(alpha = .86f), shape).then(interactive),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ReferenceArt(bitmap, art, Modifier.fillMaxWidth().height(if (featured) 104.dp else 60.dp))
        Box(Modifier.fillMaxWidth().heightIn(min = if (featured) 48.dp else 42.dp).padding(4.dp),
            contentAlignment = Alignment.Center) {
            Text(stringResource(module.title), color = Color.White, fontWeight = FontWeight.Bold,
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
            .background(Color(0xFF102449)).border(1.dp, Color(0xFF345C9E), shape).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val active = tab.id == section
            val tint = if (active) gold else Color(0xFFDDE9FF)
            Column(
                Modifier.weight(1f).heightIn(min = 52.dp).semantics { selected = active }
                    .clickable(role = Role.Tab) { if (tab.id == "settings") onSettings() else navigate(tab.id) }
                    .padding(horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(Modifier.size(32.dp).background(if (active) Color(0xFF554019) else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center) {
                    Icon(tab.icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                }
                Text(stringResource(tab.label), color = tint, style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun QuranSupplications() {
    val assets = LocalContext.current.assets
    val verses = remember(assets) {
        assets.open("quran/dua-uthmani-1.1.txt").bufferedReader().useLines { lines ->
            lines.filter { it.firstOrNull()?.isDigit() == true }
                .map { it.split('|', limit = 3) }.toList()
        }
    }
    Text(stringResource(R.string.quran_supplications), color = gold,
        style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
    verses.forEach { verse ->
        Surface(color = Color(0xFF122948), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectionContainer {
                    Text(verse[2], color = Color.White, style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                }
                Text("${verse[0]}:${verse[1]}", color = gold, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
    Text("Tanzil Project · https://tanzil.net", color = Color(0xFFDEE8FA),
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
        Text(stringResource(R.string.goal_finished), color = Color.White)
    }
}

@Composable private fun FavoriteButton(module:LearningModule,model:GuideModel) {
    val checked=module.id in model.favorites
    OutlinedButton(onClick={model.favorite(module.id)},modifier=Modifier.fillMaxWidth().semantics { selected = checked },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = gold)) {
        Icon(if(checked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,null)
        Spacer(Modifier.width(8.dp));Text(stringResource(if(checked) R.string.remove_favorite else R.string.add_favorite))
    }
}
