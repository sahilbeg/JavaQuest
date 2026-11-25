## Hook

Two tea stalls stand side by side on FC Road. At the first, you sit down, then the waiter walks off to make your chai, and you wait at an empty table. At the second, the chai is already on the tray when the waiter reaches you.

In react-09, the browser got the page first and asked for the data afterwards. That's the first stall: an empty table, then "Loading...". In Next.js, the server can fetch the data before it sends the page. The page arrives with the chai already on it.

## Concept

### A server component can wait

In next-10 you met server components: components that run on the server and send finished HTML to the browser. They never run in the browser at all.

That gives them a superpower. A server component can be an `async` function. It can `await` data right in its body, before it returns its JSX.

```callout jargon
To **render on the server** means the server runs your components and builds the HTML. The browser receives a page that already has the text in it.
```

### Syntax: an async page

```jsx
export default async function Page() {
  const data = await getData();
  return <p>{data.message}</p>;
}
```

- `async` lets the component use `await`.
- The component waits for the data, then returns JSX that uses it.
- There is no `useEffect`, no `useState` and no loading flag. By the time the JSX is built, the data is there.

### What happens when a customer opens the page

```animate
title: The server fetches before it answers
kind: scene
nodes:
  - { id: phone, label: Customer's phone, emoji: "📱" }
  - { id: next, label: Next.js server, emoji: "🖥️", value: idle }
  - { id: api, label: Dhaaga API, emoji: "🗄️" }
steps:
  - say: A customer opens dhaaga.in/items. The phone asks the Next.js server for the page.
    send: { from: phone, to: next, label: "GET /items" }
  - say: The server starts running ItemsPage. It reaches await getItems() and asks the API.
    send: { from: next, to: api, label: "GET /items" }
    set: { next: waiting }
  - say: The API is close to the server, often in the same building, so the answer comes back fast.
    send: { from: api, to: next, label: "[Golu, Coaster set]", back: true }
  - say: Now ItemsPage has the items. It returns JSX, and Next.js turns it into HTML.
    set: { next: "HTML ready" }
  - say: The phone gets one answer with the whole list already in it. No Loading... at all.
    send: { from: next, to: phone, label: "HTML with 2 items", back: true }
    focus: [phone]
```

The server is usually much closer to the API than the customer's phone is. One fast trip on the server beats a slow trip from a phone on 3G.

### fetch works on the server too

`fetch` is not only a browser thing. Node has it, so server components use the same `fetch` you learned in web-06.

```jsx
export default async function ItemsPage() {
  const res = await fetch("https://api.dhaaga.in/items");
  const items = await res.json();
  return <p>{items.length} items</p>;
}
```

Search engines and link previews get the full list too, because the list is in the HTML.

### Syntax: checking the answer

```js
const res = await fetch("https://api.dhaaga.in/items");
if (!res.ok) throw new Error(`Items API answered ${res.status}`);
const items = await res.json();
```

This is the same rule as in web-06. `fetch` does not throw for a 404 or a 503. You check `res.ok` yourself.

On the server, you don't show the error yourself. You throw it. Next.js then shows the nearest `error.jsx` page, the one you built in next-10.

### Keep fetching in small functions

Put each request in its own function in `src/lib/`. The page just calls it.

```js
export async function getItems() {
  const res = await fetch("https://api.dhaaga.in/items");
  if (!res.ok) throw new Error(`Items API answered ${res.status}`);
  return res.json();
}
```

Now every page that needs items calls `getItems()`. The URL and the error check live in one place.

### One after another is slow

The catalogue needs two things: the items and the stock. If you `await` one, then the other, the second request only starts when the first one ends.

```callout jargon
A **waterfall** is a chain of requests where each one waits for the one before, even though it doesn't need its answer. The waits add up.
```

Short timers stand in for the network here:

```js run
function slow(name, ms) {
  console.log(`asking for ${name}`);
  return new Promise((resolve) =>
    setTimeout(() => {
      console.log(`got ${name}`);
      resolve(name);
    }, ms),
  );
}

async function oneAfterAnother() {
  await slow("items", 20);
  await slow("stock", 10);
}

async function together() {
  await Promise.all([slow("items", 20), slow("stock", 10)]);
}

console.log("-- one after another --");
oneAfterAnother().then(() => {
  console.log("-- together --");
  return together();
});
```

