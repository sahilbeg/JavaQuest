## Hook

A football coach wants three lists. The top scorer in each team. How many days each player rested between games. And every player who never played in goal. A spreadsheet can do it, but only after an afternoon of copying and sorting.

Jiraiya wants the same kind of lists about Spoke riders. In [SQL basics](/t/module-08-backend/topic-07-sql-basics) you learned to filter, group and join. This ticket gives you the tools that turn an afternoon into one query: anti-joins, window functions and CTEs. Senior engineers use them every day, and interviewers love them.

## Concept

### The order a query really runs in

You write a query starting with `SELECT`. The database does not run it in that order. It runs the clauses in a fixed **logical order**.

```callout jargon
The **logical order** is the order in which a query's clauses take effect: FROM, WHERE, GROUP BY, HAVING, SELECT, ORDER BY, then LIMIT. A real database may take shortcuts, but the answer is always as if it ran in this order.
```

This page uses a small week of Spoke data: four riders (rider-1 Ada, rider-2 Ben, rider-3 Cy and rider-4 Dee) and eight rides. Ride 5 is a **guest ride**, paid at the kiosk without an account, so its `rider_id` is NULL.

```text
id | rider_id | bike_id | start_station_id | started_at          | minutes | fare_cents
---+----------+---------+------------------+---------------------+---------+-----------
1  | rider-1  | B-1042  | harbor           | 2026-11-02 08:00:00 | 10      | 400
2  | rider-2  | B-1088  | harbor           | 2026-11-02 09:00:00 | 20      | 400
3  | rider-1  | B-1088  | riverside        | 2026-11-03 08:15:00 | 12      | 280
4  | rider-3  | B-1017  | harbor           | 2026-11-03 17:40:00 | 8       | 300
5  | null     | B-1023  | uptown           | 2026-11-04 12:00:00 | 15      | 550
6  | rider-2  | B-1088  | harbor           | 2026-11-05 07:50:00 | 20      | 400
7  | rider-1  | B-1042  | harbor           | 2026-11-05 07:50:00 | 6       | 280
8  | rider-3  | B-1088  | harbor           | 2026-11-06 08:05:00 | 30      | 550
```

B-1042 and B-1023 are EBIKEs, B-1088 is a CLASSIC and B-1017 is a CARGO bike. Watch one query pass through each clause.

```animate
title: One query, clause by clause
kind: scene
nodes:
  - { id: from, label: FROM ride, emoji: "🗄️", value: "8 rows" }
  - { id: where, label: "WHERE minutes >= 10", emoji: "🚪", value: "?" }
  - { id: group, label: GROUP BY rider_id, emoji: "🧺", value: "?" }
  - { id: having, label: "HAVING COUNT(*) >= 2", emoji: "⚖️", value: "?" }
  - { id: select, label: "SELECT, ORDER BY", emoji: "📋", value: "?" }
steps:
  - say: FROM runs first. It hands over every ride in the table, all 8 rows.
    focus: [from]
  - say: WHERE checks each row on its own. Rides 4 and 7 are under 10 minutes, so 6 rows go on.
    send: { from: from, to: where, label: "8 rows" }
    set: { where: "6 rows" }
  - say: GROUP BY puts rows with the same rider_id in one pile. The guest's NULL gets a pile too.
    send: { from: where, to: group, label: "6 rows" }
    set: { group: "4 piles" }
  - say: HAVING judges whole piles. Only rider-1 and rider-2 have at least two rides.
    send: { from: group, to: having, label: "4 piles" }
    set: { having: "2 piles" }
  - say: Only now does SELECT build the columns and name them. ORDER BY sorts last, so it may use the new names.
    send: { from: having, to: select, label: "2 piles" }
    set: { select: "rider-1 2, rider-2 2" }
```

### Why an alias fails in WHERE

An **alias** is the name you give a column with `AS`. SELECT runs after WHERE, so WHERE cannot see an alias yet.

```sql
SELECT id, minutes * 30 AS cost FROM ride WHERE cost > 400;
```

```text
Column "COST" not found; SQL statement:
SELECT id, minutes * 30 AS cost FROM ride WHERE cost > 400 [42122-240]
```

Fix it by repeating the expression, `WHERE minutes * 30 > 400`, or by naming it one layer down, in a CTE (a card below). ORDER BY runs after SELECT, so `ORDER BY cost` is fine.

H2 and MySQL also let HAVING use an alias. PostgreSQL does not. Write `HAVING COUNT(*) >= 2` and it works everywhere.

### Joins in one card

You met `INNER JOIN` and `LEFT JOIN` in SQL basics. Here is the whole family, with `ride` on the left and `rider` on the right.

