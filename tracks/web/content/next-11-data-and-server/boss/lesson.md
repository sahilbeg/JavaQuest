## What happened

The flash sale opened at 9 pm. Within the first minute, Asha Rao and Ravi Joshi both pressed Place order at almost the same moment. Both thank-you pages said "Your order is order-20". Only Ravi's order is in the database. Asha's was saved first, then quietly overwritten by Ravi's, because they had the same id.

Two more reports came in the next morning. Raju deleted a test order, and the next customer's order replaced Neha Desai's order-19. And customers who double-tapped Place order on slow 3G ended up with two orders each.

Arjun traced all three to `createOrder` in `src/lib/orders.js` and the `placeOrder` server action that calls it.

```callout jargon
A **race condition** is a bug where the result depends on which of two things runs first. You met one in web-06 with slow answers in the browser. On a server, it happens when two requests read and change the same data at the same time.
```

One Node.js server handles many requests at once. While one request waits for the database, another one runs. Here's the old code's problem in miniature:

```animate
title: Two orders read the same count
kind: code
language: js
code: |
  let count = 3;
  async function take(name) {
    const seen = count;
    await null;
    count += 1;
    console.log(`${name} got order-${17 + seen}`);
  }
  take("Asha");
  take("Ravi");
steps:
  - line: 1
    vars: { count: 3 }
    say: Three orders are saved, order-17 to order-19.
  - line: 8
    say: Asha's order starts first.
  - line: 3
    vars: { seen: 3 }
    say: Asha's request reads the count. It's 3, so her id will be order-20.
  - line: 4
    say: It waits for the database. While it waits, Ravi's request gets a turn.
  - line: 9
    say: Ravi's order starts, a moment later.
  - line: 3
    vars: { seen: 3 }
    say: Ravi's request reads the count too. Asha's order isn't saved yet, so it's still 3.
  - line: 5
    vars: { count: 4 }
    say: Asha's request carries on and saves her order.
  - line: 6
    out: "Asha got order-20"
    say: Asha is told her order is order-20.
  - line: 5
    vars: { count: 5 }
    say: Ravi's request saves his, with the id it worked out from the old count.
  - line: 6
    out: "Ravi got order-20"
    say: Same number. In the real database, the second save replaced the first.
```

```callout jargon
A **read-then-write race** happens when code reads a value, waits, then writes based on what it read. Another request can change the value in between, so the write is based on old information.
```

## What you need

There's no new syntax. You need topics 4 to 6 (actions and the data layer) and the idea of a race from web-06.

`src/lib/db.js` is a pretend database, and every call waits a moment, like a real one. It has:

- `countOrders()`, `findOrder(id)`, `allOrders()`, `insertOrder(order)` (which replaces an order with the same id) and `deleteOrder(id)`.
- `nextOrderNumber()`, which works like a database **sequence**: every call gets a new number, 20, then 21, and so on, even when many calls arrive at once. It never goes back.

```callout jargon
A **sequence** is a counter kept by the database itself. Taking a number and moving the counter on happen as one step, so no two callers can ever get the same number.
```

`src/app/checkout/actions.js` has `placeOrder(prevState, formData)`. The checkout form sends `name`, `phone`, `itemId`, `qty` and a hidden `requestId`: a random id the form makes once, when it first appears. If the same form is sent twice, both sends have the same `requestId`.

```callout jargon
An action is **idempotent** when doing it twice has the same effect as doing it once. Pressing Place order twice for the same form should still make one order.
```

### Syntax: remembering work in progress

```js
const placing = new Map();

function placeOnce(requestId, start) {
  let pending = placing.get(requestId);
  if (!pending) {
    pending = start();
    placing.set(requestId, pending);
  }
  return pending;
}
```

The Map stores the *promise*, not the finished order. It's stored before anything is awaited, so a second call that arrives while the first is still working finds it and waits for the same order.

## The tiers

1. **Bronze** resolves the incident: two customers who order at the same moment get different order numbers, and both orders are saved.
2. **Silver**: a new order never takes the number of an order that exists, even after another order was deleted. Numbers only go up.
3. **Gold**: sending the same checkout form twice, at once or one after the other, makes one order, and both sends go to that order's page. Different customers ordering at the same moment still get their own orders.

## Recap

- One server runs many requests at once. Any `await` is a moment when another request can run.
- Reading a count, waiting, then writing an id built from it is a read-then-write race. Two requests can get the same id.
- Let the database hand out ids with a sequence. Counting rows also breaks when a row is deleted.
- Make order placing idempotent: a `requestId` from the form, and a Map from it to the promise of its order, stored before any `await`.

## Common mistakes

**1. Using the biggest id plus one.**

```js
const orders = await allOrders();
const biggest = Math.max(...orders.map((o) => Number(o.id.split("-")[1])));
const id = `order-${biggest + 1}`;
```

It survives a deleted order, so it looks like a fix. But it's still read-then-write: two requests both read order-19 as the biggest, and both make order-20. Bronze still fails.

Fix: take the number from the database's sequence, with one `await nextOrderNumber()` per order.

**2. Storing the order only after it's finished.**

```js
const done = new Map();
const order = done.get(requestId) ?? (await createOrder(details));
done.set(requestId, order);
```

A second tap one second later finds the order. But a double tap at the same moment doesn't: both look in the Map before either `createOrder` finishes, and both make an order.

Fix: store the promise in the Map before awaiting it.

**3. Disabling the button and stopping there.**

```jsx
<button type="submit" disabled={isPending}>Place order</button>
```

It's a good idea from topic 5, and it helps. But a form can still be sent twice, for example on a flaky network that retries, or before JavaScript has loaded. The server must protect itself.

Fix: keep the button, and make the action idempotent too.
