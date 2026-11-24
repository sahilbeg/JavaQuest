## Hook

When the FC Road stall runs low, someone phones the store room. "How many Golus are left?" The person in the store room counts the shelf and says "Three."

The call has a clear shape. You say who you want and what you're asking. They answer with a result, or with "Sorry, wrong number."

A web page asks a server for data in exactly this way. The phone call is called **HTTP**, and the phone is a function called `fetch`.

## Concept

### Clients and servers

```callout jargon
A **client** is the program that asks, like the browser showing dhaaga.in. A **server** is the program that answers, running on a computer somewhere else. An **API** is the set of questions a server agrees to answer, and the shape of its answers.
```

Dhaaga's server knows the real stock, because the shop and the stall update it. The website is a client. It must ask the server every time it needs a number, instead of typing it into the HTML.

### What a request holds

```callout jargon
**HTTP** is the set of rules that clients and servers use to talk on the web. Every conversation is one **request** from the client, then one **response** from the server.
```

A request has up to four parts:

- A **method**: what you want to do. `GET` reads data. `POST` sends new data.
- A **URL**: which thing you are asking about, such as `/api/stock/DC-1042`.
- **Headers**: short labels with extra facts, like what type of data is in the body.
- A **body**: data you send. A `GET` has no body.

```callout jargon
An **endpoint** is one URL on an API that answers one kind of question. `GET /api/stock/DC-1042` is Dhaaga's stock endpoint for Golu.
```

### What a response holds

A response has three parts:

- A **status code**: a number that says how it went.
- **Headers**: labels such as `Content-Type: application/json`.
- A **body**: the data, often JSON. For the stock endpoint, it is `{ "id": "DC-1042", "stock": 3 }`.

```animate
title: The item page asks for Golu's stock
kind: scene
nodes:
  - { id: phone, label: Customer's phone, emoji: "📱", value: "Golu's page" }
  - { id: server, label: dhaaga.in server, emoji: "🖥️", value: idle }
  - { id: db, label: Stock records, emoji: "🗄️", value: "DC-1042: 3" }
steps:
  - say: The customer opens Golu's page. The page needs the stock number, so it sends a request.
    send: { from: phone, to: server, label: "GET /api/stock/DC-1042" }
    set: { server: busy }
  - say: The server looks up the item in its stock records.
    send: { from: server, to: db, label: "DC-1042?" }
  - say: The records answer with the count.
    send: { from: db, to: server, label: "3", back: true }
  - say: The server sends a response. The status says it worked, and the body holds the data as JSON.
    send: { from: server, to: phone, label: "200 OK {stock: 3}", back: true }
    set: { server: idle }
  - say: The page reads the body and shows "3 in stock". The whole trip took a moment, and the page never froze.
    set: { phone: "3 in stock" }
```

### Status codes

The first digit tells you the family:

| Code | Name | Meaning |
|---|---|---|
| 200 | OK | It worked, and here is the data. |
| 201 | Created | It worked, and a new thing was made, such as an order. |
| 400 | Bad Request | Your request was wrong, such as a 5-digit PIN. |
| 404 | Not Found | There is nothing at that URL, such as an unknown item id. |
| 500 | Internal Server Error | The server broke while answering. |

```callout tip
2xx means success. 4xx means the client asked wrongly. 5xx means the server failed. You only need to remember the families.
```

### Syntax: fetch

```js
const response = await fetch(url);
```

`fetch` sends a request and returns a promise of a **response object**. With no other options, it sends a `GET`.

```js
async function loadGoluStock() {
  const response = await fetch("/api/stock/DC-1042");
  console.log(response.status); // 200
}
```

A URL that starts with `/` goes to the same site as the page. On dhaaga.in, `/api/stock/DC-1042` means `https://dhaaga.in/api/stock/DC-1042`.

### Syntax: response.json

```js
const data = await response.json();
```

The response arrives in two stages. First the status and headers, then the body. So reading the body is a second wait: `response.json()` returns a promise too. It reads the body and turns the JSON text into a JavaScript object.

```js
async function goluStock() {
  const response = await fetch("/api/stock/DC-1042");
  const data = await response.json();
  return data.stock; // 3
}
```

You can read the body only once. Keep `data` in a variable and use that.

### fetch only fails when the network does

