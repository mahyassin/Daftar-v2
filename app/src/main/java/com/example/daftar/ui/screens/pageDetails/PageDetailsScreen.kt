package com.example.daftar.ui.screens.pageDetails

import android.content.ClipData
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.ui.text.style.TextOverflow
import com.example.daftar.ui.components.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.daftar.backend.signedAmount
import com.example.daftar.backend.localDb.Transaction
import com.example.daftar.ui.screens.home.ErrorMessage
import kotlinx.coroutines.launch

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
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(pageName, style = MaterialTheme.typography.titleLarge,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Customer ledger", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = { IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                } },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { event(DetailsEvents.OnAddButtonPress) },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add entry") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (!state.isDialogOpen) state.error?.let { ErrorMessage(it) }
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "customer") {
                        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            CustomerAvatar(pageName)
                            Column(Modifier.weight(1f)) {
                                Text(pageName, style = MaterialTheme.typography.titleMedium)
                                if (pageNumber.isBlank()) {
                                    Text("Your customer account", style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(pageNumber, modifier = Modifier.weight(1f, fill = false),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Phone number", pageNumber)))
                                                    snackbarHostState.currentSnackbarData?.dismiss()
                                                    snackbarHostState.showSnackbar("Phone number copied")
                                                }
                                            },
                                            shape = CircleShape,
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy phone number",
                                                modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item(key = "balance") {
                        BalancePanel(
                            title = when {
                                balance > 0 -> "Outstanding balance"
                                balance < 0 -> "Customer credit"
                                else -> "All settled"
                            },
                            amount = if (balance < 0) -balance else balance,
                            caption = when {
                                balance > 0 -> "This is what $pageName owes you."
                                balance < 0 -> "Available credit for this customer."
                                else -> "This account is up to date."
                            },
                            modifier = Modifier.animateContentSize(),
                            action = if (balance > 0) ({
                                FilledTonalButton(
                                    enabled = !state.isSaving,
                                    onClick = { settling = true },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.onPrimary,
                                        contentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                ) { Text("Record full payment") }
                            }) else null,
                        )
                    }
                    item(key = "activity_heading") {
                        SectionHeading("Account activity", "${transactions.size} ${if (transactions.size == 1) "entry" else "entries"} · Debts & payments")
                    }
                    if (transactions.isEmpty()) item(key = "empty") {
                        LedgerEmptyState("Every entry tells a story",
                            "Record a debt or payment to start this customer's ledger.",
                            "Add first entry", { event(DetailsEvents.OnAddButtonPress) })
                    }
                    items(transactions, key = { it.id }) { transaction ->
                        val payment = transaction.isPayed
                        val accent = if (payment) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                        Card(
                            modifier = Modifier.fillMaxWidth().animateItem(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        ) {
                            Row(Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape,
                                    color = if (payment) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer) {
                                    Icon(if (payment) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        null, Modifier.padding(10.dp).size(20.dp), tint = accent)
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(if (payment) "PAYMENT" else "DEBT", style = MaterialTheme.typography.labelMedium, color = accent)
                                    Text(transaction.details, style = MaterialTheme.typography.titleMedium)
                                    Text("${if (payment) "−" else "+"}${money(transaction.credit.toLong())}",
                                        style = MaterialTheme.typography.titleLarge, color = accent)
                                }
                                IconButton(
                                    enabled = !state.isSaving,
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                    onClick = { deleting = transaction },
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete ${transaction.details}",
                                        modifier = Modifier.size(20.dp))
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
            icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null) },
            title = { Text("Add an entry") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()).animateContentSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    Text(if (state.tabs == DetailsTabs.Debt) "Adds to what this customer owes you."
                    else "Reduces the balance; overpayments become customer credit.")

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.nameTextField,
                        onValueChange = { event(DetailsEvents.NameTextFieldChange(it)) },
                        label = { Text("Description") }, singleLine = true, enabled = !state.isSaving,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
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
                Button(enabled = !state.isSaving, onClick = {
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
