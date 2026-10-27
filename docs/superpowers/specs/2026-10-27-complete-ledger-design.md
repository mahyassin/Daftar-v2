# Complete the local ledger

Finish the existing Android ledger using Kotlin, Compose, Navigation 3, Room,
ViewModels and StateFlow. Work locally on `feat/complete-ledger`.

## Behavior

- A debt increases the customer's balance; a payment decreases it. Negative
  balances represent customer credit (confirmed by the user).
  Existing entries with `isPayed = false` remain debts; `true` represents payments.
- Keep amounts in whole shekels, as in the existing app. Accept only positive
  amounts up to 2,147,483,647; use Long for sums. Validate names and amounts with
  visible errors, and reset/close a form only after a successful save.
- The home tabs show customer pages and a balance overview. All balances derive
  from transactions, so insertions/deletions update both screens immediately.
- Details show debts/payments distinctly, a running account balance, working
  entry-type tabs, transaction deletion, and an action to settle an outstanding
  balance by inserting a payment. Settlement is atomic and cannot duplicate a
  payment on repeated taps.
- Confirm deletions. Deleting a page cascades to its transactions. Recover orphan
  transactions into named recovered pages on upgrade rather than discarding them.
- Replace destructive fallback with explicit migrations for schemas 1 through 4.
  Version 4 adds the page foreign key/index. Schema 3 has the same entity shape as
  schema 2 in the checked-in source. Preserve legacy page fields for compatibility;
  the old stored credit field is no longer used for balance display.
- No network backend, login, decimal currency, or UI redesign is needed.

## Approach

Use SQL aggregates and foreign keys rather than updating cached balances in
ViewModels: this keeps inserts, deletes, and migrations consistent. An alternative
of manually maintaining Pages.credit introduces multiple sources of truth.
Retain the existing `isPayed` column to avoid rewriting otherwise valid ledger
records; label it clearly as debt/payment at the UI boundary.

## Validation

Add JVM tests for amount validation, signed balances, summary math and ViewModel
state transitions. Add Android database integration tests for aggregates, cascade,
atomic settlement, and upgrades. Run the available build/test tools and report any
environment limitation explicitly. Do not claim device checks without an emulator.