One after another takes 20 + 10 ms. Together takes only as long as the slowest one, 20 ms.

### Syntax: Promise.all for requests that don't need each other

```js
const [items, stock] = await Promise.all([getItems(), getStock()]);
```

Both functions are called straight away, so both requests leave at once. `Promise.all` waits for both and gives back their answers in the same order. If either one fails, the whole `await` throws, and the error page shows.

Use one `await` after another only when the second request needs the first answer, like fetching an item, then its maker.

### Server code can reach what the browser can't

A server component never ships its code to the browser. So it may talk straight to a database, or use a secret key. You'll do both later in this module: a data layer in topic 6 and secrets in topic 10.

```callout tip
A server component can pass its data to a client component as props. Keep those props plain data: strings, numbers, arrays and objects. A function or a database connection can't travel to the browser.
```

## Worked example

Kavya's catalogue page, split into a data file and a page.

```js
const API = "https://api.dhaaga.in";

export async function getItems() {
  const res = await fetch(`${API}/items`);
  if (!res.ok) throw new Error(`Items API answered ${res.status}`);
  return res.json();
}

export async function getStock() {
  const res = await fetch(`${API}/stock`);
  if (!res.ok) throw new Error(`Stock API answered ${res.status}`);
  return res.json();
}
```

```jsx
import { getItems, getStock } from "../../lib/api.js";
import { rupees } from "../../lib/money.js";

export default async function ItemsPage() {
  const [items, stock] = await Promise.all([getItems(), getStock()]);

  return (
    <main>
      <h1>Catalogue</h1>
      <ul>
        {items.map((item) => {
          const left = stock[item.id] ?? 0;
          const label = left === 0 ? "Sold out" : `${left} left`;
          return <li key={item.id}>{`${item.name}: ${rupees(item.paise)} (${label})`}</li>;
        })}
      </ul>
    </main>
  );
}
```

- The items API answers a list like `[{ "id": "DC-1042", "name": "Golu the elephant", "paise": 45000 }]`.
- The stock API answers an object of counts: `{ "DC-1042": 7, "DC-2001": 0 }`.
- `Promise.all` sends both requests at once.
- `?? 0` treats an item missing from the stock list as sold out.
- If either API fails, the function throws and the error page takes over.

## Try it

In the fix, write the page with `Promise.all` and run the checks. Then change it to two separate `await` lines and run them again. The check named "asks for the items and the stock at the same time" fails, because the stock request now waits for the items. Put `Promise.all` back.

## Recap

- A server component can be an `async` function that awaits its data before it returns JSX.
- The page arrives with the data in its HTML: no `useEffect`, no loading flag, and search engines see everything.
- `fetch` works on the server. Check `res.ok`, and throw when it's false so the error page shows.
- Start requests that don't need each other together, with `Promise.all`, to avoid a waterfall.
- Server code never reaches the browser, so it can use databases and secrets.

## Common mistakes

**1. Reaching for useEffect in a server component.**

```jsx
export default function ItemsPage() {
  const [items, setItems] = useState([]);
  useEffect(() => {
    getItems().then(setItems);
  }, []);
  return <p>{items.length} items</p>;
}
```

Next.js refuses to build it. The error says `useState` and `useEffect` only work in a Client Component, and suggests adding `"use client"`. Adding it "works", but then you're back to a page that arrives empty.

Fix: make the component `async` and write `const items = await getItems();`.

**2. Forgetting await.**

```jsx
const items = getItems();
return <ul>{items.map((item) => <li key={item.id}>{item.name}</li>)}</ul>;
```

The page crashes with `TypeError: items.map is not a function`. Without `await`, `items` is a promise, not a list.

Fix: `const items = await getItems();`, inside an `async` component.

**3. Trusting every answer.**

```js
export async function getItems() {
  const res = await fetch(`${API}/items`);
  return res.json();
}
```

When the API is down, it answers 503 with `{ "message": "down" }`. That object reaches the page, and `items.map is not a function` hides the real problem.

Fix: check `res.ok` before reading the JSON, and throw an error that names the status when it's false.

**4. A waterfall by accident.**

```jsx
const items = await getItems();
const stock = await getStock();
```

It works, but the stock request waits for the items for no reason. On a slow API, the page takes twice as long.

Fix: `const [items, stock] = await Promise.all([getItems(), getStock()]);`.
