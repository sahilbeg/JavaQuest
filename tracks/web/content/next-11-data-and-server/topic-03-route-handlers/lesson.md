## Hook

The Aundh shop has two doors. Customers come in at the front, where everything is laid out to look nice. Suppliers knock at the back, hand over a box and a slip, and leave with a signed receipt. Nobody shows the supplier the display shelves.

So far your Next.js app only had front doors: pages for people. Programs need a back door. Raju's WhatsApp bot and Arjun's restock scanner don't want HTML. They want a short, exact answer in JSON.

## Concept

### A route file answers requests

In next-10, a `page.jsx` file inside `app/` made a page. A `route.js` file in the same kind of folder makes an **API endpoint** instead.

```callout jargon
An **endpoint** is one address on a server that programs can send requests to, like `/api/stock`. A **route handler** is the function that answers requests to it.
```

The folder path is the URL, just like pages:

| File | URL |
|---|---|
| `src/app/api/stock/route.js` | `/api/stock` |
| `src/app/api/items/[id]/route.js` | `/api/items/DC-1042` |

A folder can have a `page.jsx` or a `route.js`, not both. Putting APIs under `api/` keeps them apart from the pages.

### Syntax: a route handler

```js
export async function GET(request) {
  return Response.json({ message: "hello" });
}
```

- The function's name is the HTTP method it answers: `GET`, `POST`, `PUT`, `PATCH` or `DELETE`.
- It receives a standard `Request`, the same kind of object `fetch` sends.
- It returns a standard `Response`. `Response.json(data)` turns the data into JSON text and sets the right header.

A method you don't export gets an automatic `405 Method Not Allowed`.

### A request reaches a handler

```animate
title: The WhatsApp bot asks about Golu
kind: scene
nodes:
  - { id: bot, label: WhatsApp bot, emoji: "🤖" }
  - { id: next, label: Next.js server, emoji: "🖥️" }
  - { id: get, label: "GET in api/stock/route.js", emoji: "📄" }
  - { id: stock, label: Stock data, emoji: "📦", value: "DC-1042: 7" }
steps:
  - say: A customer asks the bot about Golu. The bot sends a request to the site.
    send: { from: bot, to: next, label: "GET /api/stock?id=DC-1042" }
  - say: The path /api/stock matches the folder app/api/stock, and the method is GET. So Next.js calls the GET function.
    send: { from: next, to: get, label: "request" }
  - say: The handler reads id from the query string and looks up the stock.
    send: { from: get, to: stock, label: "DC-1042?" }
  - say: There are 7 left.
    send: { from: stock, to: get, label: "7", back: true }
  - say: The handler answers with JSON and status 200, and the bot tells the customer.
    send: { from: get, to: bot, label: "200 { id, left: 7 }", back: true }
```

### Syntax: reading the query string

```js
const id = new URL(request.url).searchParams.get("id");
```

`request.url` is the full address as text. `new URL(...)` breaks it into parts, and `searchParams.get` reads one value after the `?`. A missing value gives `null`.

Requests and responses are plain web objects, so you can try them in Node:

```js run
const request = new Request("http://localhost/api/stock?id=DC-1042");
console.log(request.method, new URL(request.url).searchParams.get("id"));

const res = Response.json({ error: "No item DC-9999." }, { status: 404 });
console.log(res.status, res.headers.get("content-type"));
res.json().then((body) => console.log(body.error));
```

### Syntax: answering with a status

```js
return Response.json({ error: "No item DC-9999." }, { status: 404 });
```

The second argument sets the status code. Programs read the status first, so pick the one that matches what happened:

| Status | Meaning | Use it when |
|---|---|---|
| 200 | OK | the answer is here |
| 201 | Created | a POST made something new |
| 400 | Bad Request | the caller sent something wrong |
| 404 | Not Found | the thing asked about doesn't exist |
| 500 | Server Error | your own code failed |

```callout tip
Always send an error message in the body too, like `{ "error": "Add ?id= with an item id." }`. The person debugging the bot at midnight will thank you.
```

### Syntax: reading a JSON body

```js
export async function POST(request) {
  const body = await request.json();
  return Response.json({ got: body });
}
```

`request.json()` reads the body and parses it. It's async, so you `await` it. If the body isn't valid JSON, it throws, so a careful handler wraps it:

```js
export async function POST(request) {
  let body;
  try {
    body = await request.json();
  } catch {
    return Response.json({ error: "The body must be JSON." }, { status: 400 });
  }
  return Response.json({ got: body });
}
```

### Never trust what arrives

Anyone on the internet can send anything to your endpoint, not just Arjun's scanner. Check every field before you use it.

