## Hook

At the stall, Meera Aunty counts the cash when a customer hands it over. Once it's in the box, nobody counts it again. The counting happens at one place: the counter, where money comes in.

Data from a server is the same. TypeScript checks your own code, but it can't see what a server will send tomorrow. So you count it once, at the door where it comes in. After that, the whole app can trust it.

## Concept

### Types stop at the edge of your code

`tsc` checks the code you write. It never sees the JSON a server sends while the app runs. So the `as` line here is a promise nobody checks:

```ts
async function loadItems(response: Response): Promise<Item[]> {
  const items = (await response.json()) as Item[];
  return items;
}
```

`response.json()` returns `any`. The `as Item[]` tells the compiler "trust me". If the server sends `"price_paise": "45000"` as text, every type still says number, and the app adds text to text.

```callout jargon
An **API contract** is the agreement between a server and its callers about what each request and answer looks like: names, types and status codes.
```

The contract can break: a backend deploy, an old app version, a proxy that returns an HTML error page. Your types only help if the data really matches them.

### The trust boundary

```callout jargon
A **trust boundary** is the place where data enters your app from outside: a server's answer, localStorage, a URL, a form. Inside the boundary, data has been checked. Outside it, data is `unknown`.
```

```animate
title: Checking data at the door
kind: scene
nodes:
  - { id: api, label: Delivery API, emoji: "🖥️", row: 0, col: 0 }
  - { id: door, label: parseDelivery, emoji: "🚪", value: waiting, row: 0, col: 2 }
  - { id: app, label: Checkout screen, emoji: "📱", value: "loading", row: 0, col: 4 }
steps:
  - say: The checkout asks the delivery API about PIN code 411007.
    send: { from: app, to: api, label: "GET /api/delivery?pin=411007" }
  - say: The answer arrives as JSON. Its type is unknown, because nothing has checked it yet.
    send: { from: api, to: door, label: '{"pin_code":"411007","days":2}' }
    set: { door: unknown }
  - say: The parser checks each field. pin_code is text and days is a number, so it builds a Delivery.
    set: { door: checked }
  - say: Only a checked Delivery goes in. The screen shows "Delivered in 2 days".
    send: { from: door, to: app, label: "Delivery" }
    set: { app: "2 days" }
  - say: After a bad deploy, days arrives as the text "2". The parser refuses it at the door.
    send: { from: api, to: door, label: '{"days":"2"}' }
    set: { door: "refused" }
  - say: The screen gets an error it must handle, instead of a wrong value that spreads through the app.
    send: { from: door, to: app, label: "error" }
    set: { app: "Try again" }
```

### Syntax: a parser for unknown data

```ts
function parseDelivery(data: unknown): Delivery | null {
  if (!isRecord(data)) return null;
  const { pin_code, days } = data;
  if (typeof pin_code !== "string" || typeof days !== "number") return null;
  return { pin: pin_code, days };
}
```

You met `unknown` and validation in [unknown and validating JSON](/t/ts-09-typescript-in-depth/topic-08-validating-json). The pattern is: take `unknown`, check every field you use, and return a real type or "no".

`isRecord` is a small type predicate: "a non-null object that isn't an array". After it, each field is `unknown` until its own check narrows it.

```callout tip
Whole numbers need more than typeof. Number.isInteger(45000.5) is false, and money in paise is always a whole number.
```

### Two shapes: the server's and the app's

The server often names things differently. Dhaaga's backend uses `snake_case`, like `pin_code`. The app uses `camelCase`, like `pin`.

```callout jargon
A **DTO** (data transfer object) is the shape data has on the wire, between server and app. The **domain type** is the shape your app works with.
```

Keep one type for each, and convert at the boundary:

```ts
interface ApiDelivery { pin_code: string; days: number }
interface Delivery { pin: string; days: number }
```

The parser converts on the way in. A small builder converts on the way out, when you send a request body. Then no screen ever sees `pin_code`, and a backend rename changes two functions, not fifty components.

### Syntax: a Result type

```ts
type Result<T> = { ok: true; value: T } | { ok: false; error: string };
```

A fetch can fail in many ways: no network, a 500 answer, JSON with the wrong shape. A function that returns `Result<T>` never throws. It hands back either the value or an error, and the `ok` field tells them apart.

It's a discriminated union, from [Discriminated unions](/t/ts-09-typescript-in-depth/topic-04-discriminated-unions). The caller can't reach `value` without checking `ok` first:

```ts
async function showDelivery(pin: string) {
  const result = await fetchDelivery(pin);
  if (result.ok) {
    showDays(result.value.days);
  } else {
    showError(result.error);
  }
}
```

Forgetting the error case is now a type error, not a blank screen.

### One contract, shared or generated

