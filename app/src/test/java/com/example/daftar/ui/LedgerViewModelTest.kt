package com.example.daftar.ui

import com.example.daftar.backend.localDb.*
import com.example.daftar.ui.screens.home.*
import com.example.daftar.ui.screens.pageDetails.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LedgerViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun mainDispatcher() = Dispatchers.setMain(dispatcher)
    @After fun resetDispatcher() = Dispatchers.resetMain()

    @Test fun blankPageCannotBeSaved() = runTest(dispatcher) {
        val repo = MemoryPages()
        val vm = HomeViewModel(repo)
        vm.handleHomeEvent(HomeEvents.AddPageEvent("  ", ""))
        advanceUntilIdle()
        assertTrue(repo.saved.isEmpty())
        assertNotNull(vm.uiState.value.error)
    }

    @Test fun successfulPageSaveResetsAndClosesDialogAndPreventsDoubleTap() = runTest(dispatcher) {
        val repo = MemoryPages()
        val vm = HomeViewModel(repo)
        vm.handleHomeEvent(HomeEvents.OnActionButtonPressed)
        vm.handleHomeEvent(HomeEvents.AddPageEvent(" Ahmad ", "+970 599"))
        vm.handleHomeEvent(HomeEvents.AddPageEvent(" Ahmad ", "+970 599"))
        advanceUntilIdle()
        assertEquals(1, repo.saved.size)
        assertEquals("Ahmad", repo.saved.single().name)
        assertFalse(vm.uiState.value.isDialogOpen)
        assertEquals("", vm.uiState.value.nameTextField)
    }

    @Test fun invalidAmountsNeverReachStorage() = runTest(dispatcher) {
        val repo = MemoryTransactions()
        val vm = PageDetailsViewModel(repo, 7)
        listOf("", "0", "-1", "1.5", "9999999999999").forEach {
            vm.eventHandler(DetailsEvents.AddItem("Item", it))
            assertNotNull(vm.uiState.value.error)
        }
        advanceUntilIdle()
        assertTrue(repo.saved.isEmpty())
    }

    @Test fun paymentTabSavesPaymentForCorrectPageAndResetsForm() = runTest(dispatcher) {
        val repo = MemoryTransactions()
        val vm = PageDetailsViewModel(repo, 7)
        vm.eventHandler(DetailsEvents.OnAddButtonPress)
        vm.eventHandler(DetailsEvents.UpdateTab(DetailsTabs.Payment))
        vm.eventHandler(DetailsEvents.AddItem(" Cash ", "50"))
        vm.eventHandler(DetailsEvents.AddItem(" Cash ", "50"))
        advanceUntilIdle()
        val entry = repo.saved.single()
        assertEquals(7, entry.pageId)
        assertEquals("Cash", entry.details)
        assertEquals(50, entry.credit)
        assertTrue(entry.isPayed)
        assertFalse(vm.uiState.value.isDialogOpen)
        assertEquals("", vm.uiState.value.priceField)
    }

    @Test fun failedSavePreservesFormAndAllowsRetry() = runTest(dispatcher) {
        val repo = MemoryTransactions().apply { fail = true }
        val vm = PageDetailsViewModel(repo, 7)
        vm.eventHandler(DetailsEvents.OnAddButtonPress)
        vm.eventHandler(DetailsEvents.NameTextFieldChange("Item"))
        vm.eventHandler(DetailsEvents.PriceTextFieldChange("25"))
        vm.eventHandler(DetailsEvents.AddItem("Item", "25"))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isDialogOpen)
        assertEquals("25", vm.uiState.value.priceField)
        assertNotNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.isSaving)
        repo.fail = false
        vm.eventHandler(DetailsEvents.AddItem("Item", "25"))
        advanceUntilIdle()
        assertEquals(1, repo.saved.size)
    }
}

private class MemoryPages : PagesRepository {
    val saved = mutableListOf<Pages>()
    override fun observePages() = flowOf(emptyList<PageBalance>())
    override suspend fun savePage(page: Pages) { saved.add(page) }
    override suspend fun deleteById(id: Int) = Unit
}

private class MemoryTransactions : TransactionsRepository {
    val saved = mutableListOf<Transaction>()
    var fail = false
    override fun observeTransaction(pageId: Int) = flowOf(emptyList<Transaction>())
    override suspend fun insertTransaction(transaction: Transaction) {
        if (fail) error("disk unavailable")
        saved.add(transaction)
    }
    override suspend fun deleteTransaction(transaction: Transaction) { saved.remove(transaction) }
    override suspend fun settlePage(pageId: Int) = Unit
}
