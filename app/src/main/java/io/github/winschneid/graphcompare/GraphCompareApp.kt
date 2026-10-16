package io.github.winschneid.graphcompare

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import io.github.winschneid.graphcompare.core.ChartScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun GraphCompareApp(initialScreen: ChartScreen?) {
    var selectedRoute by rememberSaveable { mutableStateOf(initialScreen?.route) }
    val selected = ChartScreen.fromRoute(selectedRoute)

    BackHandler(enabled = selected != null) { selectedRoute = null }

    Scaffold(
        // UiAutomator の By.res() で testTag を探せるようにする
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(selected?.title ?: "Graph Compare")
                        Text(BuildConfig.CHART_LIBRARY, style = MaterialTheme.typography.labelMedium)
                    }
                },
            )
        },
    ) { innerPadding ->
        if (selected == null) {
            ChartList(
                contentPadding = innerPadding,
                onClick = { selectedRoute = it.route },
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
            ) {
                Text(selected.description, style = MaterialTheme.typography.bodyMedium)
                FlavorChart(
                    screen = selected,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .testTag(selected.testTag),
                )
            }
        }
    }
}

@Composable
private fun ChartList(contentPadding: PaddingValues, onClick: (ChartScreen) -> Unit) {
    LazyColumn(contentPadding = contentPadding) {
        items(ChartScreen.entries) { screen ->
            ListItem(
                headlineContent = { Text(screen.title) },
                supportingContent = { Text(screen.description) },
                modifier = Modifier
                    .clickable { onClick(screen) }
                    .testTag("list_${screen.route}"),
            )
            HorizontalDivider()
        }
    }
}
