package com.example.daftar.ui.screens.pageDetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.daftar.backend.signedAmount
import com.example.daftar.backend.localDb.Transaction
import com.example.daftar.ui.screens.home.ErrorMessage
import com.example.daftar.ui.screens.home.balanceLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageDetailsScreen(
    viewModel: PageDetailsViewModel,
    pageName: String,
    pageNumber: String,
    onBack: () -> Unit,
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val balance = transactions.sumOf { signedAmount(it.credit, it.isPayed) }
    val event = viewModel::eventHandler
    var deleting by remember { mutableStateOf<Transaction?>(null) }
    var settling by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pageName) },
                navigationIcon = { IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { event(DetailsEvents.OnAddButtonPress) }) {
                Icon(Icons.Default.Add, contentDescription = "Add debt or payment")
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
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (pageNumber.isNotBlank()) Text(pageNumber)
                                Text(balanceLabel(balance), style = MaterialTheme.typography.titleLarge)
                                if (balance > 0) {
                                    TextButton(enabled = !state.isSaving, onClick = { settling = true }) {
                                        Text("Record full payment")
                                    }
                                }
                            }
                        }
                    }
                    if (transactions.isEmpty()) item {
                        Text("No entries yet. Tap + to record a debt or payment.", Modifier.padding(16.dp))
                    }
                    items(transactions, key = { it.id }) { transaction ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(transaction.details, style = MaterialTheme.typography.titleMedium)
                                    Text(if (transaction.isPayed) "Payment" else "Debt")
                                    Text(
                                        "${if (transaction.isPayed) "−" else "+"}${transaction.credit} ₪",
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                IconButton(enabled = !state.isSaving, onClick = { deleting = transaction }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete ${transaction.details}")
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
            onDismissRequest = { event(DetailsEvents.DissmissDialog) },
            title = { Text("Add entry") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TabRow(selectedTabIndex = state.tabs.ordinal) {
                        DetailsTabs.entries.forEach { tab ->
                            Tab(
                                selected = state.tabs == tab,
                                onClick = { event(DetailsEvents.UpdateTab(tab)) },
                                enabled = !state.isSaving,
                                text = { Text(if (tab == DetailsTabs.Debt) "Debt" else "Payment") },
                            )
                        }
                    }
                    Text(if (state.tabs == DetailsTabs.Debt) "Adds to what this customer owes you." else "Reduces the balance; overpayments become customer credit.")
                    OutlinedTextField(
                        value = state.nameTextField,
                        onValueChange = { event(DetailsEvents.NameTextFieldChange(it)) },
                        label = { Text("Description") }, singleLine = true, enabled = !state.isSaving,
                    )
                    OutlinedTextField(
                        value = state.priceField,
                        onValueChange = { event(DetailsEvents.PriceTextFieldChange(it)) },
                        label = { Text("Amount (whole ₪)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !state.isSaving,
                    )
                    state.error?.let { ErrorMessage(it) }
                }
            },
            confirmButton = {
                TextButton(enabled = !state.isSaving, onClick = {
                    event(DetailsEvents.AddItem(state.nameTextField, state.priceField))
                }) { Text(if (state.isSaving) "Saving…" else "Save") }
            },
            dismissButton = {
                TextButton(enabled = !state.isSaving, onClick = { event(DetailsEvents.DissmissDialog) }) { Text("Cancel") }
            },
        )
    }
    deleting?.let { transaction ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete entry?") },
            text = { Text("Delete ${transaction.details} (${transaction.credit} ₪)? The balance will be recalculated.") },
            confirmButton = {
                TextButton(enabled = !state.isSaving, onClick = {
                    event(DetailsEvents.DeleteItem(transaction)); deleting = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
    if (settling) {
        AlertDialog(
            onDismissRequest = { settling = false },
            title = { Text("Record full payment?") },
            text = { Text("Record a payment for the outstanding balance of $balance ₪. Existing entries stay in the ledger.") },
            confirmButton = {
                TextButton(enabled = !state.isSaving, onClick = {
                    event(DetailsEvents.SettleBalance); settling = false
                }) { Text("Record payment") }
            },
            dismissButton = { TextButton(onClick = { settling = false }) { Text("Cancel") } },
        )
    }
}
