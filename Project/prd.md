# PRD — ExpenseManager Implementation

## 1. Overview

`ExpenseManager` is the controller/business-logic class for the Personal
Expense Tracker. It owns the in-memory list of `Expense` objects and exposes
operations the GUI (`ExpenseTrackerGUI`) calls to add, edit, delete, query,
filter, and summarize expenses.

This PRD locks the contract for `ExpenseManager` so it can be implemented
against the class diagram (`UML Diagrams/ClassDiagram_Arnav.pdf`) and the
five sequence diagrams (`Note Apr 24, 2026.pdf`).

## 2. Source of Truth

- **Class diagram**: `UML Diagrams/ClassDiagram_Arnav.pdf`
- **Sequence diagrams**: `UML Diagrams/SequenceDiagram.pdf` (5 flows: Add,
  View/Filter, Edit, Delete, Monthly Summary)
- **Use cases**: `UML Diagrams/ExpenseTracker_UseCase.pdf` (UC-01..UC-08)
- **Existing code**: `Project/Expense.java`, `Project/Category.java`

The class diagram is the contract. Method signatures below must match it
exactly. Behavior is informed by the sequence diagrams and report.

## 3. Class Contract (from class diagram)

```
ExpenseManager
- expenses : List<Expense>
+ ExpenseManager()
+ addExpense(expense: Expense) : void
+ updateExpense(index: int, expense: Expense) : void
+ deleteExpense(index: int) : void
+ getAllExpenses() : List<Expense>
+ getExpense(index: int) : Expense
+ filterByCategory(category: Category) : List<Expense>
+ filterByDate(date: LocalDate) : List<Expense>
+ getMonthlySummary(month: int, year: int) : Map<Category, Double>
+ getTotalSpent() : double
```

Relationship: `ExpenseManager` *aggregates* `0..*` `Expense` (per diagram).

## 4. Method-by-Method Design

### 4.1 `ExpenseManager()`
- Initialize `expenses` to `new ArrayList<>()`.

### 4.2 `addExpense(Expense expense)`
- Append `expense` to `expenses`.
- Sequence diagram (Add Expense): UI calls `addExpense(details)` after
  `validate(category)` returns true; `ExpenseManager` then constructs/stores
  the new `Expense`. In our class contract, the GUI builds the `Expense`
  object and passes it in — `ExpenseManager` only stores.
- Validation: per project report ("no negative amounts"). Reject `null` and
  reject expenses whose `amount` is negative. On invalid input throw
  `IllegalArgumentException` so the GUI can surface a message.

### 4.3 `updateExpense(int index, Expense expense)`
- Replace `expenses.get(index)` with `expense`.
- Throw `IndexOutOfBoundsException` (the natural one from `List.set`) when
  `index` is out of range.
- Same null/negative-amount validation as `addExpense`.

### 4.4 `deleteExpense(int index)`
- Remove `expenses.get(index)`.
- Throws `IndexOutOfBoundsException` when out of range.

### 4.5 `getAllExpenses()`
- Return an *unmodifiable* view (`Collections.unmodifiableList(expenses)`)
  so callers cannot mutate internal state without going through the manager.

### 4.6 `getExpense(int index)`
- Return `expenses.get(index)`. Index errors propagate.

### 4.7 `filterByCategory(Category category)`
- Return a new `List<Expense>` containing every expense whose category's
  *name* equals `category.getName()`.
- Compare by name (not by reference) because the GUI's `JComboBox<Category>`
  may hand us a different `Category` instance than the one originally stored.
- Returns empty list (never null) when nothing matches.

### 4.8 `filterByDate(LocalDate date)`
- Return a new `List<Expense>` of expenses whose `getDate().equals(date)`.
- Returns empty list when nothing matches.

### 4.9 `getMonthlySummary(int month, int year)`
- Return `Map<Category, Double>` of total spend by category for the given
  month/year (matches the "totals by category" message in sequence diagram 5).
- Group by `Category` *name* (same reasoning as `filterByCategory`) but key
  the result map by the `Category` object (one canonical instance per name,
  taken from the first matching expense).
- Categories with no expenses in that month are *not* included.
- Use `LinkedHashMap` to preserve insertion order so the GUI displays a
  stable list.

### 4.10 `getTotalSpent()`
- Return the sum of `getAmount()` over all stored expenses. `0.0` when empty.

## 5. Validation Rules

| Rule                              | Where enforced               | Behavior                       |
|-----------------------------------|------------------------------|--------------------------------|
| `expense != null`                 | `addExpense`, `updateExpense`| `IllegalArgumentException`     |
| `expense.amount >= 0`             | `addExpense`, `updateExpense`| `IllegalArgumentException`     |
| Index in range                    | get/update/delete            | `IndexOutOfBoundsException`    |