| Join | Keeps |
|---|---|
| `INNER JOIN` | only pairs that match. The guest ride and Dee both vanish. |
| `LEFT JOIN` | every ride. The guest ride gets a NULL rider name. |
| `RIGHT JOIN` | every rider. Dee appears with a NULL ride id. |
| `FULL JOIN` | everything from both sides. PostgreSQL has it; H2 does not. |
| `CROSS JOIN` | every ride with every rider, no `ON` at all. 8 × 4 = 32 rows. |

```sql
SELECT r.id, p.name FROM ride r RIGHT JOIN rider p ON p.id = r.rider_id ORDER BY r.id NULLS LAST, p.name;
```

```text
id   | name
-----+-----
1    | Ada
2    | Ben
3    | Ada
4    | Cy
6    | Ben
7    | Ada
8    | Cy
null | Dee
```

Most teams write only `LEFT JOIN`. A `RIGHT JOIN` is the same join with the tables swapped, and one direction is easier to read.

### ON or WHERE in an outer join

Jiraiya asks: "How many e-bike rides did each rider take? Show every rider, even with zero." Here is a first try.

```sql
SELECT p.name, COUNT(r.id) AS ebike_rides
FROM rider p
LEFT JOIN ride r ON r.rider_id = p.id
LEFT JOIN bike b ON b.id = r.bike_id
WHERE b.type = 'EBIKE'
GROUP BY p.name ORDER BY p.name;
```

```text
name | ebike_rides
-----+------------
Ada  | 2
```

Ada's 2 is right, but Ben, Cy and Dee vanished. WHERE runs **after** the joins. Dee has no rides, so her `b.type` is NULL. `NULL = 'EBIKE'` is not true, so WHERE throws her row away. Ben's and Cy's rows hold CLASSIC and CARGO bikes, so they go too.

Move the test into the `ON` of the bike join, and count a bike column.

```sql
SELECT p.name, COUNT(b.id) AS ebike_rides
FROM rider p
LEFT JOIN ride r ON r.rider_id = p.id
LEFT JOIN bike b ON b.id = r.bike_id AND b.type = 'EBIKE'
GROUP BY p.name ORDER BY p.name;
```

```text
name | ebike_rides
-----+------------
Ada  | 2
Ben  | 0
Cy   | 0
Dee  | 0
```

```callout warning
In a LEFT JOIN, a test in ON decides which right-side rows match. A test in WHERE throws away whole result rows, including the ones the LEFT JOIN kept on purpose. A WHERE test on the right table quietly turns a LEFT JOIN into an inner join.
```

### Semi-joins: EXISTS

"Which riders started at least one ride at Harbor?" A join answers it, but repeats each rider once per matching ride.

```sql
SELECT p.id, p.name FROM rider p JOIN ride r ON r.rider_id = p.id AND r.start_station_id = 'harbor' ORDER BY p.id;
```

```text
id      | name
--------+-----
rider-1 | Ada
rider-1 | Ada
rider-2 | Ben
rider-2 | Ben
rider-3 | Cy
rider-3 | Cy
```

You want each rider once. That is a **semi-join**: keep a left row if at least one right row matches.

```callout jargon
A **semi-join** keeps each left row that has at least one match, once, and adds no columns from the right. An **anti-join** keeps each left row that has no match at all. A **correlated subquery** is a subquery that uses a column of the outer row, so it runs once per outer row.
```

```sql
SELECT p.id, p.name FROM rider p
WHERE EXISTS (SELECT 1 FROM ride r WHERE r.rider_id = p.id AND r.start_station_id = 'harbor')
ORDER BY p.id;
```

```text
id      | name
--------+-----
rider-1 | Ada
rider-2 | Ben
rider-3 | Cy
```

`EXISTS` asks "is there at least one row?" and stops at the first one. `SELECT 1` inside it is a habit: the columns are never read. `WHERE p.id IN (SELECT r.rider_id ...)` is a semi-join too.

### Anti-joins: NOT EXISTS

"Which riders never started a ride at Harbor?" Flip the test.

```sql
SELECT p.id, p.name FROM rider p
WHERE NOT EXISTS (SELECT 1 FROM ride r WHERE r.rider_id = p.id AND r.start_station_id = 'harbor')
ORDER BY p.id;
```

```text
id      | name
--------+-----
rider-4 | Dee
```

The `LEFT JOIN ... WHERE right.id IS NULL` pattern from SQL basics gives the same answer. Put the Harbor test in its `ON`, as the last card taught.

```sql
SELECT p.id, p.name FROM rider p
LEFT JOIN ride r ON r.rider_id = p.id AND r.start_station_id = 'harbor'
WHERE r.id IS NULL ORDER BY p.id;
```

Both are fine. `NOT EXISTS` says what you mean most plainly, and good planners run both the same way.

### The NOT IN trap

`NOT IN` looks like the obvious anti-join. Ask "which riders never started at Uptown?" Ada, Ben, Cy and Dee all qualify. Only the guest rode from Uptown.

```sql
SELECT p.id, p.name FROM rider p
WHERE p.id NOT IN (SELECT r.rider_id FROM ride r WHERE r.start_station_id = 'uptown')
ORDER BY p.id;
```

