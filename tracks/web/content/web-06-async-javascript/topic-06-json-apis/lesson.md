## Hook

Orders used to come in on WhatsApp like this: "hi aunty 1 golu pls, pune, 411038, upi ok?" A person can read that. A program can't, because every customer writes it differently.

A form on the website fixes that. Every order arrives with the same fields, in the same order, with the same names.

An API works like that form. The website and the server agree on exact URLs and exact shapes of data, and both sides stick to them. In this ticket you learn to ask questions with extra details, and to send new data, not just read it.

## Concept

### Dhaaga's endpoints

Here is the part of Dhaaga's API that the website uses. Every body is JSON.

| Method and URL | What it does | Answer |
|---|---|---|
| `GET /api/catalogue` | Lists every item | `200` with a list of items |
| `GET /api/stock/DC-1042` | One item's stock | `200` with `{ "id": "DC-1042", "stock": 3 }` |
| `GET /api/delivery?pin=411001` | Can we deliver to this PIN? | `200` with `{ "deliverable": true, "days": 2 }` |
| `POST /api/orders` | Places a new order | `201` with `{ "id": "order-17" }` |

```callout jargon
**REST** is a common way to design an API. Each URL names a thing, such as `/api/orders`, and the method says what to do with it: `GET` to read it, `POST` to add a new one.
```

Dhaaga delivers inside Pune, where PIN codes start with `411`. Anywhere else, the delivery endpoint answers `{ "deliverable": false }`.

### Syntax: query strings with URLSearchParams

```js
const params = new URLSearchParams({ pin: "411001" });
const url = `/api/delivery?${params}`;
```

```callout jargon
A **query string** is the part of a URL after the `?`. It holds `name=value` pairs joined by `&`, such as `?q=golu&sort=price`. It adds details to a `GET` request.
```

Some characters, like spaces and `&`, would break a URL. `URLSearchParams` turns your values into safe text for you. This is called **encoding**.

```js run
const params = new URLSearchParams({ pin: "411001" });
console.log(`/api/delivery?${params}`);

const search = new URLSearchParams({ q: "rani pink & mint" });
console.log(`/api/catalogue?${search}`);
```

The space became `+`, and the `&` became `%26`. Now the server sees one search, not two broken pieces.

### Syntax: fetch with options

```js
const response = await fetch(url, { method, headers, body });
```

To do more than a `GET`, pass a second argument: an **options object**.

- `method`: `"POST"` to send new data. Leave it out for `GET`.
- `headers`: an object of extra labels.
- `body`: the data to send, **as text**.

```js
async function sendOrder(order) {
  const response = await fetch("/api/orders", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(order),
  });
  return response.status; // 201 when the order was created
}
```

### JSON in the body

The network carries text, not objects. So you turn the object into JSON text before you send it, and the server turns it back.

```animate
title: An order becomes text and back
kind: code
language: js
code: |
  const order = { itemId: "DC-1042", qty: 1, pin: "411038" };
  const body = JSON.stringify(order);
  console.log(body);
  console.log(typeof body);
  const received = JSON.parse(body);
  console.log(received.qty + 1);
steps:
  - line: 1
    say: The order starts as a normal JavaScript object.
  - line: 2
    say: JSON.stringify turns it into one string of JSON text, ready to travel.
  - line: 3
    out: '{"itemId":"DC-1042","qty":1,"pin":"411038"}'
    say: Notice the double quotes around every key. That is JSON's rule.
  - line: 4
    out: "string"
    say: It really is just text now. This is what goes in the request body.
  - line: 5
    say: The other side calls JSON.parse to turn the text back into an object.
  - line: 6
    out: "2"
    say: qty is a number again, so adding 1 gives 2. The object survived the trip.
```

### The Content-Type header

```callout jargon
The **Content-Type** header tells the other side what kind of data is in the body. `application/json` means "this body is JSON text, please parse it as JSON".
```

Without it, many servers treat your body as plain text and refuse the request. Always send it with a JSON body.

