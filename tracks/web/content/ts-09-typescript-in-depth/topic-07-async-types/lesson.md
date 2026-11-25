## Hook

When a customer orders a tote bag on WhatsApp, Raju sends back a tracking slip. The slip isn't the bag. But it says what is coming: "1 tote bag, arriving Thursday".

Nobody tries to carry shopping in a tracking slip. They wait for the parcel, then open it.

In async JavaScript, a promise is the tracking slip. TypeScript writes what's coming on it: `Promise<number>` is a slip for a number. And it stops you from treating the slip as if it were the parcel.

## Concept

### Syntax: the Promise type

```ts
const stock: Promise<number> = Promise.resolve(3);
```

`Promise<T>` is a generic type. T is the type of the value the promise will be fulfilled with. A promise of a stock count is `Promise<number>`, and a promise that brings no value, like a timer, is `Promise<void>`.

```callout jargon
**Promise<T>** is the type of a promise that will be fulfilled with a value of type T. **void** means "no useful value", so `Promise<void>` only tells you when something finished.
```

### Syntax: typing an async function

```ts
async function stockOf(id: string): Promise<number> {
  return 3;
}
```

An `async` function always returns a promise, so its return type is always `Promise<...>`. Inside, you `return` the plain value, and TypeScript checks it against T.

Writing the plain type is an error:

```ts
async function stockOf(id: string): number {
  return 3;
}
```

```text
error TS1064: The return type of an async function or method must be the global Promise<T> type. Did you mean to write 'Promise<number>'?
```

### await unwraps the type

`await` on a `Promise<T>` gives you a T. TypeScript follows the types through every step.

```animate
title: From Promise<number> to number to a badge
kind: code
language: ts
code: |
  function wait(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
  async function stockOf(id: string): Promise<number> {
    await wait(5);
    return id === "DC-1042" ? 3 : 0;
  }
  async function badge(id: string): Promise<string> {
    const stock = await stockOf(id);
    return stock > 0 ? `${stock} in stock` : "Sold out";
  }
  badge("DC-1042").then((text) => console.log(text));
steps:
  - line: 12
    say: badge is called and hands back a Promise<string> straight away. then waits for the string inside it.
  - line: 9
    say: badge calls stockOf, which returns a Promise<number>. await pauses badge until it is fulfilled.
  - line: 5
    say: stockOf waits 5 ms for its pretend server. wait returns Promise<void>, so there is no value to keep.
  - line: 6
    say: The id is DC-1042, so stockOf returns 3. Its promise is fulfilled with that number.
  - line: 9
    vars: { stock: 3 }
    say: await unwraps Promise<number>, so stock is a plain number. Comparing it with 0 is allowed.
  - line: 10
    say: badge returns a string, which fulfils its Promise<string>.
  - line: 12
    out: "3 in stock"
    say: then receives the string and prints it.
```

### Forgetting await is caught

Without `await`, you're holding the tracking slip, not the parcel. TypeScript notices as soon as you use it like a number.

```ts
async function badge(id: string): Promise<string> {
  const stock = stockOf(id);
  return stock > 0 ? `${stock} in stock` : "Sold out";
}
```

```text
error TS2365: Operator '>' cannot be applied to types 'Promise<number>' and 'number'.
```

In plain JavaScript, this ran without a word. A promise is never greater than 0, so every item showed `Sold out`. And a badge that printed `${stock} in stock` straight away would show `[object Promise] in stock`. Fix: `const stock = await stockOf(id);`.

### Syntax: typing JSON from fetch

```ts
const reply = (await response.json()) as StockReply;
```

`response.json()` returns `Promise<any>`: TypeScript can't know what a server will send. So you say what you expect, with `as` or with a typed variable.

```ts
interface StockReply {
  id: string;
  stock: number;
}
async function fetchReply(id: string): Promise<StockReply> {
  const response = await fetch(`/api/stock/${id}`);
  const reply: StockReply = await response.json();
  return reply;
}
```

```callout warning
This is a promise you make to TypeScript, not a check. If the server sends `{ "stock": "3" }`, TypeScript still believes it's a number. The next ticket shows how to really check.
```

### Syntax: errors in catch are unknown

```ts
async function show(): Promise<void> {
  try {
    await fetchReply("DC-1042");
  } catch (error) {
    // error is unknown here
  }
}
```

