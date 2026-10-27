package com.example.daftar.backend

import org.junit.Assert.*
import org.junit.Test

class LedgerTest {
    @Test fun amountsMustBePositiveWholeShekels() {
        listOf("", " ", "0", "-1", "1.5", "abc", "2147483648", "١٢").forEach {
            assertNull("Accepted invalid amount: $it", parseAmount(it))
        }
        assertEquals(12, parseAmount(" 12 "))
        assertEquals(Int.MAX_VALUE, parseAmount("2147483647"))
    }

    @Test fun paymentsReduceDebtAndAllowCustomerCredit() {
        assertEquals(70L, signedAmount(100, false) + signedAmount(30, true))
        assertEquals(-50L, signedAmount(100, false) + signedAmount(150, true))
    }

    @Test fun totalsDoNotOverflowAtIntegerLimit() {
        assertEquals(4294967294L, signedAmount(Int.MAX_VALUE, false) * 2)
    }

    @Test fun overviewSeparatesReceivablesFromCustomerCredit() {
        val totals = ledgerTotals(listOf(100L, -40L, 20L, 0L))
        assertEquals(120L, totals.owed)
        assertEquals(40L, totals.customerCredit)
        assertEquals(80L, totals.net)
    }
}
