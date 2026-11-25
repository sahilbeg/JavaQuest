## Hook

A tailor doesn't only check the finished kurta. She checks the cloth before cutting, the seams after stitching, and the fit at the end. Each check catches a different mistake, and each is quicker than the next one.

A Next.js app has parts too: pages that run on the server, route handlers that answer requests, and server actions that change data. Each part can be checked on its own, quickly, before the whole app ever runs. That's what this ticket is about.

## Concept

### What to test in a Next.js app

You met Vitest in [Writing tests with Vitest](/t/web-07-tooling/topic-07-unit-tests) and page tests in [Testing the page](/t/web-07-tooling/topic-08-testing-the-dom). A Next.js app adds three kinds of code you built in [Data and the server](/t/next-11-data-and-server/topic-01-server-fetching):

| Part | What it is | How a check calls it |
|---|---|---|
| Server component | an async function that returns JSX | `await` it, then render the JSX |
| Route handler | `GET`, `POST`... functions in `route.js` | give it a `Request`, read the `Response` |
| Server action | an async function marked `"use server"` | give it a `FormData`, read what it returns |

All three are plain JavaScript functions. That's the good news: you can call them from a test, just like any other function.

```callout jargon
A **unit test** checks one small part on its own, in milliseconds. An **end-to-end test** (E2E) starts the whole app and drives a real browser through it, like a customer would. It's slower, but it sees everything working together.
```

### React Testing Library in one card

```callout jargon
**React Testing Library** (RTL) draws a component into **jsdom**, a pretend browser page written in JavaScript, and lets you find things the way a person would: by role, label and text.
```

```jsx
render(<p>₹450.00</p>);
screen.queryByText("₹450.00");                                   // the element, or null
screen.queryByRole("heading", { level: 1, name: "Golu the elephant" });
```

`render` draws into the page, and `screen` searches it. A `queryBy...` query returns `null` when nothing matches, so `expect(found, "message").not.toBeNull()` fails with your own friendly message. Call `cleanup` after each test to empty the page again.

### Syntax: testing a server component

```jsx
render(await ItemPage({ params: Promise.resolve({ id: "DC-1042" }) }));
```

A server component is an `async` function. RTL can't wait for a component while it draws, so you do the waiting yourself. Call the component like a function with its props, `await` the JSX it returns, and render that.

This works when the children inside are ordinary components. If a page holds another async component inside it, test that child on its own the same way, or leave it to an E2E test.

### params is a Promise

In Next.js 16, a page's `params` and `searchParams` are **Promises**. The page must `await params` before it reads `id`. So the test must pass a Promise too.

```animate
title: Reading id from a Promise
kind: code
language: js
code: |
  const params = Promise.resolve({ id: "DC-1042" });
  console.log(params.id);
  params.then((value) => console.log(value.id));
  console.log("page asked for its params");
steps:
  - line: 1
    vars: { params: "Promise" }
    say: Next.js 16 hands the page a Promise, not a plain object. The id is inside it, waiting.
  - line: 2
    out: "undefined"
    say: A Promise has no id property. Code that reads params.id gets undefined, and finds no item.
  - line: 3
    say: then waits for the Promise. Its callback can't run until the current code finishes.
  - line: 4
    out: "page asked for its params"
    say: The plain code carries on first.
  - line: 3
    out: "DC-1042"
    say: Now the callback runs with the real object. await does the same thing, more neatly.
```

If a test passes a plain object, `await params` still works, because awaiting a plain value just gives it back. But a page that forgot the `await` would pass too. Pass `Promise.resolve(...)`, and the test behaves like the real Next.js.

### Syntax: testing a route handler

```js
it("answers with Golu's stock", async () => {
  const response = await GET(new Request("http://localhost/api/stock?id=DC-1042"));
  const body = await response.json();
});
```

A route handler takes a real `Request` and returns a real `Response`, the same objects `fetch` uses. Node has both built in. So a check builds a `Request` with a full URL, calls the handler, and reads `response.status` and `await response.json()`.

Check the status as well as the body. A handler that answers `200` with an error message is a bug that customers' apps will trip over.

### Syntax: testing a server action

