# Testing a Next.js app

dhaaga.in on Next.js has three parts with no tests yet: a route handler, a server component page and a server action. Your job is `src/appChecks.jsx`: checks that pass on today's code and fail the moment someone breaks it.

Don't change anything under `src/app/` or `src/lib/`. The parts are:

- `GET` in `app/api/stock/route.js`: `GET /api/stock?id=DC-1042` answers `{ id: "DC-1042", stock: 7 }`. An unknown id answers **404** with `{ error: "No item DC-9999" }`.
- `ItemPage` in `app/items/[id]/page.jsx`: an async server component. Its `params` is a **Promise**, as in Next.js 16. It shows the name in an `<h1>` and the price, like `₹450.00`. For an unknown id it calls `notFound()`.
- `placeOrder(formData)` in `app/actions.js`: reads `id` and `qty`. A quantity outside 1 to 5 gives `{ ok: false, error: "Pick a quantity from 1 to 5" }`. A good order gives `{ ok: true, orderId, totalPaise }`.

Each check is an `async` function that gets `{ GET, ItemPage, placeOrder }` and uses the part it needs. `checkStockFound` is a worked example to copy.

1. **TODO 1:** `checkStockUnknown`. Call `GET` for `DC-9999`. Expect status `404` and `{ error: "No item DC-9999" }`.
2. **TODO 2:** `checkItemPage`. `render(await ItemPage({ params: Promise.resolve({ id: "DC-1042" }) }))`. Expect an `h1` named **Golu the elephant** and the text **₹450.00**.
3. **TODO 3:** `checkItemNotFound`. Call `ItemPage` for `DC-9999` and expect it to reject with `NEXT_NOT_FOUND`: `await expect(promise, "message").rejects.toThrow("NEXT_NOT_FOUND")`.
4. **TODO 4:** `checkPlaceOrder`. Build a `FormData` with `id` `DC-1042` and `qty` `"0"`, and expect the error. Then `qty` `"2"`: expect `{ ok: true, orderId: "order-1", totalPaise: 90000 }`.

## How your checks are graded

While the checks run, `next/navigation` is mocked: `notFound()` throws an `Error` whose message is `NEXT_NOT_FOUND`. Before each check, the order list is emptied, so the first order is always `order-1`.

First, every check runs on the real app. All of them must pass.

Then your checks run on seven broken copies, each with one bug:

- the stock comes back as text, `"7"`
- an unknown item answers 200 instead of 404
- the page shows `₹45000.00`
- the page reads `params.id` without awaiting `params`
- the page shows "Item not found" instead of calling `notFound()`
- the action accepts a quantity of 0
- the action charges for one item whatever the quantity

At least one of your checks must fail on each broken copy. The first one is already caught by the worked example.
