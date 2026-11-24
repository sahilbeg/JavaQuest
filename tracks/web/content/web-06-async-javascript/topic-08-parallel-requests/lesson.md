## Hook

You need to know if three friends are coming to the Diwali pop-up. You could phone the first, wait for her answer, hang up, phone the second, and so on. Or you could send one message to all three and read the answers as they come in.

The second way is much faster, because the waiting happens at the same time. Nobody's answer depends on anyone else's.

Web requests are the same. When the answers don't depend on each other, ask for them all at once.

## Concept

### One after another is slow

Here is the stall's stock list, written the way you learned with `for...of`:

```js
async function stockOneByOne(ids) {
  const list = [];
  for (const id of ids) {
    list.push(await getStock(id));
  }
  return list;
}
```

Each round waits for its answer before the next request even starts. If one answer takes a second, twelve items take twelve seconds.

```callout jargon
Requests are **sequential** when each one starts only after the last has finished. They run **in parallel** when they all start together and wait at the same time. A chain of sequential requests that didn't need to wait is often called a **waterfall**.
```

```animate
title: Three items, two ways
kind: scene
nodes:
  - { id: tablet, label: Stall tablet, emoji: "📟", value: "0 s" }
  - { id: server, label: dhaaga.in API, emoji: "🖥️", value: idle }
steps:
  - say: One by one. The tablet asks for Golu and waits a whole second for the answer.
    send: [{ from: tablet, to: server, label: "DC-1042?" }, { from: server, to: tablet, label: "3", back: true }]
    set: { tablet: "1 s" }
  - say: Only then does it ask for the coaster set, and wait again.
    send: [{ from: tablet, to: server, label: "DC-2001?" }, { from: server, to: tablet, label: "12", back: true }]
    set: { tablet: "2 s" }
  - say: And then the tote bag. Three items took three seconds, and most of that was waiting.
    send: [{ from: tablet, to: server, label: "DC-3001?" }, { from: server, to: tablet, label: "0", back: true }]
    set: { tablet: "3 s" }
  - say: Now all at once. The tablet sends all three requests straight away.
    set: { tablet: "0 s", server: "3 requests" }
    send: [{ from: tablet, to: server, label: "DC-1042?" }, { from: tablet, to: server, label: "DC-2001?" }, { from: tablet, to: server, label: "DC-3001?" }]
  - say: The server works on them together. All three answers are back after about one second.
    send: [{ from: server, to: tablet, label: "3", back: true }, { from: server, to: tablet, label: "12", back: true }, { from: server, to: tablet, label: "0", back: true }]
    set: { tablet: "1 s", server: idle }
```

### Start them all, then wait

Calling an async function starts its work straight away and gives you a promise. So if you call `getStock` for every id **without** awaiting each one, every request starts at once:

```js
const promises = ["DC-1042", "DC-2001", "DC-3001"].map((id) => getStock(id));
```

`promises` is an array of three pending promises, and three requests are already on their way. Now you need one way to wait for all of them.

### Syntax: Promise.all

```js
const values = await Promise.all(promises);
```

`Promise.all` takes an array of promises and returns **one** promise.

- It is fulfilled when every promise is fulfilled.
- Its value is an array of their values, **in the same order as the promises you passed in**, whatever order they finished in.

```js run
const wait = (ms, value) => new Promise((resolve) => setTimeout(() => resolve(value), ms));

Promise.all([wait(30, "DC-1042: 3"), wait(10, "DC-2001: 12"), wait(20, "DC-3001: 0")])
  .then((results) => console.log(results.join(" | ")));
console.log("Asked for all three");
```

The coaster set finished first, but the results still come out in the order you asked. So `values[0]` always belongs to `ids[0]`.

### Pairing results with their ids

Because the order is kept, you can join each value back to its id by position:

```js
async function stockWithIds(ids) {
  const stocks = await Promise.all(ids.map((id) => getStock(id)));
  return ids.map((id, i) => ({ id, stock: stocks[i] }));
}
```

The second `map` gets the index `i` as its second argument. `stocks[i]` is the answer for `ids[i]`.

### One failure fails them all

`Promise.all` is **fail-fast**: as soon as any promise is rejected, the whole thing is rejected with that error. The other answers are thrown away, even the ones that worked.

```js run
const wait = (ms, value) => new Promise((resolve) => setTimeout(() => resolve(value), ms));
const fail = (ms, message) => new Promise((_, reject) => setTimeout(() => reject(new Error(message)), ms));

Promise.all([wait(20, "DC-1042: 3"), fail(10, "404 for DC-9999")])
  .then((results) => console.log(results.join(" | ")))
  .catch((error) => console.log(`Whole list failed: ${error.message}`));
```

