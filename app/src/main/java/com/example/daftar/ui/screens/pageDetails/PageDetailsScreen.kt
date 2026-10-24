package com.example.daftar.ui.screens.pageDetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.daftar.backend.localDb.Transaction

@Composable
fun PageDetailsScreen(
    viewModel: PageDetailsViewModel,
    pageName: String,
    pageNumber: String,

) {
    val transaction by viewModel.transactions.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsState()
    DetailsPageContainer(
        transactions = transaction,
        pageName = pageName,
        pageNumber = pageNumber,
        uiState = uiState,
        event = { viewModel.eventHandler(it) }
    )
}


@Composable
fun DetailsPageContainer(
    modifier: Modifier = Modifier,
    transactions: List<Transaction>,
    pageName: String,
    pageNumber: String,
    uiState: DetailsPageUiState,
    event: (DetailsEvents) -> Unit
    ) {
    Scaffold(
        topBar = { DetailsTopBar(Modifier) },
        floatingActionButton = {
            FloatingActionButton(onClick = { event(DetailsEvents.OnAddButtonPress)} ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) {
        DetailsPageContent(transactions, modifier.padding(it), pageName, pageNumber)

        AnimatedVisibility(uiState.isDialogOpen) {
            Dialog(onDismissRequest = {event(DetailsEvents.DissmissDialog)}) {
                AddTransactionDilaogContent(
                    Modifier,
                    event,
                    uiState.nameTextField,
                    uiState.priceField,
                    tab = uiState.tabs
                )
            }
        }
    }
}

@Composable
fun DetailsPageContent(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    name: String,
    number: String,
) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(name, Modifier.padding(end = 30.dp, top = 12.dp), fontSize = 60.sp, color = Color.LightGray)
        Text(number, Modifier.padding(start = 40.dp, top = 0.dp, end = 0.dp, bottom = 10.dp), color = Color.Gray)

        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))


        LazyColumn() {
            items(transactions){
                TransactionBuble(
                    credit = it.credit,
                    name = it.details
                )
            }
        }



    }
}

@Composable
fun DetailsTopBar(modifier: Modifier = Modifier) {
    Surface(
        modifier
            .height(30.dp)
            .fillMaxWidth(),
        shadowElevation = 10.dp
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Details page")
        }

    }
}

@Composable
fun TransactionBuble(
    modifier: Modifier = Modifier,
    credit: Int,
    name: String,
) {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(name, color = Color.Gray)

        Box(contentAlignment = Alignment.Center, modifier = modifier
            .wrapContentSize()
            .padding(bottom = 20.dp)) {

            Surface(shape = CircleShape, tonalElevation = 10.dp, modifier = Modifier.padding(8.dp)) {
                Text("$credit", fontSize = 40.sp, modifier = Modifier.padding(4.dp))
            }

            Surface(
                shape = CircleShape,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    }

}


@Composable
fun AddTransactionDilaogContent(
    modifier: Modifier = Modifier,
    event: (DetailsEvents) -> Unit,
    name: String,
    price: String,
    tab: DetailsTabs
    ) {
    Surface() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            PrimaryTabRow(
                selectedTabIndex = tab.ordinal,
            ){
                Tab(
                    selected = tab == DetailsTabs.Negitave,
                    onClick = {},
                    modifier = Modifier.padding(vertical = 12.dp)
                ){Icon(Icons.Default.MoneyOff, null)}

                Tab(
                    selected = tab == DetailsTabs.Credit,
                    onClick = {}
                ){ Icon(Icons.Default.Add, null) }
            }

            TextField(
                value = name,
                onValueChange = {event(DetailsEvents.NameTextFieldChange(it))},
                label = {Text("Item Name")},
                modifier = Modifier.padding(12.dp)
            )

            TextField(
                value = price,
                onValueChange = {event(DetailsEvents.PriceTextFieldChange(it))},
                label = {Text("Item Price")},
                modifier = Modifier.padding(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            TextButton(onClick = { event (DetailsEvents.AddItem(name, price)) }) {
                Text("Add")
            }

        }
    }


}

@Preview
@Composable
private fun Contentprev() {
    val t = listOf(
        Transaction(0,"sugar",5,false, 1),
        Transaction(0,"sugar",5,false, 1),
        Transaction(0,"sugar",5,false, 1),
        Transaction(0,"sugar",5,false, 1),
        Transaction(0,"sugar",5,false, 1),
        Transaction(0,"sugar",5,false, 1),
        Transaction(0,"sugar",5,false, 1),

    )
//    DetailsPageContainer(pageNumber = "0599416872", pageName = "ahmad", transactions = t)
}

@Preview(showBackground = true)
@Composable
private fun BublePrev() {
//    TransactionBuble()
}

@Preview
@Composable
private fun DialogPrev() {
    AddTransactionDilaogContent(
        event = {},
        name = "",
        price = "",
        tab = DetailsTabs.Negitave
    )
}


data class DetailsPageUiState(
    val isDialogOpen: Boolean = false,
    val nameTextField: String = "",
    val priceField: String = "",
    val tabs: DetailsTabs = DetailsTabs.Negitave,
)

enum class DetailsTabs{
    Negitave,
    Credit,

}


sealed class DetailsEvents{
    object OnAddButtonPress: DetailsEvents()
    object DissmissDialog: DetailsEvents()
    data class PriceTextFieldChange(val value: String): DetailsEvents()
    data class NameTextFieldChange(val value: String): DetailsEvents()
    data class UpdateTab(val tab: DetailsTabs): DetailsEvents()
    data class AddItem(val itemName: String, val itemPrice: String): DetailsEvents()


}