```text
id | name
---+-----
```

No rows at all. The subquery returns one value, the guest's NULL. So for Ada the test is `'rider-1' NOT IN (NULL)`, which means `'rider-1' <> NULL`. That is not true and not false. It is **unknown**, and WHERE keeps only true rows.

`NOT EXISTS` with the same test returns all four riders. It never compares with NULL: it only asks whether a matching row exists.

```callout tip
Use NOT EXISTS for anti-joins. If you must use NOT IN, add WHERE r.rider_id IS NOT NULL inside the subquery. Remember that today's NOT NULL column may become nullable next year.
```

### NULL and three-valued logic

SQL logic has three values: TRUE, FALSE and **UNKNOWN**. Any comparison with NULL gives UNKNOWN, and H2 prints UNKNOWN as `null`.

```sql
SELECT NULL = NULL AS eq, NULL <> 'x' AS ne, (NULL = 1) OR TRUE AS or_true, (NULL = 1) AND FALSE AS and_false;
```

```text
eq   | ne   | or_true | and_false
-----+------+---------+----------
null | null | TRUE    | FALSE
```

| a | b | a AND b | a OR b |
|---|---|---|---|
| TRUE | UNKNOWN | UNKNOWN | TRUE |
| FALSE | UNKNOWN | FALSE | UNKNOWN |
| UNKNOWN | UNKNOWN | UNKNOWN | UNKNOWN |

`NOT UNKNOWN` is still UNKNOWN. That is why `NOT IN` breaks: `x NOT IN (a, NULL)` means `x <> a AND x <> NULL`, and the second half is never true. Arithmetic spreads NULL too: `NULL + 5` is NULL, and so is `'B-' || NULL`.

### COUNT(*), COUNT(column) and COALESCE

`COUNT(*)` counts rows. `COUNT(column)` counts rows where that column is not NULL. `COUNT(DISTINCT column)` counts different non-NULL values.

```sql
SELECT COUNT(*) AS all_rows, COUNT(rider_id) AS with_rider, COUNT(DISTINCT rider_id) AS riders FROM ride;
```

```text
all_rows | with_rider | riders
---------+------------+-------
8        | 7          | 3
```

After a LEFT JOIN, the difference matters. Dee has no rides, but she still gets one row, full of NULLs.

```sql
SELECT p.name, COUNT(*) AS star, COUNT(r.id) AS col
FROM rider p LEFT JOIN ride r ON r.rider_id = p.id
GROUP BY p.name ORDER BY p.name;
```

```text
name | star | col
-----+------+----
Ada  | 3    | 3
Ben  | 2    | 2
Cy   | 2    | 2
Dee  | 1    | 0
```

`COALESCE(a, b, ...)` returns its first argument that is not NULL. Use it to show a default.

```sql
SELECT id, COALESCE(rider_id, 'guest') AS who FROM ride WHERE id IN (4, 5, 6) ORDER BY id;
```

```text
id | who
---+--------
4  | rider-3
5  | guest
6  | rider-2
```

One more surprise: `SUM` over zero rows is NULL, not 0. No ride started at Depot, so `SUM(minutes)` there is `null`, while `COALESCE(SUM(minutes), 0)` is `0`.

### GROUP BY, HAVING and CASE

`GROUP BY` makes piles, and `HAVING` keeps whole piles, as in SQL basics. `CASE` adds if-else inside a query.

```sql
SELECT id, minutes,
       CASE WHEN minutes < 10 THEN 'short'
            WHEN minutes < 20 THEN 'medium'
            ELSE 'long' END AS length
FROM ride ORDER BY id;
```

```text
id | minutes | length
---+---------+-------
1  | 10      | medium
2  | 20      | long
3  | 12      | medium
4  | 8       | short
5  | 15      | medium
6  | 20      | long
7  | 6       | short
8  | 30      | long
```

`CASE` tries each `WHEN` from the top and stops at the first true one. With no `ELSE`, a row that matches nothing gets NULL.

Put a `CASE` inside an aggregate, and you count only some rows. This is called **conditional aggregation**.

```sql
SELECT start_station_id, COUNT(*) AS rides,
       SUM(CASE WHEN b.type = 'EBIKE' THEN 1 ELSE 0 END) AS ebike_rides
FROM ride r JOIN bike b ON b.id = r.bike_id
GROUP BY start_station_id ORDER BY start_station_id;
```

```text
start_station_id | rides | ebike_rides
-----------------+-------+------------
harbor           | 6     | 2
riverside        | 1     | 0
uptown           | 1     | 1
```

PostgreSQL and H2 also accept the shorter `COUNT(*) FILTER (WHERE b.type = 'EBIKE')`, which gave the same numbers.

### Window functions keep every row

`GROUP BY` squashes each pile into one row. Sometimes you want the pile's total **next to** each row. That is a **window function**.