```js run
function checkAdd(add) {
  return Number.isInteger(add) && add > 0;
}

console.log(checkAdd(5));
console.log(checkAdd("5"));
console.log(checkAdd(-3));
console.log(checkAdd(2.5));
```

Text `"5"`, negative numbers and fractions are all refused. Without this check, `left + "5"` would turn `12` into `"125"`.

### Syntax: a dynamic route handler

```js
export async function GET(request, { params }) {
  const { id } = await params;
  return Response.json({ id });
}
```

In a folder like `api/items/[id]`, the second argument carries the URL's values. Just like page params in next-10, `params` is a Promise in Next.js 16, so you `await` it. A request to `/api/items/DC-1042` gives `id` as `"DC-1042"`.

### NextRequest and NextResponse

Next.js also has its own versions in `next/server`. `NextRequest` adds handy parts like `request.nextUrl.searchParams` and `request.cookies`. `NextResponse` adds helpers for cookies and redirects.

They are extensions of the standard objects, so everything in this lesson works with either. You'll use `request.cookies` in topic 8.

### Route handler or server action?

Next topic, you'll meet server actions: functions your own forms call. Use a route handler when **another program** calls you: a bot, a scanner, a payment company's webhook, or a mobile app. Use a server action when your own pages change data.

## Worked example

The stock endpoint. `getLeft(id)` gives a number, or `undefined` for an unknown item.

```js
import { getLeft, addStock } from "../../../lib/stock.js";

export async function GET(request) {
  const id = new URL(request.url).searchParams.get("id");
  if (!id) return Response.json({ error: "Add ?id= with an item id." }, { status: 400 });

  const left = getLeft(id);
  if (left === undefined) return Response.json({ error: `No item ${id}.` }, { status: 404 });

  return Response.json({ id, left });
}

export async function POST(request) {
  let body;
  try {
    body = await request.json();
  } catch {
    return Response.json({ error: "The body must be JSON." }, { status: 400 });
  }
  const { id, add } = body;
  if (!Number.isInteger(add) || add <= 0) {
    return Response.json({ error: "add must be a whole number above 0." }, { status: 400 });
  }
  if (getLeft(id) === undefined) return Response.json({ error: `No item ${id}.` }, { status: 404 });

  return Response.json({ id, left: addStock(id, add) });
}
```

- Each problem returns early with its own status and message. The happy path is at the bottom.
- `left === undefined` is checked, not `!left`, because 0 left is a real answer.
- The POST checks its input before it changes anything.

## Try it

After the checks pass, try a request in your head. What does `GET /api/stock?id=DC-2001` answer? The coaster set has 0 left. Now change `left === undefined` to `!left` and run the checks again: one fails, because a sold-out item now looks like a missing one. Change it back.

## Recap

- A `route.js` file in `app/` makes an endpoint. Its URL comes from the folders, like a page's.
- Export one function per method, `GET`, `POST` and so on. Each gets a `Request` and returns a `Response`.
- Read the query with `new URL(request.url).searchParams`, and a JSON body with `await request.json()` inside `try`.
- Answer with `Response.json(body, { status })`, and pick the status that matches: 200, 201, 400, 404.
- In a dynamic route handler, `const { id } = await params;` reads the URL's values.

## Common mistakes

**1. A default export instead of a named method.**

```js
export default async function handler(request) {
  return Response.json({ ok: true });
}
```

Every request gets `405 Method Not Allowed`, because Next.js looks for a function named after the method, like `GET`.

Fix: `export async function GET(request) { ... }`.

**2. Status 200 for every answer.**

```js
export async function GET(request) {
  const id = new URL(request.url).searchParams.get("id");
  const left = getLeft(id);
  if (left === undefined) return Response.json({ error: "No item." });
  return Response.json({ id, left });
}
```

The bot sees 200, thinks it worked, and tells the customer "undefined left".

Fix: pass `{ status: 404 }` as the second argument of `Response.json`. The status must match the problem.

**3. Treating 0 as missing.**

```js
export async function GET(request) {
  const id = new URL(request.url).searchParams.get("id");
  const left = getLeft(id);
  if (!left) return Response.json({ error: `No item ${id}.` }, { status: 404 });
  return Response.json({ id, left });
}
```

The coaster set has 0 left, so the bot says the coaster set doesn't exist, instead of "sold out".

Fix: `if (left === undefined)`.

**4. Reading params without await.**

```js
export async function GET(request, { params }) {
  const item = findItem(params.id);
}
```

`params` is a Promise in Next.js 16, so `params.id` is `undefined` and every item is "not found".

Fix: `const { id } = await params;`.
