## Hook

At the Aundh shop, each shelf has a small card. When the last tote bag goes, the card is flipped to "Sold out". When a new batch arrives, it is flipped back. Same shelf, different card, depending on what is there.

A React component can do the same. It looks at its props and state, and decides what to show.

## Concept

### Showing things depends on data

A component is a function, so it can use `if`, `? :` and `&&` like any function. React then draws whatever it returns. There is no special React syntax for "show this only when". It is plain JavaScript.

Watch one badge as the stock of tote bags goes down:

```animate
title: One badge, three looks
kind: scene
nodes:
  - { id: stock, label: "stock", emoji: "📦", value: "5" }
  - { id: badge, label: "StockBadge()", emoji: "🏷️" }
  - { id: page, label: "Page", emoji: "🖥️", value: "(nothing)" }
steps:
  - say: Five tote bags are in stock. The badge has nothing to warn about, so it returns null and the page shows no badge.
    focus: [stock, page]
  - say: Customers buy three. Stock is now 2, and the component re-renders.
    send: { from: stock, to: badge, label: "stock = 2" }
    set: { stock: "2" }
  - say: This time the function returns a warning badge.
    send: { from: badge, to: page, label: "Only 2 left" }
    set: { page: "Only 2 left" }
  - say: The last two go at the stall. Stock is 0, and the component re-renders again.
    send: { from: stock, to: badge, label: "stock = 0" }
    set: { stock: "0" }
  - say: Now the function returns a different badge, and React swaps the text on the page.
    send: { from: badge, to: page, label: "Sold out" }
    set: { page: "Sold out" }
```

### Syntax: an early return

```jsx
function DeliveryNote({ totalPaise }) {
  if (totalPaise === 0) {
    return null;
  }
  return <p className="delivery">Delivery inside Pune: ₹60.00</p>;
}
```

An `if` at the top of the component can return early. Everything below it runs only when the `if` did not return.

### Returning null shows nothing

`return null;` means "draw nothing here". The component still exists, but it puts nothing on the page. Use it when a component has nothing to say, like a delivery note for an empty cart.

```callout jargon
**Conditional rendering** means showing different things, or nothing, depending on a condition. In React, you do it with ordinary JavaScript: `if`, `? :` and `&&`.
```

### Syntax: the ternary in JSX

```jsx
{stock > 0 ? <button type="button">Add to cart</button> : <button type="button">Notify me</button>}
```

You cannot put an `if` inside curly braces, because `if` is a statement. The ternary operator, `condition ? a : b`, is an expression, so it fits. It picks one of two things to show.

```callout jargon
The **ternary operator** `? :` takes a condition and two values. It gives the first value when the condition is true, and the second when it is false.
```

### Syntax: && for "this or nothing"

```jsx
{giftWrap && <p className="gift-wrap">Gift wrap available</p>}
```

When there is no "else", use `&&`. If `giftWrap` is `true`, the result is the paragraph. If it is `false`, the result is `false`, and React draws nothing for `false`.

React skips `false`, `true`, `null` and `undefined`. It does draw numbers and text.

### The 0 trap

That last rule causes a famous bug. `&&` gives back the left side when it is falsy, and `0` is falsy. Watch:

```animate
title: What && really gives back
kind: code
language: js
code: |
  const stock = 0;
  const giftWrap = false;
  console.log(stock && "Only a few left");
  console.log(giftWrap && "Gift wrap available");
  console.log(stock > 0 && "Only a few left");
steps:
  - line: 1
    vars: { stock: 0 }
    say: Golu the elephant is sold out, so stock is 0.
  - line: 2
    vars: { giftWrap: false }
    say: And this item has no gift wrap.
  - line: 3
    out: "0"
    say: 0 is falsy, so && stops and gives back the left side. That is the number 0, and React draws numbers on the page.
  - line: 4
    out: "false"
    say: Here && gives back false. React draws nothing for false, so this one is safe.
  - line: 5
    out: "false"
    say: Compare first. stock > 0 is false, a real true-or-false value, so nothing appears. This is the fix.
```

So `{stock && <Badge />}` shows a lonely `0` when stock runs out. Always give `&&` a true-or-false value on its left, such as `stock > 0`.

### Syntax: choosing with a variable

```jsx
let action;
if (stock > 0) {
  action = <button type="button" onClick={onAdd}>Add to cart</button>;
} else {
  action = <button type="button" onClick={onNotify}>Notify me</button>;
}
return <article>{action}</article>;
```

JSX is a value, so you can store it in a variable. When the logic gets long, an `if` before the `return` can be easier to read than a ternary inside it.

### Conditions in attributes

```jsx
<button type="button" disabled={stock === 0}>Add to cart</button>
```

Conditions work in props too. `disabled` gets `true` or `false`, and React adds or removes the attribute.

## Worked example

The cart shows a note about delivery. Delivery inside Pune costs ₹60, and it is free above ₹999. An empty cart shows no note.

```jsx
export function DeliveryNote({ totalPaise }) {
  if (totalPaise === 0) {
    return null;
  }

  const free = totalPaise > 99900;

  return (
    <p className="delivery">
      {free ? "Free delivery inside Pune" : "Delivery inside Pune: ₹60.00"}
      {!free && <small> Free above ₹999.</small>}
    </p>
  );
}
```

Line by line:

- The early return handles the empty cart: the note shows nothing at all.
- `free` is a true-or-false value worked out once, so the JSX stays easy to read.
- The ternary picks one of two texts.
- `!free && ...` adds the small hint only when delivery is not free. `!free` is a real boolean, so it is safe with `&&`.

## Try it

The same decisions work in plain JavaScript. Run it, then add a total of `99901` to the list and predict its line.

```js run
function deliveryLabel(totalPaise) {
  if (totalPaise === 0) return null;
  return totalPaise > 99900 ? "Free delivery" : "Delivery ₹60.00";
}

for (const total of [0, 45000, 99900, 104900]) {
  console.log(total, deliveryLabel(total));
}
```

It prints `0 null`, `45000 Delivery ₹60.00`, `99900 Delivery ₹60.00` and `104900 Free delivery`. ₹999 exactly is not above ₹999, so it still pays for delivery.

## Recap

- Components decide what to show with ordinary JavaScript: `if`, `? :` and `&&`.
- An early `return null;` draws nothing.
- Inside JSX, use a ternary to pick one of two things, and `&&` to show something or nothing.
- React draws numbers and text, but skips `false`, `true`, `null` and `undefined`.
- Never put a number on the left of `&&`. Write `stock > 0 && ...`, not `stock && ...`.

## Common mistakes

**1. A number on the left of &&.**

```jsx
<article>
  <h3>{name}</h3>
  {stock && <p>In stock</p>}
</article>
```

When stock is 0, the card shows a lonely `0` under the name. `0 && ...` gives back `0`, and React draws numbers. Compare first: `{stock > 0 && <p>In stock</p>}`.

**2. Writing if inside the braces.**

```jsx
<article>
  {if (stock > 0) { <button type="button">Add to cart</button> }}
</article>
```

The build stops with a syntax error at `if`. Curly braces only take values. Use a ternary inside the braces, or an `if` before the `return`.

**3. Leaving out the second half of a ternary.**

```jsx
{stock > 0 ? <button type="button">Add to cart</button>}
```

The build stops with a syntax error at the closing brace, because a ternary needs both parts. Add `: null` to show nothing, or switch to `stock > 0 && ...`.
