# Streaming with Suspense

Make Meera Aunty's dashboard show its heading at once, and let each slow card fill in on its own.

`src/lib/reports.js` is done, and slow. `getTodaySales()` answers `{ orders: 12, totalPaise: 845000 }` and `getLowStock()` answers a list like `[{ id: "DC-1042", name: "Golu the elephant", left: 2 }]`. `rupees()` in `src/lib/money.js` is done too.

Change `src/app/dashboard/cards.jsx`:

1. **TODO 1:** `SalesCard({ salesPromise })` reads the sales with `use(salesPromise)` and shows `<h2>Today's sales</h2>` and a paragraph like `12 orders, ₹8,450.00`.
2. **TODO 2:** `LowStockCard({ stockPromise })` reads the list with `use(stockPromise)` and shows `<h2>Low stock</h2>` and one `<li>` per item, like `Golu the elephant: 2 left`. An empty list shows `Nothing is running low.` instead.

Change `src/app/dashboard/page.jsx`:

3. **TODO 3:** start both reports at the top of `DashboardPage`, **without** `await`, and pass the promises to the cards. The page must return straight away.
4. **TODO 4:** under the heading, wrap each card in its own `<Suspense>`: the sales card with the fallback `<p>Loading today's sales...</p>`, the stock card with `<p>Loading low stock...</p>`.

The checks replace the reports with pretend ones they can finish whenever they like.