```callout jargon
A **window function** computes a value for each row from a set of related rows, called its **window**, without squashing them. The `OVER (...)` clause defines the window. **PARTITION BY** splits rows into groups, and the function restarts in each one.
```

```sql
SELECT id, rider_id, minutes, SUM(minutes) OVER (PARTITION BY rider_id) AS rider_total
FROM ride WHERE rider_id IS NOT NULL ORDER BY rider_id, id;
```

```text
id | rider_id | minutes | rider_total
---+----------+---------+------------
1  | rider-1  | 10      | 28
3  | rider-1  | 12      | 28
7  | rider-1  | 6       | 28
2  | rider-2  | 20      | 40
6  | rider-2  | 20      | 40
4  | rider-3  | 8       | 38
8  | rider-3  | 30      | 38
```

Seven rows went in, and seven came out. With `GROUP BY rider_id`, you would get three rows and lose the ride ids.

### Syntax: a window function

```sql
function(args) OVER (
    PARTITION BY group_columns          -- optional: restart for each group
    ORDER BY sort_columns               -- optional: the order inside each group
    ROWS BETWEEN start AND end          -- optional: the frame, which rows to add up
)
```

- Ranking functions: `ROW_NUMBER()`, `RANK()`, `DENSE_RANK()`.
- Looking at neighbours: `LAG(col)`, `LEAD(col)`.
- Any aggregate works too: `SUM`, `COUNT`, `AVG`, `MIN`, `MAX`.
- Window functions run in the SELECT step, after WHERE, GROUP BY and HAVING. So they can only appear in SELECT and ORDER BY.

### ROW_NUMBER, RANK and DENSE_RANK on ties

The three ranking functions only differ when values tie. Rides 2 and 6 both lasted 20 minutes.

```sql
SELECT id, minutes,
       ROW_NUMBER() OVER (ORDER BY minutes DESC) AS row_num,
       RANK()       OVER (ORDER BY minutes DESC) AS rnk,
       DENSE_RANK() OVER (ORDER BY minutes DESC) AS dense
FROM ride ORDER BY minutes DESC, id;
```

```text
id | minutes | row_num | rnk | dense
---+---------+---------+-----+------
8  | 30      | 1       | 1   | 1
2  | 20      | 2       | 2   | 2
6  | 20      | 3       | 2   | 2
5  | 15      | 4       | 4   | 3
3  | 12      | 5       | 5   | 4
1  | 10      | 6       | 6   | 5
4  | 8       | 7       | 7   | 6
7  | 6       | 8       | 8   | 7
```

- `ROW_NUMBER` gives every row its own number. Between ties, the order is up to the database, so add a tie-breaker such as `id`.
- `RANK` gives ties the same number, then skips: 2, 2, 4. Like a race where two runners share silver and nobody gets bronze.
- `DENSE_RANK` gives ties the same number with no gap: 2, 2, 3.

### Top N per group

"The two longest rides at each station" is a classic. First rank inside each station. Then keep places 1 and 2. You cannot filter on the rank in the same query.

```sql
SELECT id, start_station_id, minutes,
       RANK() OVER (PARTITION BY start_station_id ORDER BY minutes DESC) AS place
FROM ride WHERE place <= 2;
```

```text
Column "PLACE" not found; SQL statement:
SELECT id, start_station_id, minutes, RANK() OVER (PARTITION BY start_station_id ORDER BY minutes DESC) AS place FROM ride WHERE place <= 2 [42122-240]
```

The rank does not exist yet when WHERE runs. Writing the window function itself in WHERE fails too, with `Feature not supported: "Window function"`. So rank in an inner query, and filter in an outer one. The worked example does exactly that.

Which ranking function? The question decides. "Exactly two rows per station" is `ROW_NUMBER` with a tie-breaker. "Everyone in the top two places, ties kept" is `RANK`. "The top two different values" is `DENSE_RANK`.

### LAG and LEAD

`LAG(col)` reads `col` from the row before, inside the same partition. `LEAD(col)` reads the row after. With no such row, you get NULL.

```sql
SELECT rider_id, id, minutes,
       LAG(minutes)  OVER (PARTITION BY rider_id ORDER BY started_at) AS prev_minutes,
       LEAD(minutes) OVER (PARTITION BY rider_id ORDER BY started_at) AS next_minutes
FROM ride WHERE rider_id IS NOT NULL ORDER BY rider_id, started_at;
```

```text
rider_id | id | minutes | prev_minutes | next_minutes
---------+----+---------+--------------+-------------
rider-1  | 1  | 10      | null         | 12
rider-1  | 3  | 12      | 10           | 6
rider-1  | 7  | 6       | 12           | null
rider-2  | 2  | 20      | null         | 20
rider-2  | 6  | 20      | 20           | null
rider-3  | 4  | 8       | null         | 30
rider-3  | 8  | 30      | 8            | null
```