Category validation (existence in a known list) is the GUI's responsibility
per the sequence diagrams (`validate(category)` happens before
`addExpense`).

## 6. Out of Scope

- Persistence (project report explicitly: in-memory only).
- GUI work (`ExpenseTrackerGUI`) — separate implementation task.
- Concurrency — single-threaded Swing app.

## 7. Implementation Plan

1. Create `Project/ExpenseManager.java` on branch `feature/ExpenseManager`.
2. Match class-diagram signatures exactly.
3. Implement the 9 methods per Section 4.
4. Compile against the existing `Expense.java` / `Category.java` to confirm
   no signature drift.
5. Open a PR into `main` (parallel to the existing
   `feature/ExpenseAndCategory` PR pattern).

## 8. Acceptance Criteria

- `javac Project/*.java` succeeds with no warnings on this PR's branch.
- All 9 public methods exist with the exact signatures from the class
  diagram.
- A simple manual smoke run (add a few expenses, filter, summary) returns
  the values expected by the sequence diagrams.

---

# Phase 2 — Persistence (`feature/Persistence`)

## 9. Motivation

The project report notes data is in-memory only and explicitly flags
"data saving features so that expenses are stored even after the program
is closed" as a future improvement. Phase 2 adds that.

We deliberately keep `ExpenseManager` storage-agnostic — persistence is a
separate class so the class diagram's `ExpenseManager` contract does not
change. The GUI loads expenses on startup and saves on shutdown.

## 10. Scope

In scope:
- A new `ExpenseStorage` utility class with static `save` and `load`.
- CSV file format on local disk.
- Round-trip preservation of every `Expense` field plus its `Category`
  (name + RGB color).
- Graceful handling of a missing file on first run.

Out of scope:
- Multi-user / concurrent writes.
- Encryption or cloud sync.
- Schema migration (file format is v1; if it changes, old files break —
  acceptable for a student project).

## 11. File Format

Single CSV file (default name `expenses.csv`), one expense per line.

Header:
```
date,amount,description,categoryName,r,g,b
```

- `date`: ISO-8601 (`LocalDate.toString()`, e.g. `2026-04-30`).
- `amount`: plain decimal (no currency symbol).
- `description`: free text, RFC-4180 escaped — wrap in double quotes if
  it contains `,`, `"`, `\r`, or `\n`; double up internal quotes.
- `categoryName`: same escaping as description.
- `r`, `g`, `b`: integer 0–255 from `Category.getColor()`.

Categories are *not* stored separately; each row carries enough info to
reconstruct its `Category`. On load, `ExpenseStorage` deduplicates by
category *name* so all expenses tagged "Food" share one canonical
`Category` instance — required for `filterByCategory` and the GUI's
`JComboBox` to behave consistently.

## 12. `ExpenseStorage` API

```
ExpenseStorage  (utility class — package-private fields, public statics)
+ save(expenses: List<Expense>, file: Path) : void   throws IOException
+ load(file: Path) : List<Expense>                   throws IOException
+ loadOrEmpty(file: Path) : List<Expense>            (no checked throw)
```

- `save` overwrites the file atomically (write to `<file>.tmp`, then
  `Files.move(..., REPLACE_EXISTING, ATOMIC_MOVE)`) so a crash mid-save
  cannot corrupt the existing file.
- `load` throws `IOException` on a malformed row so the GUI can show
  an error instead of silently dropping data.
- `loadOrEmpty` returns `new ArrayList<>()` if the file does not exist;
  rethrows as unchecked on I/O errors. Intended for first-run startup.

## 13. GUI Integration (next phase, not this branch)

Once `ExpenseTrackerGUI` lands, wire it up like:

```java
Path file = Path.of("expenses.csv");
ExpenseManager manager = new ExpenseManager();
for (Expense e : ExpenseStorage.loadOrEmpty(file)) manager.addExpense(e);
// ... user interactions ...
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    try { ExpenseStorage.save(manager.getAllExpenses(), file); }
    catch (IOException ignore) {}
}));
```

## 14. Acceptance Criteria (Phase 2)

- `javac Project/*.java` clean.
- Round-trip: `save` then `load` returns expenses equal to the original
  on every field (date, amount, description, category name, category
  color RGB).
- Descriptions containing `,`, `"`, and embedded newlines round-trip
  intact.
- Two expenses with the same category name map to the *same*
  `Category` instance after load (`==`, not just `.equals`).
- `loadOrEmpty` on a non-existent path returns an empty list, not
  throws.
