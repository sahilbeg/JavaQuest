## Hook

At Dhaaga's stall, Meera Aunty prepares the price board at home the night before. It's finished before the stall opens, and nobody needs to change it during the day. The cash box is different: it has to be right there at the counter, because customers use it all day.

A Next.js page works the same way. Most of it, like the list of items and prices, can be prepared on the server and sent finished. Only the parts customers press, like an Add to cart button, need to be in the browser.

## Concept

### Two kinds of components

In Next.js, every component is one of two kinds:

```callout jargon
A **server component** runs only on the server. Its HTML is sent to the browser, but its code never is. A **client component** also runs in the browser, so it can have state and react to clicks. In the app router, every component is a server component unless you say otherwise.
```

| | Server component | Client component |
|---|---|---|
| Runs | on the server only | on the server for the first HTML, then in the browser |
| Can be `async` and `await` data | yes | no |
| Can read the database or secret keys | yes | no |
| `useState`, `useEffect`, `onClick` | no | yes |
| `window`, `localStorage` | no | yes, in effects and handlers |
| Its code is downloaded by the phone | no | yes |

### Syntax: an async server component

```jsx
export default async function HomePage() {
  const items = await getItems();
  return <p>{items.length} pieces in the shop</p>;
}
```

A server component can be an `async` function. It can `await` data right in its body, with no effect and no loading state to manage. In react-09 you fetched in a `useEffect` after the first render. Here the data is ready before the HTML is even sent.

Because this code never reaches the browser, it may read the database or use a secret key. Customers can't see it.

### Syntax: "use client"

```jsx
"use client";
import { useState } from "react";

export function LikeButton() {
  const [likes, setLikes] = useState(0);
  return <button onClick={() => setLikes(likes + 1)}>♥ {likes}</button>;
}
```

The line `"use client";` at the very top of a file makes its components client components. It must come before any `import`. Comments may come before it, but no code.

```callout jargon
A **directive** is a line of plain text in quotes, like `"use client";`, that gives an instruction to the tools rather than doing something itself. It's written as a string, so the file is still ordinary JavaScript.
```

### The boundary

`"use client"` marks a **boundary**. The file with the line, and every component it imports, become part of the JavaScript sent to the browser. Everything above the boundary stays on the server.

```animate
title: Building the home page
kind: scene
nodes:
  - { id: db, label: Catalogue, emoji: "🗄️" }
  - { id: page, label: HomePage (server), emoji: "🖥️" }
  - { id: button, label: AddToCartButton (client), emoji: "🛒", value: "count 0" }
  - { id: phone, label: Phone, emoji: "📱" }
steps:
  - say: A phone asks for the home page. HomePage is a server component, so it runs on the server.
    send: { from: phone, to: page, label: "GET /" }
  - say: It awaits the catalogue. The database and its password never leave the server.
    send: { from: page, to: db, label: "getItems()" }
  - say: Six items come back. The page draws a list, with an AddToCartButton in each row.
    send: { from: db, to: page, label: "6 items", back: true }
  - say: The server sends the HTML, plus the JavaScript for the button only. HomePage's own code stays behind.
    send: { from: page, to: phone, label: "HTML + button JS", back: true }
  - say: In the browser, hydration wakes up the buttons. The list itself needs no JavaScript at all.
    focus: [button]
  - say: The customer taps Add to cart. The button's state changes right there in the browser, with no trip to the server.
    set: { button: "count 1" }
```

So a page can be mostly server, with small client pieces where people press things. That keeps the JavaScript the phone downloads small.

### Props across the boundary

A server component passes props to a client component like any parent does. But those props must travel from the server to the browser, so they must be data that can be written down.

```callout jargon
**Serializable** data can be turned into text and back: strings, numbers, booleans, `null`, arrays and plain objects. A function can't: it's code, and code doesn't travel this way.
```

```jsx
<AddToCartButton itemId="DC-1042" name="Golu the elephant" />
```

This is fine: two strings. Passing `onAdd={() => ...}` from a server component would fail, because a function can't be sent. The client component should hold its own handlers.

### Which kind should it be?

Start every component as a server component. Make it a client component only when it needs one of these:

- state (`useState`, `useReducer`),
- effects (`useEffect`),
- event handlers (`onClick`, `onChange`, `onSubmit`),
- browser-only things (`window`, `localStorage`),
- hooks from `next/navigation` like `usePathname`.

And push `"use client"` as far down as you can: make the button a client component, not the whole page.

## Worked example

A page that shows Golu's reviews, with a client "helpful" button on each:

```jsx
// src/app/reviews/page.jsx (server)
import { getReviews } from "../../lib/reviews.js";
import { HelpfulButton } from "./HelpfulButton.jsx";

export default async function ReviewsPage() {
  const reviews = await getReviews("DC-1042");
  return (
    <ul>
      {reviews.map((review) => (
        <li key={review.id}>
          <p>{review.text}</p>
          <HelpfulButton reviewId={review.id} />
        </li>
      ))}
    </ul>
  );
}
```

```jsx
// src/app/reviews/HelpfulButton.jsx (client)
"use client";
import { useState } from "react";

export function HelpfulButton({ reviewId }) {
  const [pressed, setPressed] = useState(false);
  return (
    <button type="button" disabled={pressed} onClick={() => setPressed(true)}>
      {pressed ? "Thanks!" : "Helpful"}
    </button>
  );
}
```

- The page is `async` and awaits the reviews. No `useEffect`, no loading flag.
- The page passes only a string, `reviewId`, across the boundary.
- `HelpfulButton.jsx` starts with `"use client"` because it has state and a click handler.
- The list and the review text are plain HTML. Only the button's code goes to the browser.

## Try it

In this ticket's code, once your checks pass, move `"use client";` from the button into `page.jsx`. The checks fail: a page with `"use client"` can no longer be `async`, and it would send the whole page's code to every phone. Put it back.

## Recap

- Components are server components by default. They run on the server, can be `async`, can read data directly, and send no code to the browser.
- `"use client"` at the top of a file makes client components: they can use state, effects, event handlers and browser APIs.
- The directive marks a boundary: that file and everything it imports go to the browser.
- Props from a server component to a client component must be serializable data, never functions.
- Keep pages on the server and make only the small interactive pieces client components.

## Common mistakes

**1. Using state in a server component.**

```jsx
export default function HomePage() {
  const [count, setCount] = useState(0);
  return <button onClick={() => setCount(count + 1)}>{count}</button>;
}
```

Next.js stops with an error saying `useState` only works in a client component, and suggests adding `"use client"`.

Fix: move the button into its own file that starts with `"use client"`, and render it from the page.

**2. Marking the whole page as a client component.**

```jsx
"use client";
export default async function HomePage() {
  const items = await getItems();
  // ...
}
```

Client components can't be `async`, so React refuses to render it. Even without `async`, every line of the page would be sent to the phone.

Fix: keep the page on the server. Put `"use client"` only in the small component that needs it.

**3. Passing a function from the server.**

```jsx
<AddToCartButton itemId={item.id} onAdd={() => addToCart(item.id)} />
```

Next.js stops with an error saying event handlers cannot be passed to client component props.

Fix: pass data, like `itemId`, and let the client component handle its own click.

**4. Putting "use client" after the imports.**

```jsx
import { useState } from "react";
"use client";
```

The line is no longer the first statement, so it doesn't count as a directive. Next.js stops with an error saying the `"use client"` directive must be placed before other expressions.

Fix: `"use client";` goes on the first line, above every import.