Here is the surprise. A `404` or a `500` is still an answer, so `fetch` does **not** reject. Its promise rejects only when no answer came back at all: no signal, a wrong address, or a blocked request.

So you check the answer yourself. `response.ok` is `true` for any status from 200 to 299, and `false` otherwise.

```js
async function goluStock() {
  const response = await fetch("/api/stock/DC-1042");
  if (!response.ok) {
    throw new Error(`Stock check failed: ${response.status}`);
  }
  const data = await response.json();
  return data.stock;
}
```

This pretend `fetch` gives back the same shape as the real one, so you can run it here:

```js run
async function pretendFetch(url) {
  if (url === "/api/stock/DC-1042") {
    return { ok: true, status: 200, json: async () => ({ id: "DC-1042", stock: 3 }) };
  }
  return { ok: false, status: 404, json: async () => ({ error: "No such item" }) };
}

async function show(url) {
  const response = await pretendFetch(url);
  console.log(url, response.status, response.ok);
}

async function main() {
  await show("/api/stock/DC-1042");
  await show("/api/stock/DC-9999");
}

main();
```

The unknown item still gives a response. It is just not `ok`.

### Watching requests in DevTools

Open DevTools (F12, or right-click and Inspect) and choose the **Network** tab. Reload the page. Every request appears as a row, with its method, URL, status and time. Click one to see its headers and its body under **Response**.

```callout tip
When stock looks wrong, look at the Network tab first. It tells you whether the page asked the wrong question or the server gave the wrong answer.
```

## Worked example

The stall's tablet shows one line per item. Here is the function behind it, written for a real browser:

```js
async function stockLine(id) {
  const response = await fetch(`/api/stock/${id}`);
  if (!response.ok) {
    throw new Error(`Stock check failed: ${response.status}`);
  }
  const data = await response.json();
  return `${data.id}: ${data.stock} left`;
}
```

Line by line:

1. The template literal builds the URL for this item, such as `/api/stock/DC-1042`.
2. `await fetch(...)` sends a `GET` and waits for the status and headers.
3. If the status isn't in the 200s, the function throws. The caller's `catch` can show a message.
4. `await response.json()` waits for the body and turns it into `{ id: "DC-1042", stock: 3 }`.
5. The function returns `DC-1042: 3 left`. Because it is `async`, the caller gets a promise of that text.

## Try it

In the runnable example above, add a third line to `main`: `await show("/api/stock/DC-1042/");`, with a slash at the end. Predict the result before you run it. A tiny change in the URL is a different endpoint, and that is a common cause of a 404.

## Recap

- A client sends an HTTP request (method, URL, headers, body), and the server sends a response (status code, headers, body).
- 2xx means success, 4xx means the client asked wrongly, and 5xx means the server failed.
- `await fetch(url)` gives you a response object. `await response.json()` turns its body into data.
- `fetch` only rejects when no answer arrives. Check `response.ok` yourself, and throw for a 404 or 500.
- The Network tab in DevTools shows every request and its answer.

## Common mistakes

**1. Forgetting to await response.json().**

```js
async function goluStock() {
  const response = await fetch("/api/stock/DC-1042");
  const data = response.json();
  return data.stock;
}
```

The page shows `undefined in stock`. `data` is a promise, not the object. Fix: `const data = await response.json();`.

**2. Not checking response.ok.**

```js
async function stockFor(id) {
  const response = await fetch(`/api/stock/${id}`);
  const data = await response.json();
  return data.stock;
}
```

For an unknown id, the server answers `404` with `{ "error": "No such item" }`. There is no `stock` in that, so the page shows `undefined in stock` instead of an error. Fix: `if (!response.ok) throw new Error(...)` before reading the body.

**3. Expecting fetch to throw for a 404.**

```js
async function stockFor(id) {
  try {
    const response = await fetch(`/api/stock/${id}`);
    return (await response.json()).stock;
  } catch (error) {
    return "Item not found";
  }
}
```

The `catch` never runs for a 404, because `fetch` succeeded: it got an answer. Fix: check `response.ok` and throw yourself, so the `catch` gets the failure.

**4. Typing the stock into the HTML.**

```html
<p class="stock">In stock</p>
```

This is the bug in the ticket. The text never changes, however many are sold. Fix: leave the element empty, fetch the number when the page loads, and fill it in.