Without `PARTITION BY rider_id`, Ben's first ride would "see" Ada's ride as its previous one. `LAG(minutes, 1, 0)` gives 0 instead of NULL when there is no row before.

Before window functions, this needed a **self-join**: joining the ride table to itself to find "the latest earlier ride of the same rider". That is slower and much harder to read.

### Running totals and frames

Add `ORDER BY` inside `OVER`, and `SUM` becomes a running total. Which rows it adds is the **frame**.

```callout jargon
A **frame** is the slice of the partition a window aggregate adds up for the current row. `ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW` means "from the first row up to this one". `ROWS BETWEEN 2 PRECEDING AND CURRENT ROW` means "this row and the two before it".
```

Rides 6 and 7 started at the same moment, 07:50 on 5 November. Watch the two totals there.

```sql
SELECT id, started_at, fare_cents,
       SUM(fare_cents) OVER (ORDER BY started_at) AS range_total,
       SUM(fare_cents) OVER (ORDER BY started_at, id
                             ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS rows_total
FROM ride ORDER BY started_at, id;
```

```text
id | started_at          | fare_cents | range_total | rows_total
---+---------------------+------------+-------------+-----------
1  | 2026-11-02 08:00:00 | 400        | 400         | 400
2  | 2026-11-02 09:00:00 | 400        | 800         | 800
3  | 2026-11-03 08:15:00 | 280        | 1080        | 1080
4  | 2026-11-03 17:40:00 | 300        | 1380        | 1380
5  | 2026-11-04 12:00:00 | 550        | 1930        | 1930
6  | 2026-11-05 07:50:00 | 400        | 2610        | 2330
7  | 2026-11-05 07:50:00 | 280        | 2610        | 2610
8  | 2026-11-06 08:05:00 | 550        | 3160        | 3160
```

With `ORDER BY` and no frame, the default is `RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW`. **RANGE** treats rows that tie on the ORDER BY value as one step. So ride 6 already includes ride 7's fare: 2610 twice.

**ROWS** counts physical rows, one at a time. It gives 2330, then 2610. Two habits follow:

- Write `ROWS BETWEEN ...` whenever you mean "row by row".
- Give the window ORDER BY a tie-breaker, such as `id`, so the order is the same on every run.

### Window functions over groups

Can you put a running total on top of `GROUP BY`? Yes: window functions run after grouping, so `SUM(COUNT(*)) OVER (...)` adds up the per-group counts. The inner `COUNT(*)` makes one number per day. The outer `SUM ... OVER` runs along the days.

```sql
WITH daily AS (SELECT CAST(started_at AS DATE) AS ride_day, minutes FROM ride)
SELECT ride_day, COUNT(*) AS rides,
       SUM(COUNT(*)) OVER (ORDER BY ride_day ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS rides_so_far
FROM daily GROUP BY ride_day ORDER BY ride_day;
```

```text
ride_day   | rides | rides_so_far
-----------+-------+-------------
2026-11-02 | 2     | 2
2026-11-03 | 2     | 4
2026-11-04 | 1     | 5
2026-11-05 | 2     | 7
2026-11-06 | 1     | 8
```

Why the `daily` CTE? In H2, `GROUP BY CAST(started_at AS DATE)` plus the same `CAST` inside `OVER` fails with `Column "STARTED_AT" must be in the GROUP BY list`. Naming the day first, then grouping by that column, avoids it. Also, `DAY` is a reserved word in H2, so the column is `ride_day`.

### CTEs: name each step with WITH

A **CTE** lets you name a query and use it below, like a variable for a table.

```callout jargon
A **CTE** (common table expression) is a named subquery written before the main query with `WITH name AS (...)`. Each CTE can use the ones above it. It exists only while that one query runs.
```

```sql
WITH rider_rides AS (
    SELECT rider_id, COUNT(*) AS rides FROM ride WHERE rider_id IS NOT NULL GROUP BY rider_id
), busy AS (
    SELECT rider_id FROM rider_rides WHERE rides >= 2
)
SELECT p.name, rr.rides
FROM busy b JOIN rider_rides rr ON rr.rider_id = b.rider_id JOIN rider p ON p.id = b.rider_id
ORDER BY p.name;
```

```text
name | rides
-----+------
Ada  | 3
Ben  | 2
Cy   | 2
```

CTEs fix the alias problem: name a column in one step, filter on it in the next. They also make a long query read top-down, like a recipe. In PostgreSQL 12 and later, the planner can merge a simple CTE into the main query, so it costs nothing extra.

### Recursive CTEs

Some questions loop: "which stations can I reach from Harbor, one bike lane at a time?" A **recursive CTE** refers to itself. Spoke's `station_link` table holds one-way lanes: riverside → old-mill, old-mill → harbor, harbor → riverside, harbor → uptown and uptown → depot.