Anything can be thrown: an `Error`, a string, even a number. So in strict mode, the `catch` variable has the type `unknown`, and you must narrow it before you read `.message`.

```ts
async function show(): Promise<void> {
  try {
    await fetchReply("DC-1042");
  } catch (error) {
    console.log(error.message);
  }
}
```

```text
error TS18046: 'error' is of type 'unknown'.
```

Fix: `error instanceof Error ? error.message : String(error)`.

### Promise.all keeps each type

`Promise.all` takes an array of promises and gives back one promise of all their values. TypeScript keeps each value's own type, in order.

```ts run
function later<T>(value: T): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(value), 5));
}

async function card(id: string): Promise<string> {
  const [stock, paise, name] = await Promise.all([
    later(3),
    later(45000),
    later("Golu the elephant"),
  ]);
  return `${name}: ₹${paise / 100}, ${stock} left`;
}

card("DC-1042").then((text) => console.log(text));
```

It prints `Golu the elephant: ₹450, 3 left`. TypeScript knows `stock` and `paise` are numbers and `name` is a string, so a mix-up like `name / 100` would be an error.

## Worked example

The item page asks for a price and a stock count together, and must show a message instead of breaking when either fails.

```ts run
function later<T>(value: T, fail = false): Promise<T> {
  return new Promise((resolve, reject) =>
    setTimeout(() => (fail ? reject(new Error("Stock server is busy")) : resolve(value)), 5),
  );
}

async function itemLine(id: string, serverDown: boolean): Promise<string> {
  try {
    const [paise, stock] = await Promise.all([later(45000), later(3, serverDown)]);
    return `${id}: ₹${paise / 100}, ${stock} in stock`;
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    return `${id}: price unknown (${message})`;
  }
}

itemLine("DC-1042", false)
  .then((line) => console.log(line))
  .then(() => itemLine("DC-1042", true))
  .then((line) => console.log(line));
```

Line by line:

1. `later<T>` is a pretend server: it fulfils with the value after 5 ms, or rejects with an `Error` when `fail` is true.
2. `itemLine` is `async` and returns `Promise<string>`, so every path must return a string.
3. `Promise.all` gives a pair of numbers, `paise` and `stock`, unpacked in one line.
4. If either promise rejects, `await` throws, and `catch` gets an `unknown`. Narrowing with `instanceof Error` makes `.message` safe.
5. It prints `DC-1042: ₹450, 3 in stock`, then `DC-1042: price unknown (Stock server is busy)`.

## Try it

Make `later` reject with a plain string, `reject("timeout")`, instead of an `Error`. Run it and see which side of the `instanceof` check the message comes from.

```ts run
async function double(paise: number): Promise<number> {
  return paise * 2;
}
double(45000).then((total) => console.log(total));
```

## Recap

- `Promise<T>` is a promise of a T; `Promise<void>` brings no value.
- An `async` function's return type is always `Promise<...>`, and you return the plain value inside it.
- `await` turns a `Promise<T>` into a T. A forgotten `await` shows up as a type error where you use the value.
- `response.json()` is `any`: give it a type with `as` or a typed variable, knowing it isn't checked.
- In `catch`, the error is `unknown`, so narrow it with `instanceof Error`. `Promise.all` keeps each value's own type.

## Common mistakes

**1. Giving an async function a plain return type.**

```ts
async function stockOf(id: string): number {
  return 3;
}
```

```text
error TS1064: The return type of an async function or method must be the global Promise<T> type. Did you mean to write 'Promise<number>'?
```

Fix: `Promise<number>`.

**2. Using a promise as if it were the value.**

```ts
async function total(id: string): Promise<number> {
  const stock = stockOf(id);
  return stock * 45000;
}
```

```text
error TS2362: The left-hand side of an arithmetic operation must be of type 'any', 'number', 'bigint' or an enum type.
```

`stock` is a `Promise<number>`, the slip and not the parcel. Fix: `const stock = await stockOf(id);`.

**3. Reading .message from the catch variable straight away.**

```ts
async function safeStock(id: string): Promise<string> {
  try {
    return String(await stockOf(id));
  } catch (error) {
    return error.message;
  }
}
```

```text
error TS18046: 'error' is of type 'unknown'.
```

A server library might throw a string or a plain object. Fix: `return error instanceof Error ? error.message : String(error);`.
