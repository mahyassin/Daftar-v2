# Complete Ledger Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task by task.

**Goal:** Make the existing local customer ledger usable end to end.

**Architecture:** Transaction-derived balances, Room foreign keys and migrations,
ViewModel-owned validated forms, Compose pages/details/summary.

**Tech Stack:** Kotlin, Compose Material 3, Room, Navigation 3, JUnit.

**Spec:** `docs/superpowers/specs/2026-10-27-complete-ledger-design.md`

## Global Constraints

- Whole shekels; individual amounts 1..2,147,483,647, totals use Long.
- Preserve existing data through versions 1, 2 and 3; never destructive fallback.
- Work locally on `feat/complete-ledger`, no publish/merge required.

## Review Focus

- Empty, nonnumeric, negative, zero, oversized inputs must not crash or write.
- Deletion and payments must immediately change account and overview balances.
- Orphaned historical transactions must survive upgrades.
- Rapid repeated saves/settlements must not create duplicate records.
- Failed writes must leave the form/data intact and expose a useful error.

## Task 1: Ledger storage and arithmetic

Files: `backend/localDb/{TransactionEntity,TransactionDao,TransactionRepo,LocalDB}.kt`,
new `backend/localDb/DatabaseMigrations.kt`, new `backend/Ledger.kt`, `AppContainer.kt`.

Interfaces: `PageBalance.credit: Long`, `PagesRepo.observePages(): Flow<List<PageBalance>>`,
`TransactionRepo.settlePage(pageId: Int)`; transaction `isPayed` means payment.

- [ ] Add tests: 100 debt - 30 payment = 70; 100 - 150 = -50; sums beyond Int;
  invalid amounts; database balance updates, cascade and atomic double settlement;
  migrate legacy/recover orphan records.
- [ ] Run tests/baseline and record limitations.
- [ ] Implement safe parsing, aggregate queries, cascade schema and migrations.
- [ ] Verify the domain tests and database integration tests where supported.

## Task 2: Screen state and functional UI

Files: `ui/screens/home/*.kt`, `ui/screens/pageDetails/*.kt`, `MainActivity.kt`.
Add ViewModel tests for save success/reset, validation, write failures and duplicates.

- [ ] Add state tests using real ViewModels and controlled repository boundaries.
- [ ] Implement guarded saves, settlement/deletion handlers and error states.
- [ ] Wire home/summary and debt/payment tabs, balances, empty states, back action,
  loading indicators, inline errors, and deletion confirmations.
- [ ] Run unit tests and build/lint, fix implementation failures.

## Task 3: Verification and handoff

- [ ] Review diff against the design, addressing important defects.
- [ ] Run complete available verification; record unavailable Android checks.
- [ ] Add README with behavior, build/test commands and migration semantics.
- [ ] Leave code and tests reviewable on the feature branch; report exact results.