```sql
WITH RECURSIVE reach(station, hops) AS (
    SELECT to_id, 1 FROM station_link WHERE from_id = 'harbor'      -- the anchor: one hop
    UNION ALL
    SELECT l.to_id, r.hops + 1                                       -- the step: one more lane
    FROM reach r JOIN station_link l ON l.from_id = r.station
    WHERE r.hops < 3                                                 -- the stop condition
)
SELECT station, hops FROM reach ORDER BY hops, station;
```

```text
station   | hops
----------+-----
riverside | 1
uptown    | 1
depot     | 2
old-mill  | 2
harbor    | 3
```

```animate
title: Reaching stations from Harbor, hop by hop
kind: scene
nodes:
  - { id: harbor, label: Harbor, emoji: "🚲", value: start, row: 0, col: 1 }
  - { id: riverside, label: Riverside, emoji: "🌊", row: 1, col: 0 }
  - { id: uptown, label: Uptown, emoji: "🏙️", row: 1, col: 2 }
  - { id: oldmill, label: Old Mill, emoji: "🏭", row: 2, col: 0 }
  - { id: depot, label: Depot, emoji: "🏗️", row: 2, col: 2 }
links:
  - { from: harbor, to: riverside }
  - { from: harbor, to: uptown }
  - { from: riverside, to: oldmill }
  - { from: oldmill, to: harbor }
  - { from: uptown, to: depot }
steps:
  - say: The anchor runs once. It finds every lane out of Harbor and marks those stations as 1 hop.
    send: [{ from: harbor, to: riverside, label: "1" }, { from: harbor, to: uptown, label: "1" }]
    set: { riverside: "1 hop", uptown: "1 hop" }
  - say: The step runs on the rows the last round found. Each takes one more lane, with hops + 1.
    send: [{ from: riverside, to: oldmill, label: "2" }, { from: uptown, to: depot, label: "2" }]
    set: { oldmill: "2 hops", depot: "2 hops" }
  - say: Round three follows Old Mill's lane back to Harbor. The lanes form a loop.
    send: { from: oldmill, to: harbor, label: "3" }
    set: { harbor: "start, and 3 hops" }
  - say: The stop condition hops < 3 ends it. With no new rows, the recursion is done.
    focus: [harbor]
```

The anchor runs once. Then the step runs again and again on the rows the last round produced, until a round adds nothing. Harbor appears at 3 hops because of the loop. `GROUP BY station` with `MIN(hops)`, plus `WHERE station <> 'harbor'`, gives each station once, with its shortest distance.

Remove `WHERE r.hops < 3`, and in H2 the query never finishes. Every trip around the loop makes new rows, with bigger hop counts. On a test run, it was still going after two minutes.

`UNION` instead of `UNION ALL` does not help while `hops` is in the row: `hops` grows each time, so no row ever repeats. Keep only the station column, and PostgreSQL's `UNION` does end on a loop, because a station seen before counts as a repeat. H2 does not: that version also ran until a 10-second timeout. PostgreSQL 14 and later also offer a `CYCLE` clause that marks and stops loops. MySQL and SQL Server stop with an error once a recursion limit is reached.

Always give a recursive CTE a stop condition: a depth limit, or a check that a station is not already on the path.

Recursive CTEs walk any tree or graph kept in a table. Examples are an org chart, folders inside folders, parts inside parts, or a chain of referrals.

### Subquery, join or window function?

Many questions can be answered three ways. Take "each rider's longest ride, ties kept".

| Way | SQL shape | Good when |
|---|---|---|
| Correlated subquery | `WHERE r.minutes = (SELECT MAX(...) WHERE r2.rider_id = r.rider_id)` | the table is small, or the planner turns it into a join |
| Join to a grouped subquery | `JOIN (SELECT rider_id, MAX(minutes) ... GROUP BY rider_id) m ON ...` | you need the group's value and the row |
| Window function | `RANK() OVER (PARTITION BY rider_id ORDER BY minutes DESC)`, then `place = 1` | ranking, top N, neighbours, running totals |

All three returned the same four rows here: rides 3, 2, 6 and 8. Pick the one that reads most clearly, then check the query plan if it is slow. The next ticket shows how to read one. As a rule, a window function reads the table once, while a correlated subquery may read it once per row.

### Reading a big query top-down

Long SQL looks scary. Read it in this order, and it falls apart into small pieces.

1. **Read the CTEs top to bottom.** Say each one in a sentence: "`counts`: rides per rider, station and month".
2. **In the main query, start at FROM and the joins.** What is one row here? A ride? A rider? A rider-month?
3. **Then WHERE, GROUP BY and HAVING.** Which rows survive, and what is one row after grouping?
4. **Then SELECT.** Which columns, and which window functions run over which partitions?
5. **Finally ORDER BY and LIMIT.**

The question "what is one row?" catches most bugs. A join that turns one rider into five rows makes every later `SUM` five times too big.

### In an interview