Writing the server's types by hand twice, once in the backend and once in the frontend, lets them drift apart. Teams avoid that in a few ways:

- **Shared types:** when both sides are TypeScript, one package holds the API types and both import it.
- **Generated types:** the backend publishes an **OpenAPI** file describing every endpoint. A tool such as `openapi-typescript` turns it into TypeScript types for the frontend.
- **Schema libraries:** a library such as **Zod** describes the shape once, and gives you both the runtime check and the type from it.

```callout jargon
**OpenAPI** is a standard file format that describes an HTTP API: its URLs, request bodies, answers and status codes. Tools read it to make docs, types and test servers.
```

Even with generated types, check at the boundary. Generated types describe what the server **should** send. The check proves what it **did** send.

## Worked example

Here is a helper Kavya wrote so every screen fetches the same safe way:

```ts
async function getJson<T>(url: string, parse: (data: unknown) => T | null): Promise<Result<T>> {
  try {
    const response = await fetch(url);
    if (!response.ok) return { ok: false, error: `HTTP ${response.status}` };
    const data: unknown = await response.json();
    const value = parse(data);
    if (value === null) return { ok: false, error: "Unexpected data from the server." };
    return { ok: true, value };
  } catch (error) {
    return { ok: false, error: error instanceof Error ? error.message : "The network failed." };
  }
}

async function checkPin() {
  const delivery = await getJson("/api/delivery?pin=411007", parseDelivery);
  return delivery;
}
```

Line by line:

- `getJson<T>` is generic. The parser decides `T`, so `delivery` is a `Result<Delivery>` with no `as` anywhere.
- `!response.ok` turns a 404 or 500 into an error. `fetch` itself only throws when the network fails.
- `const data: unknown` throws away the `any` that `json()` returns, so the compiler forces the check.
- The parser's `null` becomes an error the screen can show.
- `catch` covers a dead network and an answer that isn't JSON at all, such as an HTML error page.

## Try it

This parses three answers from the delivery API. Run it.

```ts run
type Delivery = { pin: string; days: number };

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function parseDelivery(data: unknown): Delivery | null {
  if (!isRecord(data)) return null;
  const { pin_code, days } = data;
  if (typeof pin_code !== "string" || typeof days !== "number") return null;
  return { pin: pin_code, days };
}

const answers: unknown[] = [
  JSON.parse('{"pin_code":"411007","days":2}'),
  JSON.parse('{"pin_code":"411007","days":"2"}'),
  JSON.parse("null"),
];
for (const answer of answers) {
  console.log(JSON.stringify(parseDelivery(answer)));
}
```

It prints:

```text
{"pin":"411007","days":2}
null
null
```

1. Add an answer with `"days": -3`. Should a delivery take minus three days? Make the parser refuse it.
2. Add a field `"courier": "Speedy"` to the first answer. Does it reach the `Delivery`? Why not?

## Recap

- Types check your code, not the data a server sends while the app runs. `response.json()` is `any`, and `as` only hides that.
- Check data once, at the trust boundary: take `unknown`, check every field, and return a real type or "no".
- Keep the server's shape (the DTO) and the app's shape apart, and convert at the boundary in both directions.
- Return a `Result<T>` from fetches, so every caller must handle the error case.
- Share or generate the contract's types (shared packages, OpenAPI, Zod), and still check at the boundary.

## Common mistakes

**1. Casting the JSON.**

```ts
const items = response.json() as Promise<Item[]>;
```

Symptom: the server sends a price as text, `"45000" + "12000"` joins into `"4500012000"`, and a cart total reads ₹4,50,00,120.00. Fix: `const data: unknown = await response.json();`, then a parser.

**2. Forgetting that fetch doesn't throw on 404 or 500.**

```ts
const data: Promise<unknown> = fetch(url).then((response) => response.json());
```

Symptom: a 500 answer's error JSON is parsed as if it were the item list. Fix: check `response.ok` first, and turn a bad status into an error.

**3. Passing the server's object straight through.**

```ts
const parseItem = (data: any): Item => data;
```

Symptom: screens start reading `price_paise` and `warehouse_note`, and a backend rename breaks fifty components. Fix: build a new app object with only the fields the app uses.

**4. Throwing from a fetch helper and hoping someone catches.**

```ts
if (!response.ok) throw new Error("HTTP " + response.status);
```

Symptom: one screen forgets the `try`, and the error crashes the whole page. Fix: return a `Result<T>`, so the type makes every caller handle the error.

**5. Trusting generated types without a check.**

```ts
const order = response.json() as Promise<components["schemas"]["Order"]>;
```

Symptom: the types match the OpenAPI file, but an old server still sends the old shape, and the app breaks. Fix: generated types describe what should arrive; a parser at the boundary proves what did.
