package org.erthi.guide

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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

@Composable fun HomeScreen(onSettings:()->Unit,model:GuideModel=viewModel()) {
    var section by rememberSaveable { mutableStateOf("home") }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    androidx.activity.compose.BackHandler(selected!=null || section!="home") {
        if(selected!=null) selected=null else section="home"
    }
    LazyColumn(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Row(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.app_name),style=MaterialTheme.typography.headlineMedium,modifier=Modifier.weight(1f).semantics { heading() })
            IconButton(onClick=onSettings) { Icon(Icons.Default.Settings,stringResource(R.string.settings)) }
        };Text(stringResource(R.string.identity),style=MaterialTheme.typography.labelMedium)
            Text(stringResource(R.string.review_notice),style=MaterialTheme.typography.bodySmall) }
        item {
            // Full-width labels remain readable at large font sizes and in long translations.
            listOf("home" to R.string.home,"favorites" to R.string.favorites,"library" to R.string.library,"progress" to R.string.progress).forEach { (id,res) ->
                OutlinedButton(onClick={section=id;selected=null},modifier=Modifier.fillMaxWidth().semantics { this.selected = section == id }) {
                    Text(stringResource(res));if(section==id) { Spacer(Modifier.width(8.dp));Icon(Icons.Default.Check,null) }
                }
            }
        }
        val detail=modules.firstOrNull { it.id==selected }
        if(detail!=null) {
            item {
                TextButton(onClick={selected=null}) { Text(stringResource(R.string.back)) }
                PageTitle(stringResource(detail.title))
                Text(stringResource(R.string.pending))
                Button(onClick={model.goal(detail.id)},modifier=Modifier.fillMaxWidth()) {
                    Text(stringResource(if(detail.id in model.goals) R.string.target_saved else R.string.goal))
                }
                FavoriteButton(detail,model)
            }
        } else if(section=="library") {
            item { PageTitle(stringResource(R.string.library));Text(stringResource(R.string.pending));PageTitle(stringResource(R.string.sources));Text("https://erthi.org · admin@erthi.org") }
        } else {
            if (section == "home") item { OutlinedTextField(value=query,onValueChange={query=it},label={Text(stringResource(R.string.search))},modifier=Modifier.fillMaxWidth(),singleLine=true) }
            // Resolve localized titles before filtering; state stores stable IDs, never translated labels.
            items(modules,key={it.id}) { module ->
                val title=stringResource(module.title)
                if((section!="favorites" || module.id in model.favorites) && (section!="progress" || module.id in model.goals) && (section != "home" || matchesModuleQuery(title, query))) {
                    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                        Icon(when(module.id) {
                            "child"->Icons.Default.ChildCare;"prayer"->Icons.Default.SelfImprovement;"hisn"->Icons.Default.Shield
                            "zakat"->Icons.Default.VolunteerActivism;"fasting"->Icons.Default.DarkMode;"hajj"->Icons.Default.Place
                            else->Icons.AutoMirrored.Filled.MenuBook
                        },contentDescription=null,tint=when(module.id) { "child"->Color(0xFF65B5E8);"zakat"->Color(0xFF66BD92);else->MaterialTheme.colorScheme.primary },modifier=Modifier.size(40.dp))
                        TextButton(onClick={selected=module.id},modifier=Modifier.fillMaxWidth()) { Text(title,style=MaterialTheme.typography.titleLarge) }
                        FavoriteButton(module,model)
                    } }
                }
            }
            if((section=="favorites" && model.favorites.isEmpty()) || (section=="progress" && model.goals.isEmpty())) {
                item { Text(stringResource(R.string.empty)) }
            }
            if(section=="progress") item { Text(stringResource(R.string.completed));Text(stringResource(R.string.pending)) }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
@Composable private fun FavoriteButton(module:LearningModule,model:GuideModel) {
    val checked=module.id in model.favorites
    OutlinedButton(onClick={model.favorite(module.id)},modifier=Modifier.fillMaxWidth().semantics { selected = checked }) {
        Icon(if(checked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,null)
        Spacer(Modifier.width(8.dp));Text(stringResource(if(checked) R.string.remove_favorite else R.string.add_favorite))
    }
}