SQL rounds show up for backend, data and many SDE interviews. Spot these patterns from the wording:

- **"Top N per group", "the second highest", "the latest per user":** a window function in a CTE, then filter. Ask how ties should count before you choose `ROW_NUMBER`, `RANK` or `DENSE_RANK`.
- **"Never", "without any", "who did not":** an anti-join with `NOT EXISTS`. Mention the `NOT IN` and NULL trap, and interviewers will remember you.
- **"Day over day", "time since the last", "consecutive":** `LAG` and `LEAD`. "Running total" or "moving average": a framed `SUM` or `AVG`.
- **"Every employee under this manager", "all sub-folders":** a recursive CTE with a stop condition.

Classic problems: the second highest salary, department top three salaries, consecutive login days, customers who never ordered, and the cumulative sum of sales. Always say what one row means, and state your tie rule out loud.

## Worked example

Jiraiya's newsletter wants "the two longest rides at each station, ties kept". Build it in layers.

**Step 1: rank inside each station.** `PARTITION BY start_station_id` restarts the ranking per station. `ORDER BY minutes DESC` puts the longest first. Ties should share a place, so use `RANK`.

**Step 2: filter one layer up.** The rank only exists after SELECT runs, so name it in a CTE and filter in the main query.

**Step 3: sort the output.** Station, then place, then ride id as a tie-breaker, so the result is the same every run.

```java run
import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:lesson");
             Statement sql = db.createStatement()) {
            sql.execute("""
                CREATE TABLE ride (id INT PRIMARY KEY, rider_id VARCHAR(10), start_station_id VARCHAR(20), minutes INT);
                INSERT INTO ride VALUES (1, 'rider-1', 'harbor', 10), (2, 'rider-2', 'harbor', 20),
                    (3, 'rider-1', 'riverside', 12), (4, 'rider-3', 'harbor', 8), (5, NULL, 'uptown', 15),
                    (6, 'rider-2', 'harbor', 20), (7, 'rider-1', 'harbor', 6), (8, 'rider-3', 'harbor', 30);
                """);
            ResultSet rows = sql.executeQuery("""
                WITH ranked AS (
                    SELECT id, start_station_id, minutes,
                           RANK() OVER (PARTITION BY start_station_id ORDER BY minutes DESC) AS place
                    FROM ride
                )
                SELECT start_station_id, id, minutes, place
                FROM ranked
                WHERE place <= 2
                ORDER BY start_station_id, place, id
                """);
            while (rows.next()) {
                System.out.println(rows.getString("start_station_id") + ": ride " + rows.getInt("id")
                        + ", " + rows.getInt("minutes") + " min, place " + rows.getInt("place"));
            }
        }
    }
}
```

It prints:

```text
harbor: ride 8, 30 min, place 1
harbor: ride 2, 20 min, place 2
harbor: ride 6, 20 min, place 2
riverside: ride 3, 12 min, place 1
uptown: ride 5, 15 min, place 1
```

Line by line:

1. `WITH ranked AS (...)` names step 1. Each of the 8 rides gets a `place` inside its own station.
2. At Harbor, ride 8 (30 min) is place 1. Rides 2 and 6 tie on 20 minutes, so both get place 2.
3. `WHERE place <= 2` runs in the outer query, where `place` is now an ordinary column.
4. Harbor lists three rides, not two. That is the tie rule you chose. With `ROW_NUMBER`, ride 6 would vanish.
5. Riverside and Uptown have one ride each, so each lists one ride.

Your practice needs one more layer underneath: count rides per rider, station and month first, then rank those counts.

## Try it

Run the program. Then change the `RANK()` line and run again after each change.

1. Swap `RANK()` for `ROW_NUMBER()`, then for `DENSE_RANK()`. Watch rides 2 and 6, and ride 5 after them.
2. Use `RANK() OVER (PARTITION BY rider_id ORDER BY minutes DESC)`. Where does the ranking restart?
3. Use `LAG(minutes) OVER (PARTITION BY rider_id ORDER BY started_at)`. Which rows get `null`, and why?
4. Use `SUM(minutes) OVER (ORDER BY started_at)`, then add `ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW`. Rides 6 and 7 tie on `started_at`.

