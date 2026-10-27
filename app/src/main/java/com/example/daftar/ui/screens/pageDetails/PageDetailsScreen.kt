package com.example.daftar.ui.screens.pageDetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.daftar.backend.localDb.Transaction
import kotlin.math.abs

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
        DetailsPageContent(
            transactions,
            modifier.padding(it),
            pageName,
            pageNumber,
            events = event
        )

        AnimatedVisibility(uiState.isDialogOpen) {
            Dialog(onDismissRequest = {event(DetailsEvents.DissmissDialog)}) {
                AddTransactionDilaogContent(
                    Modifier,
                    event,
                    uiState.nameTextField,
                    uiState.priceField,
                    tab = uiState.tabs,
                    isEdit = false,
                    transactionId = 0,
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
    events: (DetailsEvents) -> Unit
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


        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(transactions){
                TransactionBuble(
                    credit = it.credit,
                    name = it.details,
                    id = it.id,
                    event = events
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
    id: Int,
    event: (DetailsEvents) -> Unit
) {

    val alignment = if(credit > 0) Alignment.BottomEnd else Alignment.BottomStart
    val icon = if(credit > 0) Icons.Default.Add else Icons.Default.HorizontalRule
    var isPressed by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(name, color = Color.Gray, modifier = Modifier.padding(top
        = 40.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(isPressed) {
                Icon(Icons.Default.Edit, null)
            }   

            Box(contentAlignment = Alignment.Center, modifier = modifier
                .wrapContentSize()
                .padding()
            ) {

                Surface(
                    shape = CircleShape,
                    tonalElevation = 10.dp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable(
                            onClick = { isPressed = !isPressed }
                        )

                ) {
                    Text("${abs(credit)}", fontSize = 40.sp, modifier = Modifier.padding(4.dp))
                }

                Surface(
                    shape = CircleShape,
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .align(alignment)

                ) {
                    Icon(icon, null)
                }


            }

            AnimatedVisibility(isPressed) {
                IconButton (
                    onClick = {
                        event(DetailsEvents.DeleteTransaction(id))
                        isPressed = false
                    },
                ){
                    Icon(Icons.Default.Close, null)
                }

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
    tab: DetailsTabs,
    isEdit: Boolean,
    transactionId: Int,
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

            TextButton(
                onClick = {
                    if(isEdit) event(DetailsEvents.UpdateTransaction(transactionId))
                    else {
                        event(DetailsEvents.AddItem(name, price))
                        event(DetailsEvents.DissmissDialog)
                    }
                }
            ){
                if(isEdit) Text("Done")
                else Text("Add")
            }

        }
    }


}

@Preview
@Composable
private fun Contentprev() {
    val t = listOf(
        Transaction(0,"sugar",15,false, 1),
        Transaction(1,"sugar",-5,false, 1),
        Transaction(2,"sugar",34,false, 1),
        Transaction(3,"sugar",-2,false, 1),
        Transaction(4,"sugar",35,false, 1),
        Transaction(5,"sugar",5,false, 1),
        Transaction(7,"sugar",5,false, 1),

    )
    DetailsPageContainer(
        pageNumber = "0599416872",
        pageName = "ahmad",
        transactions = t,
        uiState = DetailsPageUiState(),
        event = {  }
    )
}


@Preview
@Composable
private fun DialogPrev() {
//    AddTransactionDilaogContent(
//        event = {},
//        name = "",
//        price = "",
//        tab = DetailsTabs.Negitave,
//        modifier = TODO(),
//        isEdit = TODO(),
//        transactionId = TODO()
//    )
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
    data class DeleteTransaction(val id: Int): DetailsEvents()
    data class UpdateTransaction(val id: Int): DetailsEvents()


}