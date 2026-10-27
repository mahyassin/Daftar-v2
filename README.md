# Daftar

A local Android customer ledger built with Kotlin, Jetpack Compose, Material 3,
Navigation 3, ViewModels/StateFlow, and Room. Requires Android 7.0 (API 24) or newer.

## Using the ledger

- Add a customer page with a name and optional phone number.
- Open the page and add a **Debt** or **Payment**. Debts increase what the customer
  owes you; payments reduce it. Overpayments remain as customer credit.
- Amounts are positive whole shekels (₪), up to 2,147,483,647 per entry. Account
  totals use 64-bit integers and may exceed that amount.
- **Record full payment** settles an outstanding balance while preserving history.
- Delete an incorrect entry to recalculate the balance. Page deletion removes all
  its entries. Both actions require confirmation.
- **Overview** shows total receivables, customer credit, and the net balance.

All data stays in the local `transactions.db` database. No account or server is
required. Android's configured system backup rules still apply.

## Build and test

Install JDK 25 and the Android SDK (platform 37 and build tools 36.0.0), then set
`ANDROID_HOME` or `sdk.dir` in an untracked `local.properties` file.

```sh
bash gradlew testDebugUnitTest assembleDebug lintDebug
bash gradlew connectedDebugAndroidTest  # requires a connected device/emulator
```

Unit tests cover validation, signed balances, overview math, ViewModel save/error
states, and migrations using real SQLite. Device database tests cover Room query
invalidation, cascading deletion and atomic settlement.

## Existing databases

Schema version 4 uses a foreign key and index for transactions. Explicit migrations
support versions 1, 2 and 3; upgrades never use destructive fallback. Version 1
records are grouped into pages by name and number. Orphaned transactions from
later versions are preserved in `Recovered page <id>` pages. Version 3 is treated
as the version-2 column layout, matching the original version-3 entity definitions.

Balances derive from transactions. The old `Pages.credit` column is retained for
compatibility but is not used to display balances. The existing `isPayed` field is
used as the entry type: `false` for debts, `true` for payments. Future schema changes
must add explicit migrations and preserve the exported Room schemas.