```js
it("places an order for two Golus", async () => {
  const form = new FormData();
  form.set("id", "DC-1042");
  form.set("qty", "2");
  const result = await placeOrder(form);
});
```

The `"use server"` line tells Next.js to run the function on the server. In a test, it's just a string at the top of the file, and the action is an ordinary async function. Build the `FormData` the form would send, call it, and check what it returns.

Every field in a `FormData` is text. A quantity arrives as `"2"`, never `2`, so test with text exactly as a browser sends it.

### Syntax: mocking next/navigation

```js
vi.mock("next/navigation", () => ({
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
  redirect: vi.fn((to) => {
    throw new Error("NEXT_REDIRECT " + to);
  }),
}));
```

```callout jargon
To **mock** a module is to swap it for a fake one during a test. `vi.mock(path, factory)` tells Vitest: whenever any file imports this path, give it what `factory` returns. `vi.fn` makes a fake function that also remembers its calls.
```

`notFound()` and `redirect()` work by throwing a special error that only the Next.js server knows how to catch. In a test there is no Next.js server. So you replace them with fakes that throw an error you can recognise.

Then a check can say "this page must call notFound": `await expect(ItemPage(...), "message").rejects.toThrow("NEXT_NOT_FOUND")`. The page is async, so a throw inside it becomes a rejected promise.

### A data layer you can reset

```callout jargon
A **data layer** is the one module that reads and writes the store's data, like `lib/orders.js`. Pages, handlers and actions ask it, instead of each touching the data in its own way.
```

In these exercises the data layer keeps orders in memory. That makes checks fast, but it also means one check's order is still there for the next one. Give the data layer a `resetOrders()` function, and call it before each check, in a `beforeEach`. Then every check starts from the same empty shop, and the first order is always `order-1`.

### A check that's worth keeping

A check earns its place only if it fails when the code is wrong. The way to find out is to break the code on purpose, one bug at a time, and see whether any check goes red.

```animate
title: One check meets a broken page
kind: scene
nodes:
  - { id: check, label: checkItemPage, emoji: "🧪", row: 0, col: 0 }
  - { id: real, label: Real ItemPage, emoji: "✅", value: "await params", row: 0, col: 3 }
  - { id: broken, label: Broken ItemPage, emoji: "🐛", value: "params.id", row: 2, col: 3 }
steps:
  - say: The check calls the real page with params as a Promise, like Next.js 16 does.
    send: { from: check, to: real, label: "params: Promise" }
  - say: The real page awaits params, finds Golu and returns his heading and ₹450.00. The check passes.
    send: { from: real, to: check, label: "<h1>Golu the elephant</h1>", back: true }
    set: { check: passed }
  - say: Now the same check calls a broken copy that reads params.id without await.
    send: { from: check, to: broken, label: "params: Promise" }
    set: { check: running }
  - say: params.id is undefined, so no item is found and the page calls notFound. The check fails. Bug caught.
    send: { from: broken, to: check, label: "NEXT_NOT_FOUND", back: true }
    set: { check: "failed: caught it" }
    focus: [check]
```

### What unit tests can't see

Unit tests call each part directly. They skip the parts Next.js does for you: routing a URL to the right file, streaming, caching, cookies, and the real browser. A page could pass every unit test and still be unreachable because a folder has the wrong name.

So teams add a few **end-to-end tests**, usually with a tool called Playwright. Each one starts the built app with `next build` and `next start`, opens a real browser, and walks a whole journey: open Golu's page, add him to the cart, place the order. Keep these few and important. Most checks stay as quick unit tests.

## Worked example

Here's a full test file for a tiny route handler, `GET /api/hours`, which says whether the Aundh shop is open on a given day.

```js
export async function GET(request) {
  const day = new URL(request.url).searchParams.get("day");
  const days = ["Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
  if (!day) {
    return Response.json({ error: "Missing day" }, { status: 400 });
  }
  return Response.json({ day, open: days.includes(day) });
}
```

```js
import { describe, it, expect } from "vitest";
import { GET } from "../src/app/api/hours/route.js";

describe("GET /api/hours", () => {
  it("says the shop is open on Sunday", async () => {
    const response = await GET(new Request("http://localhost/api/hours?day=Sun"));
    expect(response.status, "a good request should answer 200").toBe(200);
    expect(await response.json(), "the shop opens on Sunday").toEqual({ day: "Sun", open: true });
  });

  it("refuses a request with no day", async () => {
    const response = await GET(new Request("http://localhost/api/hours"));
    expect(response.status, "no day should answer 400 Bad Request").toBe(400);
  });
});
```

