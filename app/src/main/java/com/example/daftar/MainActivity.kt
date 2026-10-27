package com.example.daftar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.daftar.ui.screens.home.HomeScreen
import com.example.daftar.ui.screens.home.HomeViewModel
import com.example.daftar.ui.theme.DaftarTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.daftar.ui.screens.pageDetails.PageDetailsScreen
import com.example.daftar.ui.screens.pageDetails.PageDetailsViewModel
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as App).container
        enableEdgeToEdge()
        setContent {
            DaftarTheme {
                AppNavigation(modifier = Modifier, container)
            }
        }
    }
}


@Composable
fun AppNavigation(modifier: Modifier = Modifier, container: AppContainer) {

    val backStack = rememberNavBackStack(Home)

    NavDisplay(
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Home> {
                val homeViewModel: HomeViewModel = viewModel(factory = container.factory)
                HomeScreen(
                    viewModel = homeViewModel,
                    onPageClick = { id, name , number ->
                        backStack.add(PageDetails(id, name, number))
                    }
                )
            }
            entry<PageDetails> {
                val pageDetailsViewModel: PageDetailsViewModel = viewModel(key = "page-${it.pageId}", factory = container.pageDetailFactory(it.pageId))
                PageDetailsScreen(
                    viewModel = pageDetailsViewModel,
                    pageName = it.pageName,
                    pageNumber = it.pageNumber,
                    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                )

            }
        },
        backStack = backStack,
        onBack = { if(backStack.size > 1) { backStack.removeAt(backStack.lastIndex) } },
    )
}

@Serializable
data object Home: NavKey
@Serializable
data class PageDetails(val pageId: Int, val pageName: String, val pageNumber: String): NavKey