```java run
import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:lesson");
             Statement sql = db.createStatement()) {
            sql.execute("""
                CREATE TABLE ride (id INT PRIMARY KEY, rider_id VARCHAR(10), start_station_id VARCHAR(20),
                                   started_at TIMESTAMP, minutes INT);
                INSERT INTO ride VALUES
                    (1, 'rider-1', 'harbor', '2026-11-02 08:00', 10), (2, 'rider-2', 'harbor', '2026-11-02 09:00', 20),
                    (3, 'rider-1', 'riverside', '2026-11-03 08:15', 12), (4, 'rider-3', 'harbor', '2026-11-03 17:40', 8),
                    (5, NULL, 'uptown', '2026-11-04 12:00', 15), (6, 'rider-2', 'harbor', '2026-11-05 07:50', 20),
                    (7, 'rider-1', 'harbor', '2026-11-05 07:50', 6), (8, 'rider-3', 'harbor', '2026-11-06 08:05', 30);
                """);
            String query = """
                SELECT id, rider_id, minutes,
                       RANK() OVER (ORDER BY minutes DESC) AS result
                FROM ride
                ORDER BY minutes DESC, id
                """;
            ResultSet rows = sql.executeQuery(query);
            while (rows.next()) {
                System.out.println("ride " + rows.getInt("id") + " | " + rows.getString("rider_id")
                        + " | " + rows.getInt("minutes") + " min | " + rows.getString("result"));
            }
        }
    }
}
```

## Recap

- A query runs in the order FROM, WHERE, GROUP BY, HAVING, SELECT, ORDER BY. So WHERE cannot see a SELECT alias or a window function: filter one layer up, in a CTE.
- In a LEFT JOIN, put tests on the right table in ON. A WHERE test there turns it into an inner join.
- Anti-joins use NOT EXISTS. NOT IN returns nothing once its list holds a NULL, because comparisons with NULL are UNKNOWN. COUNT(column) skips NULLs, and COALESCE fills them in.
- Window functions add a value to every row without squashing rows. RANK, DENSE_RANK and ROW_NUMBER differ on ties; LAG and LEAD read neighbours; ROWS frames make running totals.
- CTEs name each step of a long query. A recursive CTE walks a graph from an anchor, and always needs a stop condition.

## Common mistakes

**1. Filtering on a SELECT alias in WHERE.**

```sql
SELECT id, minutes * 30 AS cost FROM ride WHERE cost > 400;
```

```text
Column "COST" not found; SQL statement:
SELECT id, minutes * 30 AS cost FROM ride WHERE cost > 400 [42122-240]
```

WHERE runs before SELECT, so the alias does not exist yet. Fix: repeat the expression in WHERE, or compute it in a CTE and filter in the main query.

**2. NOT IN against a list that holds a NULL.**

```sql
SELECT p.id, p.name FROM rider p
WHERE p.id NOT IN (SELECT r.rider_id FROM ride r WHERE r.start_station_id = 'uptown');
```

```text
id | name
---+-----
```

No error, and no rows. The guest ride put a NULL in the list, and `x <> NULL` is never true. Fix: `WHERE NOT EXISTS (SELECT 1 FROM ride r WHERE r.rider_id = p.id AND r.start_station_id = 'uptown')`.

**3. A WHERE test on the right side of a LEFT JOIN.**

```sql
SELECT p.name, COUNT(r.id) AS ebike_rides
FROM rider p LEFT JOIN ride r ON r.rider_id = p.id LEFT JOIN bike b ON b.id = r.bike_id
WHERE b.type = 'EBIKE' GROUP BY p.name;
```

```text
name | ebike_rides
-----+------------
Ada  | 2
```

Ben, Cy and Dee should show 0, but WHERE threw their rows away. Fix: move `b.type = 'EBIKE'` into the bike join's ON, and count `b.id`.

**4. A window function in WHERE.**

```sql
SELECT id, rider_id FROM ride WHERE ROW_NUMBER() OVER (ORDER BY id) <= 2;
```

```text
Feature not supported: "Window function"; SQL statement:
SELECT id, rider_id FROM ride WHERE ROW_NUMBER() OVER (ORDER BY id) <= 2 [50100-240]
```

Window functions run in the SELECT step, after WHERE. Fix: compute the number in a CTE, then filter on it in the main query.

**5. ROW_NUMBER when ties should share a place.**

```sql
WITH ranked AS (SELECT id, minutes, ROW_NUMBER() OVER (ORDER BY minutes DESC) AS place FROM ride)
SELECT id, minutes FROM ranked WHERE place <= 2;
```

Symptom: rides 2 and 6 both took 20 minutes, but only one is listed, and which one can change between runs. Fix: decide the tie rule first. "Ties share a place" is `RANK`; "the top two different values" is `DENSE_RANK`.

**6. A running total with the default frame and tied ORDER BY values.**

```sql
SELECT id, SUM(fare_cents) OVER (ORDER BY started_at) AS running FROM ride;
```

Symptom: rides 6 and 7 started at the same moment, and both show 2610. The default frame is RANGE, which adds tied rows together. Fix: `OVER (ORDER BY started_at, id ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)` gives 2330, then 2610.

**7. COUNT(*) after a LEFT JOIN.**

```sql
SELECT p.name, COUNT(*) AS rides FROM rider p LEFT JOIN ride r ON r.rider_id = p.id GROUP BY p.name;
```

Symptom: Dee has no rides, but the report says 1. The LEFT JOIN kept one row for her, full of NULLs, and `COUNT(*)` counted it. Fix: `COUNT(r.id)` skips the NULL and gives 0.