```animate
title: Placing order-17
kind: scene
nodes:
  - { id: page, label: Checkout page, emoji: "🛒", value: "Place order" }
  - { id: server, label: dhaaga.in API, emoji: "🖥️", value: idle }
  - { id: orders, label: Orders list, emoji: "📋", value: "16 orders" }
steps:
  - say: The customer presses Place order. The page turns the order into JSON text.
    focus: [page]
    set: { page: "sending..." }
  - say: It sends a POST with a Content-Type header, so the server knows the body is JSON.
    send: { from: page, to: server, label: "POST /api/orders (JSON)" }
    set: { server: "reading JSON" }
  - say: The server parses the body, checks the PIN, and saves the new order.
    send: { from: server, to: orders, label: "add order-17" }
    set: { orders: "17 orders" }
  - say: It answers 201 Created, with the new order's id in a JSON body.
    send: { from: server, to: page, label: "201 {id: order-17}", back: true }
    set: { server: idle }
  - say: The page reads the id and shows it to the customer.
    set: { page: "Order order-17 placed" }
```

### When the server says no

A good API explains what went wrong in the body, even for an error. Dhaaga's API answers a bad order with `400` and `{ "error": "PIN must be 6 digits" }`.

That message was written for people. Show it, instead of a vague "Something went wrong".

```js
async function sendOrder(order) {
  const response = await fetch("/api/orders", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(order),
  });
  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.error);
  }
  return data.id;
}
```

Here the body is read **before** checking `ok`, because both answers carry useful JSON.

## Worked example

The custom-order form sends a request for a special piece, such as an elephant in Indigo. The server answers `201` with `{ "id": "custom-4" }`.

```js
async function requestCustom(name, colour, phone) {
  const response = await fetch("/api/custom-orders", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, colour, phone }),
  });
  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.error);
  }
  return `Request ${data.id} received`;
}
```

Line by line:

1. `{ name, colour, phone }` is shorthand for `{ name: name, colour: colour, phone: phone }`.
2. The options say: this is a `POST`, its body is JSON, and here is the body as text.
3. `await response.json()` reads the answer, whether it is a success or an error.
4. If the status isn't ok, the server's own message becomes the error, such as `Phone number is missing`.
5. Otherwise the function returns `Request custom-4 received`.

## Try it

Run the `URLSearchParams` example again, but add a second field: `new URLSearchParams({ q: "golu", sort: "price" })`. Predict where the `&` goes before you press Run. Then try a value with a `?` in it, and see how it is encoded.

## Recap

- An API is a list of endpoints. Each has a method, a URL and an agreed JSON shape for its answers.
- Add details to a `GET` with a query string built by `URLSearchParams`, which encodes awkward characters.
- To send data, call `fetch(url, { method: "POST", headers, body })`.
- The body must be text: use `JSON.stringify`, and send `Content-Type: application/json`.
- Error answers often carry a JSON message, such as `{ "error": "PIN must be 6 digits" }`. Read it and show it.

## Common mistakes

**1. Sending the object without JSON.stringify.**

```js run
const order = { itemId: "DC-1042", qty: 1 };
console.log(String(order));
```

This prints `[object Object]`, and that is exactly what the server receives as the body if you write `body: order`. It answers `400`. Fix: `body: JSON.stringify(order)`.

**2. Forgetting the Content-Type header.**

```js
async function sendOrder(order) {
  return fetch("/api/orders", { method: "POST", body: JSON.stringify(order) });
}
```

The JSON text arrives, but it is labelled as plain text, so many servers won't parse it and refuse the order. Fix: add `headers: { "Content-Type": "application/json" }`.

**3. Gluing a query string together by hand.**

```js run
const q = "rani pink & mint";
console.log(`/api/catalogue?q=${q}`);
```

The `&` starts a new pair, so the server sees `q=rani pink ` and a strange extra field called ` mint`. Fix: `` `/api/catalogue?${new URLSearchParams({ q })}` ``.

**4. Throwing away the server's message.**

```js
async function sendOrder(order) {
  const response = await fetch("/api/orders", { method: "POST" });
  if (!response.ok) {
    throw new Error("Something went wrong");
  }
}
```

The customer never learns that their PIN has 5 digits. Fix: read `const data = await response.json();` and `throw new Error(data.error)`, so the real reason reaches the screen.