Line by line:

- The handler is imported straight from its `route.js`. No server starts.
- `new Request(...)` needs a full URL. The host doesn't matter, so `http://localhost` is fine.
- `toEqual` compares the whole JSON body, so a wrong field or a `"true"` string would fail.
- The second test checks the status of a bad request. That's an edge a customer's app will meet.

Now break the handler in your head. Remove the `!day` check: the second test fails. Swap `Sun` out of the list: the first one fails. Both tests earn their place.

## Try it

This runs a stock handler three times with real `Request` and `Response` objects. Run it.

```js run
async function GET(request) {
  const id = new URL(request.url).searchParams.get("id");
  const stock = { "DC-1042": 7, "DC-1205": 0 };
  if (!id) return Response.json({ error: "Missing id" }, { status: 400 });
  if (!(id in stock)) return Response.json({ error: "No item " + id }, { status: 404 });
  return Response.json({ id, stock: stock[id] });
}

async function main() {
  for (const query of ["?id=DC-1042", "?id=DC-9999", ""]) {
    const response = await GET(new Request("http://localhost/api/stock" + query));
    console.log(response.status, JSON.stringify(await response.json()));
  }
}

main();
```

It prints:

```text
200 {"id":"DC-1042","stock":7}
404 {"error":"No item DC-9999"}
400 {"error":"Missing id"}
```

1. Add `"?id=DC-1205"` to the list. The keychain has 0 in stock. Is that a 200 or a 404, and why is that right?
2. Change the 404 line to return status 200. Which line of the output changes, and what would a check need to compare to notice?

## Recap

- Server components, route handlers and server actions are plain async functions, so a quick unit test can call each one directly.
- Test a server component by awaiting it with its props, then rendering the JSX it returns. In Next.js 16, pass `params` as `Promise.resolve({ ... })`.
- Test a route handler with a real `Request`, and check both `response.status` and `await response.json()`.
- Test a server action with a `FormData` you build, using text values exactly as a form sends them.
- Mock `next/navigation` so `notFound()` and `redirect()` throw errors you can expect, reset the data layer before each test, and keep a few end-to-end tests for whole journeys.

## Common mistakes

**1. Rendering an async component as a tag.**

```jsx
render(<ItemPage params={Promise.resolve({ id: "DC-1042" })} />);
```

Symptom: Golu's heading never shows up, and React complains about an async component. RTL draws like a browser and can't wait for a server component. Fix: `render(await ItemPage({ params: Promise.resolve({ id: "DC-1042" }) }))`.

**2. Passing params as a plain object.**

```jsx
render(await ItemPage({ params: { id: "DC-1042" } }));
```

Symptom: every check is green, but a page that reads `params.id` without `await` also passes. In Next.js 16 that page shows a 404 for every item. Fix: always pass `params: Promise.resolve({ id: "DC-1042" })`, like the real framework.

**3. Forgetting to mock next/navigation.**

```jsx
await expect(ItemPage({ params: Promise.resolve({ id: "DC-9999" }) }), "404").rejects.toThrow("NEXT_NOT_FOUND");
```

Symptom: the check fails, because the real `notFound()` throws Next.js's own internal error with a different message. Fix: add the `vi.mock("next/navigation", ...)` block at the top of the test file.

**4. Forgetting await before expect(...).rejects.**

```js
expect(ItemPage({ params: Promise.resolve({ id: "DC-9999" }) }), "404").rejects.toThrow("NEXT_NOT_FOUND");
```

Symptom: the check doesn't wait for the page. It can finish, and look green, before the promise settles, so it can't trust what it sees. Fix: `await expect(...).rejects.toThrow(...)`.

**5. Not resetting the data layer.**

```js
// no beforeEach(resetOrders) in this file
```

Symptom: the order check passes alone but fails after another test placed an order, because the id is `order-2`, not `order-1`. Fix: call `resetOrders()` in a `beforeEach`, so every test starts with an empty shop.
