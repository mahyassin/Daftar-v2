package com.example.daftar.backend

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.daftar.backend.localDb.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalDBTest {
    private lateinit var db: LocalDB
    @Before fun open() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(), LocalDB::class.java
        ).build()
    }
    @After fun close() = db.close()

    @Test fun balancesReactToInsertionPaymentAndDeletion() = runBlocking {
        val id = db.getPageDao().insert(Pages(name = "Ahmad", number = "", credit = 0)).toInt()
        val debt = Transaction(details = "Groceries", credit = 100, isPayed = false, pageId = id)
        val debtId = db.getTransactionsDao().insert(debt).toInt()
        db.getTransactionsDao().insert(Transaction(details = "Payment", credit = 30, isPayed = true, pageId = id))
        assertEquals(70L, db.getPageDao().observePages().first().single().credit)
        db.getTransactionsDao().delete(debt.copy(id = debtId))
        assertEquals(-30L, db.getPageDao().observePages().first().single().credit)
    }

    @Test fun deletingPageDeletesItsTransactions() = runBlocking {
        val id = db.getPageDao().insert(Pages(name = "Ahmad", number = "", credit = 0)).toInt()
        db.getTransactionsDao().insert(Transaction(details = "Item", credit = 10, isPayed = false, pageId = id))
        db.getPageDao().deleteById(id)
        assertTrue(db.getTransactionsDao().observeTransaction(id).first().isEmpty())
    }

    @Test fun settlingTwiceOnlyRecordsOnePayment() = runBlocking {
        val id = db.getPageDao().insert(Pages(name = "Ahmad", number = "", credit = 0)).toInt()
        db.getTransactionsDao().insert(Transaction(details = "Item", credit = 100, isPayed = false, pageId = id))
        db.getTransactionsDao().settlePage(id)
        db.getTransactionsDao().settlePage(id)
        assertEquals(0L, db.getPageDao().observePages().first().single().credit)
        assertEquals(2, db.getTransactionsDao().observeTransaction(id).first().size)
    }

    @Test fun versionThreeUpgradePassesRoomValidationAndPreservesOrphans() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "daftar-migration-test.db"
        context.deleteDatabase(name)
        try {
            context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
                old.execSQL("CREATE TABLE Pages (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, number TEXT NOT NULL, credit INTEGER NOT NULL)")
                old.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, details TEXT NOT NULL, credit INTEGER NOT NULL, isPayed INTEGER NOT NULL, pageId INTEGER NOT NULL)")
                old.execSQL("INSERT INTO transactions VALUES (1, 'Kept entry', 25, 0, 42)")
                old.version = 3
            }
            Room.databaseBuilder(context, LocalDB::class.java, name)
                .addMigrations(*DatabaseMigrations.ALL).build().use { upgraded ->
                    val page = upgraded.getPageDao().observePages().first().single()
                    assertEquals(42, page.id)
                    assertEquals("Recovered page 42", page.name)
                    assertEquals(25L, page.credit)
                    assertEquals(1, upgraded.getTransactionsDao().observeTransaction(42).first().size)
                }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
