package com.example.daftar.ui.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import com.example.daftar.ui.components.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.daftar.backend.ledgerTotals
import com.example.daftar.backend.localDb.PageBalance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onPageClick: (Int, String, String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pages by viewModel.pages.collectAsStateWithLifecycle()
    var deleting by remember { mutableStateOf<PageBalance?>(null) }
    val event = viewModel::handleHomeEvent
    // Keep each tab's position when Crossfade disposes its outgoing content.
    val pagesListState = rememberLazyListState()
    val overviewListState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                Icons.Default.AutoStories,
                                null,
                                Modifier
                                    .padding(10.dp)
                                    .size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text(
                                "Daftar",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "A little clarity, every day",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { event(HomeEvents.OnActionButtonPressed) },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New customer") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = state.tab == HomeTab.Pages,
                    onClick = { event(HomeEvents.SelectTab(HomeTab.Pages)) },
                    icon = { Icon(Icons.Default.Contacts, null) }, label = { Text("Pages") },
                )
                NavigationBarItem(
                    selected = state.tab == HomeTab.Overview,
                    onClick = { event(HomeEvents.SelectTab(HomeTab.Overview)) },
                    icon = { Icon(Icons.Default.Money, null) }, label = { Text("Overview") },
                )
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (!state.isDialogOpen) state.error?.let { ErrorMessage(it) }
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                Crossfade(
                    targetState = state.tab,
                    animationSpec = tween(220),
                    label = "Ledger tab"
                ) { tab ->
                    val totals = ledgerTotals(pages.map { it.credit })
                    LazyColumn(
                        state = if (tab == HomeTab.Pages) pagesListState else overviewListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item(key = "summary") {
                            BalancePanel(
                                title = if (tab == HomeTab.Pages) "Customers owe you" else "Net balance",
                                amount = if (tab == HomeTab.Pages) totals.owed else totals.net,
                                caption = if (tab == HomeTab.Pages) "Your customer accounts, all in one place."
                                    else "Receivables minus customer credit.",
                            )
                        }
                        if (tab == HomeTab.Overview) {
                            item(key = "overview_heading") { SectionHeading("The bigger picture", "A clear view of your ledger") }
                            item(key = "receivables") { MetricRow("Customers owe you", totals.owed, Icons.Default.ArrowUpward, debt = true) }
                            item(key = "credit") { MetricRow("Customer credit", totals.customerCredit, Icons.Default.ArrowDownward) }
                        }
                        item(key = "customers_heading") {
                            SectionHeading("Customer pages", "${pages.size} ${if (pages.size == 1) "customer" else "customers"} in your ledger")
                        }
                        if (pages.isEmpty()) {
                            item(key = "empty") {
                                LedgerEmptyState(
                                    "Start a fresh page",
                                    "Add your first customer to keep debts and payments together.",
                                    "Add customer", { event(HomeEvents.OnActionButtonPressed) },
                                )
                            }
                        }
                        items(pages, key = { it.id }) { page ->
                            Card(
                                modifier = Modifier.fillMaxWidth().animateItem(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            ) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Row(
                                        modifier = Modifier.weight(1f)
                                            .clickable(onClickLabel = "Open ${page.name}") { onPageClick(page.id, page.name, page.number) }
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        CustomerAvatar(page.name)
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(page.name, style = MaterialTheme.typography.titleMedium,
                                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            if (page.number.isNotBlank()) Text(page.number,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            BalanceBadge(page.credit)
                                        }
                                    }
                                    IconButton(
                                        enabled = !state.isSaving,
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                        onClick = { deleting = page },
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete ${page.name}",
                                            modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (state.isDialogOpen) {
        AlertDialog(
            onDismissRequest = { event(HomeEvents.DissmisDialog) },
            icon = { Icon(Icons.Default.Contacts, null) },
            title = { Text("A new customer page") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.nameTextField,
                        onValueChange = { event(HomeEvents.OnNameTextFieldChange(it)) },
                        label = { Text("Name") }, singleLine = true, enabled = !state.isSaving,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.numberTextField,
                        onValueChange = { event(HomeEvents.OnNumberTextFieldChange(it)) },
                        label = { Text("Phone number (optional)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        enabled = !state.isSaving,
                    )
                    state.error?.let { ErrorMessage(it) }
                }
            },
            confirmButton = {
                Button(enabled = !state.isSaving, onClick = {
                    event(HomeEvents.AddPageEvent(state.nameTextField, state.numberTextField))
                }) { Text(if (state.isSaving) "Saving…" else "Add page") }
            },
            dismissButton = {
                TextButton(enabled = !state.isSaving, onClick = { event(HomeEvents.DissmisDialog) }) { Text("Cancel") }
            },
        )
    }
    deleting?.let { page ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete ${page.name}?") },
            text = { Text("This permanently deletes this page and all its debts and payments.") },
            confirmButton = {
                TextButton(enabled = !state.isSaving, onClick = {
                    event(HomeEvents.DeletePage(page.id)); deleting = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}

fun balanceLabel(balance: Long): String = when {
    balance > 0 -> "Owes you: $balance ₪"
    balance < 0 -> "Customer credit: ${-balance} ₪"
    else -> "Settled: 0 ₪"
}

@Composable
fun ErrorMessage(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(12.dp).fillMaxWidth().animateContentSize(),
    ) { Text(message, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium) }
}
