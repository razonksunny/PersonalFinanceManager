# Personal Finance Manager

A small desktop application to keep track of the money you earn and spend.
It is made for a college Object-Oriented Programming project: the code is
written to be simple, short and easy to explain.

![Java](https://img.shields.io/badge/Java-21-orange) ![JavaFX](https://img.shields.io/badge/JavaFX-21-blue) ![SQLite](https://img.shields.io/badge/SQLite-3-lightgrey) ![Maven](https://img.shields.io/badge/Maven-3.9-blue)

---

## What it does

* **Register and log in.** Every user gets their own account. The password is
  never stored as plain text.
* **Dashboard.** Shows the total income, the total expenses, the current
  balance, the five newest transactions and the budgets that need attention.
* **Transactions.** Add, change and delete income and expenses. Search by any
  detail (description, category, type, date or amount) while you type, and
  filter by type and category.
* **Budgets.** Set a monthly limit for a category and get a warning when the
  spending reaches the limit.
* **Reports.** Pick a month and see the totals, the expenses split up by
  category and a simple bar chart.

---

## Technologies

| Part | What is used |
|---|---|
| Language | Java 21 |
| Screen | JavaFX 21.0.2 |
| Database | SQLite (file: `finance.db`) |
| Database access | JDBC (`org.xerial:sqlite-jdbc`) |
| Build | Maven 3.9 |

---

## Installing

You need two things on your computer:

1. **JDK 21** – check with `java -version` (it must say 21).
2. **Maven** – check with `mvn -version`.

Then open a terminal in the project folder and run:

```bash
mvn clean javafx:run
```

The first time, Maven downloads the libraries (JavaFX and the SQLite driver).
After that it starts straight away.

The window that opens is the login screen. If you do not have an account yet,
type a name and a password and press **Register**. The application then takes
you to the dashboard.

> The `javafx:run` command also compiles the project, so you do not need to
> run `mvn compile` first.

---

## The database file

The file `finance.db` is made in the project folder the first time the
application starts. The three tables (`users`, `transactions`, `budgets`) are
created automatically at that moment.

The data stays in the file, so it is still there the next time you start the
application. To start again with an empty database, delete `finance.db` while
the application is closed.

---

## The classes and where the OOP ideas are

### The model (`model` package)

| Class | What it does |
|---|---|
| `Transaction` | The abstract parent of all money movements. Holds the amount, the category, the description and the date. |
| `Income` | Money that comes in. Extends `Transaction`. |
| `Expense` | Money that goes out. Extends `Transaction`. |
| `Budget` | The spending limit for one category in one month. |
| `User` | A person with an account. Owns their transactions and budgets. |
| `Category` | The fixed lists of categories that can be picked. |
| `Report` | The totals of one month and the expenses per category. |

### The other packages

| Class | What it does |
|---|---|
| `Main` | Starts the application, opens the database, shows the login screen. |
| `DatabaseManager` | Creates the tables and does all the SQL. |
| `FinanceService` | Does the work: log in, add transactions, totals, budgets, reports. |
| `LoginView`, `MainWindow`, `DashboardView`, `TransactionsView`, `BudgetsView`, `ReportsView`, `UiStyle` | The screens and the shared look of the application. |

### The five OOP concepts

| Concept | Where to find it |
|---|---|
| **Abstraction** | `Transaction` is an abstract class (`Transaction.java:5`). It says what a transaction is, but you cannot make one directly – you must make an `Income` or an `Expense`. |
| **Inheritance** | `Income extends Transaction` (`Income.java:5`) and `Expense extends Transaction` (`Expense.java:5`). Both get the amount, the category, the description and the date from the parent. |
| **Polymorphism** | `Transaction` has the abstract method `getType()` (`Transaction.java:27`). `Income` answers `"Income"` (`Income.java:17`) and `Expense` answers `"Expense"` (`Expense.java:17`). Because the program always calls `transaction.getType()`, the right answer comes back for every kind of transaction. |
| **Encapsulation** | Every field in every class is `private` and is reached with a getter and a setter. For example `User.java:14` holds the list of transactions, and `User.getTransactions()` gives it out. |
| **Composition** | A `User` **has** transactions and budgets: `User.java:14` and `User.java:15` keep the lists, and `User.addTransaction()` (`User.java:44`) puts a new one in. The transactions belong to the user. |

---

## Three things worth knowing

**Money is counted in whole cents.** A computer cannot store `0.10` exactly, so
`0.10 + 0.10 + 0.10` can come out as `0.30000000000000004`. To stop that, the
amount is kept as a `long` number of cents: `12.34` is stored as `1234`.
Every total, every comparison and the budget warning are then exact.

**The password is hashed.** `User.hashPassword()` uses SHA-256 from Java
itself (no extra library) and the database only keeps the hash. A real
application would use a stronger password library, but for this local
demonstration this is enough.

**The look lives in one file.** Every colour, font, table row and button
state is in `src/main/resources/styles.css`. The Java code only gives a
control a style class, for example
`button.getStyleClass().add("primary-button")`, and the file is loaded once
per window by `UiStyle.apply(scene)`.

---

## The category names

* Expenses: Food, Transport, Education, Shopping, Health, Other
* Income: Salary, Bonus, Gift, Other

---

## Project layout

```
pom.xml
README.md
src/main/java/com/financemanager/
â”œâ”€â”€ Main.java
â”œâ”€â”€ model/     Transaction, Income, Expense, Budget, User, Category, Report
â”œâ”€â”€ db/        DatabaseManager
â”œâ”€â”€ service/   FinanceService
â””â”€â”€ ui/        LoginView, MainWindow, DashboardView, TransactionsView,
              BudgetsView, ReportsView, UiStyle
src/main/resources/
â””â”€â”€ styles.css The colours, fonts and button states of the whole application
```
