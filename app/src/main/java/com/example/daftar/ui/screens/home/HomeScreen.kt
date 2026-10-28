package com.example.daftar.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Money
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

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Daftar") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { event(HomeEvents.OnActionButtonPressed) }) {
                Icon(Icons.Default.Add, contentDescription = "Add customer page")
            }
        },
        bottomBar = {
            NavigationBar {
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
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.tab == HomeTab.Overview) {
                        val totals = ledgerTotals(pages.map { it.credit })
                        item {
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Balance overview", style = MaterialTheme.typography.titleLarge)
                                    Text("Customers owe you: ${totals.owed} ₪")
                                    Text("Customer credit: ${totals.customerCredit} ₪")
                                    Text("Net balance: ${totals.net} ₪")
                                    Text("${pages.size} customer pages")
                                }
                            }
                        }
                    }
                    if (pages.isEmpty()) {
                        item { Text("No customer pages yet. Tap + to add your first page.", Modifier.padding(16.dp)) }
                    }
                    items(pages, key = { it.id }) { page ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    Modifier.weight(1f).clickable { onPageClick(page.id, page.name, page.number) }.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(page.name, style = MaterialTheme.typography.titleMedium)
                                    if (page.number.isNotBlank()) Text(page.number)
                                    Text(balanceLabel(page.credit), color = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(enabled = !state.isSaving, onClick = { deleting = page }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete ${page.name}")
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
            title = { Text("Add customer page") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.nameTextField,
                        onValueChange = { event(HomeEvents.OnNameTextFieldChange(it)) },
                        label = { Text("Name") }, singleLine = true, enabled = !state.isSaving,
                    )
                    OutlinedTextField(
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
                TextButton(enabled = !state.isSaving, onClick = {
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
    Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
}
