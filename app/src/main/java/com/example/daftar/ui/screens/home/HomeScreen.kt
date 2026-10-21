package com.example.daftar.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.daftar.backend.localDb.Pages


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onPageClick: (Int) -> Unit,

    ) {
    val uiState by viewModel.uiState.collectAsState()
    val pages by viewModel.pages.collectAsState()
    Container(uiState, pages, { viewModel.handleHomeEvent(it) }, onPageClick)
}

@Composable
fun Container(
    uiState: HomeUiState,
    pages: List<Pages>,
    eventHandler: (HomeEvents) -> Unit,
    onPageClick: (Int) -> Unit,
) {
    Scaffold(
        floatingActionButton = { AddNameButton( onclick = { eventHandler(it)}) },
        bottomBar = { BottomBar(isName =  uiState.isName) }
    ) {
        HomeScreenContent(Modifier.padding(it), pages, eventHandler, onPageClick)

        AnimatedVisibility(
            visible =  uiState.isDialogOpen
        ) {
            Dialog(onDismissRequest = {eventHandler(HomeEvents.DissmisDialog)}) {
                AddPageWindowContent(
                    eventHandler = eventHandler,
                    name = uiState.nameTextField,
                    number = uiState.numberTextField
                )
            }
        }

    }

}


@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    transactions: List<Pages>,
    eventHandler: (HomeEvents) -> Unit,
    onPageClick: (Int) -> Unit,
) {
    LazyVerticalGrid(GridCells.Fixed(1), modifier) {
        items(items = transactions, key = { it.id }){
            ContactCard(
                Modifier,
                it.name,
                it.number,
                it.credit,
                it.id,
                eventHandler,
                onPageClick = onPageClick
            )
        }
    }
}

@Composable
fun AddNameButton(modifier: Modifier = Modifier , onclick: (HomeEvents) -> Unit) {
    FloatingActionButton(
        onClick = { onclick(HomeEvents.OnActionButtonPressed) },
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
    }
}

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    isName: Boolean,
) {
    NavigationBar() {
        NavigationBarItem(
            onClick = {},
            icon = { Icon(Icons.Default.Contacts, null) },
            selected = isName,
        )

        NavigationBarItem(
            onClick = {},
            icon = { Icon(Icons.Default.Money, null)},
            selected = !isName,
        )
    }
}

@Composable
fun ContactCard(
    modifier: Modifier = Modifier,
    name: String,
    number: String,
    credit: Int,
    id: Int,
    eventHandler: (HomeEvents) -> Unit,
    onPageClick: (Int) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {



        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)) {

            Surface(modifier.weight(10f).clickable(
                true,
                onClick = { onPageClick(id) },
            ), shadowElevation = 10.dp) {
                Row(
                    horizontalArrangement =  Arrangement.SpaceBetween,
                    verticalAlignment =  Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(name)
                    Text("$credit ₪")
                }
            }


            Surface(modifier
                .width(20.dp)
                .height(20.dp),
                shadowElevation = (1).dp,
            ) {
                IconButton({ eventHandler(HomeEvents.DeletePage(id)) }){
                    Icon(Icons.Default.Clear, null)
                }
            }
        }
        Surface(modifier
            .wrapContentSize(),
            shadowElevation = 10.dp) {
            Row(
                horizontalArrangement =  Arrangement.Center,
                verticalAlignment =  Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp).wrapContentHeight()
            ) {
                Text(number)
            }

        }

    }



}

@Composable
fun AddPageWindowContent(
    modifier: Modifier = Modifier,
    eventHandler: (HomeEvents) -> Unit,
    name: String,
    number: String,
) {
    Surface() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val smallPadding = Modifier.padding(8.dp)
            Text("add page", modifier = smallPadding)
            TextField(
                value = name,
                onValueChange = { eventHandler(HomeEvents.OnNameTextFieldChange(it))},
                label = { Text("page name") },
                modifier = smallPadding,
            )

            TextField(
                value = number,
                onValueChange = {eventHandler(HomeEvents.OnNumberTextFieldChange(it))},
                label = { Text("page number") },
                modifier = smallPadding,
            )

            Button(onClick = {
                eventHandler(HomeEvents.AddPageEvent(name, number))
                eventHandler(HomeEvents.DissmisDialog)
            }) {
                Text("Add Page")
            }
        }
    }
}

@Preview
@Composable
private fun prev() {
    val uiState = HomeUiState( isName =  true)
    val transactions = listOf(
        Pages(0, "ahmad", "0032424523", 40,),
        Pages(1, "ahmad", "0032424523", 40,),
        Pages(2, "ahmad", "0032424523", 40,),
        Pages(3, "ahmad", "0032424523", 40,),
        Pages(4, "ahmad", "0032424523", 40,),
        Pages(5, "ahmad", "0032424523", 40,),
        Pages(6, "ahmad", "0032424523", 40,),

    )
    fun eventHandler(event : HomeEvents) {

    }
    Container(
        uiState,
        transactions,
        { eventHandler(it) },
        onPageClick = {}
    )
}

@Preview
@Composable
private fun CardPrev() {
    ContactCard(
        modifier = Modifier,
        "ahmad",
        "0599416872",
        59,
        0,
        eventHandler = {},
        onPageClick = {}
    )
}

data class HomeUiState(
    val isName: Boolean = false,
    val isDialogOpen: Boolean = false,
    val nameTextField: String = "",
    val numberTextField: String = "",
)

sealed class HomeEvents{
    object OnActionButtonPressed: HomeEvents()
    object DissmisDialog: HomeEvents()
    data class OnNameTextFieldChange(val name: String): HomeEvents()
    data class OnNumberTextFieldChange(val number: String): HomeEvents()
    data class AddPageEvent(val name: String, val number: String): HomeEvents()
    data class DeletePage(val id: Int): HomeEvents()
    data class NavigateToDetails(val id: Int): HomeEvents()

}


@Preview
@Composable
private fun AddPageAlertprev() {
//    AddPageWindowContent()
}
