package com.example.daftar.backend

fun parseAmount(value: String): Int? {
    val text = value.trim()
    if (text.isEmpty() || text.any { it !in '0'..'9' }) return null
    return text.toIntOrNull()?.takeIf { it > 0 }
}

fun signedAmount(amount: Int, isPayment: Boolean): Long =
    if (isPayment) -amount.toLong() else amount.toLong()

data class LedgerTotals(val owed: Long, val customerCredit: Long) {
    val net: Long get() = owed - customerCredit
}

fun ledgerTotals(balances: List<Long>) = LedgerTotals(
    owed = balances.filter { it > 0 }.sum(),
    customerCredit = -balances.filter { it < 0 }.sum(),
)