That's right when you need every answer, such as all the prices in a cart. It's wrong for a stock list, where one bad id shouldn't hide the other eleven.

### Syntax: Promise.allSettled

```js
const results = await Promise.allSettled(promises);
```

`Promise.allSettled` waits for every promise to settle and **never rejects**. Each result is an object that says how it went:

- `{ status: "fulfilled", value: 3 }`
- `{ status: "rejected", reason: theError }`

```js run
const ok = Promise.resolve(3);
const bad = Promise.reject(new Error("404 for DC-9999"));

Promise.allSettled([ok, bad]).then((results) => {
  for (const result of results) {
    if (result.status === "fulfilled") {
      console.log("fulfilled:", result.value);
    } else {
      console.log("rejected:", result.reason.message);
    }
  }
});
```

The results keep the order of the promises too, so result `i` belongs to id `i`.

### Two more: Promise.race and Promise.any

```callout jargon
`Promise.race(promises)` settles like the **first** promise to settle, fulfilled or rejected. `Promise.any(promises)` is fulfilled by the first promise that is **fulfilled**, and ignores failures unless all of them fail.
```

You'll use `all` and `allSettled` most. `race` is handy for a time limit: race the request against a timer that rejects.

### When to stay sequential

Parallel only works when the requests don't need each other. Reserving an item and then paying for it must stay in order, because paying needs the reservation to have worked. Use `Promise.all` for independent requests, and one `await` after another for steps that depend on each other.

## Worked example

The Diwali pop-up's screen shows stock for three items, and should still show the others if one id is wrong:

```js
async function popUpBoard(ids) {
  const results = await Promise.allSettled(ids.map((id) => getStock(id)));
  return results.map((result, i) =>
    result.status === "fulfilled" ? `${ids[i]}: ${result.value} left` : `${ids[i]}: unknown`,
  );
}
```

Line by line:

1. `ids.map((id) => getStock(id))` starts every request at once and makes an array of promises.
2. `await Promise.allSettled(...)` waits for all of them. It never throws, even if some fail.
3. `results.map` turns each result into a line. `i` pairs the result with its id.
4. A fulfilled result shows its number. A rejected one shows `unknown`, and the other lines still appear.

## Try it

In the `Promise.all` example, change the delays to 10, 20 and 30. The output order doesn't change. Then replace `Promise.all` with `Promise.race` and `.join(" | ")` with nothing (just `console.log(results)`). Which single value comes out now?

## Recap

- Requests that don't depend on each other should run in parallel. Awaiting them one by one makes a slow waterfall.
- Start every request first (`ids.map((id) => getStock(id))`), then wait for them all together.
- `Promise.all` gives the values in the order you passed the promises, and rejects as soon as any one fails.
- `Promise.allSettled` never rejects. Each result says `fulfilled` with a `value` or `rejected` with a `reason`.
- Steps that depend on each other, like reserve then pay, stay sequential.

## Common mistakes

**1. Getting an array of promises instead of values.**

```js run
async function getStock(id) {
  return { "DC-1042": 3, "DC-2001": 12 }[id];
}

const stocks = ["DC-1042", "DC-2001"].map(async (id) => await getStock(id));
console.log(stocks.length, stocks[0] instanceof Promise);
```

This prints `2 true`. The `await` inside the callback only waits inside that callback. `map` still hands back promises. Fix: `const stocks = await Promise.all(ids.map((id) => getStock(id)));`.

**2. Awaiting independent requests one by one.**

```js
async function slowBoard(ids) {
  const list = [];
  for (const id of ids) {
    list.push(await getStock(id));
  }
  return list;
}
```

It works, but twelve items take twelve round trips. Fix: start them all with `map` and wait once with `Promise.all`.

**3. Using Promise.all when some failures are fine.**

```js
async function board(ids) {
  return Promise.all(ids.map((id) => getStock(id)));
}
```

One unknown id rejects the whole list, and the customer sees nothing. Fix: use `Promise.allSettled` and show the ones that worked.

**4. Passing promises one by one instead of in an array.**

```js
async function both(a, b) {
  return Promise.all(getStock(a), getStock(b));
}
```

`Promise.all` takes **one** argument: an array. This version rejects with a `TypeError` about something not being iterable. Fix: `Promise.all([getStock(a), getStock(b)])`.
