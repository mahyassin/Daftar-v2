package com.example.daftar.ui.screens.pageDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Date

@Composable
fun PageDetailsScreen(
    viewModel: PageDetailsViewModel
) {
    DetailsPageContainer()
}


@Composable
fun DetailsPageContainer(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { DetailsTopBar(Modifier) }
    ) {
        DetailsPageContent(modifier.padding(it))
    }
}

@Composable
fun DetailsPageContent(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Ahmad", Modifier.padding(end = 30.dp, top = 12.dp), fontSize = 60.sp, color = Color.LightGray)
        Text("0544056506", Modifier.padding(start = 40.dp, top = 0.dp, end = 0.dp, bottom = 10.dp), color = Color.Gray)

        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))


        Text("10/ 11/ 2012")

        TransactionBuble()
        TransactionBuble()
        TransactionBuble()
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
fun TransactionBuble(modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.wrapContentSize().padding(20.dp)) {

        Surface(shape = CircleShape, tonalElevation = 10.dp, modifier = Modifier.padding(8.dp)) {
            Text("12", fontSize = 40.sp, modifier = Modifier.padding(4.dp))
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

@Preview
@Composable
private fun Contentprev() {
    DetailsPageContainer()
}

@Preview(showBackground = true)
@Composable
private fun BublePrev() {
    TransactionBuble()
}