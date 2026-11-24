## Hook

Before a shop opens on a festival day, the owner walks in through the front door like a customer. She picks up a coaster set, reads the price tag, walks to the counter and pays. She doesn't check how the shelf was screwed together. She checks what a customer will meet.

Good component tests work the same way. They don't peek inside a component at its state or its functions. They look at the screen, click what a customer clicks, and check what the customer would see. Then a test only breaks when a customer would notice something wrong.

## Concept

### Why test like a user

You met Vitest in [Writing tests with Vitest](/t/web-07-tooling/topic-07-unit-tests). There you tested plain functions: put numbers in, check the number that comes out. A React component is different. Its "output" is a piece of page, and its "input" is clicks and typing.

```callout jargon
**React Testing Library** (RTL for short) is a small library for testing components. It draws a component into a pretend page and gives you ways to find things on it the way a person would: by role, label and text.
```

RTL has one guiding rule: the more your tests look like the way your software is used, the more you can trust them. So a good test:

- finds the **Add to cart** button by its words, not by a CSS class;
- clicks it, like a finger or a mouse would;
- checks what changed on screen, or what the card told its parent.

A bad test reads `useState` values or calls the click handler directly. It passes while the real button is broken, and it breaks when you rename a variable that no customer ever sees.

### How a component test runs

```callout jargon
**jsdom** is a pretend browser written in JavaScript. It has a `document`, elements and events, but no screen. Tests run in it, so they don't need a real browser and they run in milliseconds.
```

```animate
title: A test clicks Add to cart
kind: scene
nodes:
  - { id: test, label: The test, emoji: "🧪", row: 0, col: 0 }
  - { id: dom, label: jsdom page, emoji: "📄", value: empty, row: 0, col: 2 }
  - { id: card, label: ProductCard, emoji: "🧸", row: 0, col: 4 }
  - { id: spy, label: onAdd spy, emoji: "🕵️", value: "0 calls", row: 2, col: 4 }
steps:
  - say: render() draws the card into jsdom, the pretend page. No real browser opens.
    send: { from: test, to: dom, label: "render(<ProductCard/>)" }
    set: { dom: "card drawn" }
  - say: The test finds the button the way a customer would, by its role and its words.
    send: { from: test, to: dom, label: "getByRole button" }
  - say: userEvent clicks it. The same events fire as for a real click, in the same order.
    send: { from: test, to: card, label: "click" }
  - say: The card calls its onAdd prop. In the test, onAdd is a spy that remembers every call.
    send: { from: card, to: spy, label: "onAdd('DC-1042', 1)" }
    set: { spy: "1 call" }
  - say: The test checks what the spy saw. One call, with the right id and quantity, means the button works.
    send: { from: test, to: spy, label: "expect calls" }
    focus: [spy]
```

### Syntax: render and screen

```jsx
render(<ProductCard product={golu} onAdd={onAdd} />);
const price = screen.queryByText("₹450.00");
```

`render` draws a component into jsdom's `document.body`. `screen` is your view of the whole page: every query starts from it.

After each test, call `cleanup` to remove what you drew, so the next test starts with an empty page. With Vitest's globals turned off, you write it once at the top of a test file: `afterEach(cleanup);`.

### Picking a query: role first

```callout jargon
An element's **role** is what it is to assistive technology: a `button`, a `link`, a `heading`, a `textbox`, a `checkbox`. Its **accessible name** is what a screen reader reads out for it, usually its text or its label.
```

RTL gives many ways to find an element. Use them in this order:

1. **`ByRole`** with a name: `getByRole("button", { name: "Add to cart" })`. This is how screen reader users find things, so it also tests that your page is accessible.
2. **`ByLabelText`**: form fields by their `<label>`, such as `getByLabelText("Quantity")`.
3. **`ByText`**: words that aren't a control, such as a price or "Out of stock".
4. **`ByTestId`**: a `data-testid` attribute, only when nothing else works. No customer can see it.

A query by role or label has a bonus: if someone deletes the label, the test fails. The bug a blind customer would hit becomes a red test.

### Syntax: getBy, queryBy and findBy

```jsx
screen.getByRole("button", { name: "Add to cart" });   // throws if missing
screen.queryByText("Out of stock");                    // null if missing
await screen.findByText("Added to cart");              // waits, then throws if still missing
```

Every query comes in three kinds, and each fits a different job:

| Kind | When it's missing | Use it for |
|---|---|---|
| `getBy...` | throws an error | something that must be there right now |
| `queryBy...` | returns `null` | checking that something is **not** there |
| `findBy...` | waits up to a second, then throws | something that appears later, like data from a `fetch` |

Each one also has an `All` form, like `queryAllByRole("heading")`, that returns a list.

```callout tip
For a friendly failure message, use queryBy and give expect a message: expect(screen.queryByText("₹450.00"), "Golu should cost ₹450.00").not.toBeNull(). A getBy miss throws a long error dump instead.
```

### Syntax: userEvent

```jsx
const user = userEvent.setup();
await user.click(button);
await user.type(input, "Golu");
await user.selectOptions(select, "3");
await user.keyboard("{Enter}");
await user.tab();
```

```callout jargon
**user-event** is RTL's partner library. It acts like a person: one click fires pointer down, mouse down, pointer up, mouse up and click, just as a browser does.
```

Make one `user` at the start of each test, and `await` every action: each one returns a promise. An older helper, `fireEvent`, fires one single event. It can click a disabled button that no person could click, so prefer `userEvent`.

### Checking callbacks with vi.fn

Many components don't change what they show. They tell their parent, through a callback prop like `onAdd`. To test that, pass a **spy**.

```callout jargon
A **spy** (also called a mock function) is a fake function that remembers every call. In Vitest you make one with `vi.fn()`. Its `mock.calls` is a list with one entry per call, each holding that call's arguments.
```

```jsx
const onAdd = vi.fn();
render(<ProductCard product={golu} onAdd={onAdd} />);
await user.click(screen.getByRole("button", { name: "Add to cart" }));
expect(onAdd.mock.calls, "one click should add one Golu").toEqual([["DC-1042", 1]]);
```

Compare the **whole** list with `toEqual`. Then a second call, a missing call or a wrong argument all fail. Checking only `onAdd.mock.calls.length > 0` would miss most bugs.

### What not to test

Leave out anything a customer can't see or do:

- **State values.** A customer never sees `quantity === 3`. They see a select showing 3, and a cart with 3 Golus.
- **Class names and tags.** Swapping a `<div>` for a `<section>` shouldn't break a test.
- **Child components' insides.** Test `ProductCard` through its own screen and props.

Tests written this way survive refactors. You can rewrite the whole component, and if a customer sees the same thing, every test stays green.

### Would it fail if the code were wrong?

A test that never fails is decoration. The real question for every test is: if someone broke this, would the test notice?

One way to answer it is to break the code on purpose, one bug at a time, and see whether a test goes red.

```callout jargon
A **mutant** is a copy of your code with one small bug added on purpose, like `>` turned into `>=`. **Mutation testing** runs your tests against many mutants. A mutant that no test catches shows a hole in your tests.
```

```animate
title: One check meets two broken copies
kind: code
language: js
code: |
  const real = (paise) => "₹" + (paise / 100).toFixed(2);
  const broken = (paise) => "₹" + paise.toFixed(2);
  const check = (format) => format(45000) === "₹450.00";
  console.log(check(real));
  console.log(check(broken));
steps:
  - line: 1
    say: The real formatter divides paise by 100 before it shows rupees.
  - line: 2
    say: A broken copy forgets to divide. It would show Golu at ₹45000.00.
  - line: 3
    say: The check asks one question. Does Golu's price come out as ₹450.00?
  - line: 4
    out: "true"
    say: On the real code, the check passes. That's the first rule. A check must pass on working code.
  - line: 5
    out: "false"
    say: On the broken copy, it fails. The check caught the bug, so it earns its place.
```

## Worked example

Here is a full test file for a tiny piece of Dhaaga's cart: a button that shows how many items are in the cart.

```jsx
import { useState } from "react";

export function CartButton({ onOpen }) {
  const [count, setCount] = useState(0);
  return (
    <div>
      <button type="button" onClick={() => setCount(count + 1)}>Add a keychain</button>
      <button type="button" onClick={onOpen}>Cart ({count})</button>
    </div>
  );
}
```

```jsx
import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CartButton } from "../src/CartButton.jsx";

afterEach(cleanup);

describe("CartButton", () => {
  it("counts each keychain added", async () => {
    const user = userEvent.setup();
    render(<CartButton onOpen={vi.fn()} />);
    await user.click(screen.getByRole("button", { name: "Add a keychain" }));
    await user.click(screen.getByRole("button", { name: "Add a keychain" }));
    expect(screen.queryByRole("button", { name: "Cart (2)" }), "two clicks should show Cart (2)").not.toBeNull();
  });

  it("opens the cart once per click", async () => {
    const user = userEvent.setup();
    const onOpen = vi.fn();
    render(<CartButton onOpen={onOpen} />);
    await user.click(screen.getByRole("button", { name: "Cart (0)" }));
    expect(onOpen.mock.calls.length, "one click should open the cart once").toBe(1);
  });
});
```

Line by line:

- `afterEach(cleanup)` empties the page after every test.
- `userEvent.setup()` makes a fresh pretend user for each test.
- The first test never reads `count`. It reads the button's words, `Cart (2)`, exactly as a customer would.
- The second test passes a spy and counts its calls.

Now try breaking `CartButton` in your head. Make it `setCount(count + 2)`: the first test fails. Remove `onClick={onOpen}`: the second test fails. Both tests earn their place.

## Try it

This runs one check against the real price formatter and two broken copies. Run it.

```js run
const real = (paise) => "₹" + (paise / 100).toFixed(2);
const forgotDivide = (paise) => "₹" + paise.toFixed(2);
const roundsAway = (paise) => "₹" + Math.round(paise / 100) + ".00";

function check(format) {
  return format(45000) === "₹450.00";
}

const copies = [["real", real], ["forgotDivide", forgotDivide], ["roundsAway", roundsAway]];
for (const [name, format] of copies) {
  console.log(name + ": " + (check(format) ? "check passes" : "check FAILS, bug caught"));
}
```

It prints:

```text
real: check passes
forgotDivide: check FAILS, bug caught
roundsAway: check passes
```

`roundsAway` throws away the paise, but Golu's price has none, so the check can't tell.

1. Change `45000` and `"₹450.00"` to a price with paise, like `12350` and `"₹123.50"`. Does the check catch `roundsAway` now?
2. Add a fourth copy that adds `"₹"` twice. Does your check catch it?

## Recap

- Test components the way customers use them: find things by role, label and text, act with `userEvent`, and check what changed on screen or what the callbacks received.
- Prefer queries in this order: `ByRole`, `ByLabelText`, `ByText`, and `ByTestId` only as a last resort. A role or label query also proves the page is accessible.
- `getBy` throws when something is missing, `queryBy` returns `null`, and `findBy` waits for something that appears later.
- Pass a `vi.fn()` spy as a callback prop, and compare its whole `mock.calls` list with `toEqual`.
- A test earns its place only if it would fail when the code is wrong. Break the code on purpose to find out.

## Common mistakes

**1. Finding elements by class or tag.**

```jsx
const button = container.querySelector(".add-btn");
```

Symptom: a designer renames the class and every test fails, though nothing broke for customers. Worse, the test still passes when the button loses its words. Fix: `screen.getByRole("button", { name: "Add to cart" })`.

**2. Checking only that a callback was called.**

```jsx
expect(onAdd.mock.calls.length > 0, "onAdd was called").toBe(true);
```

Symptom: the card sends `"Golu the elephant"` instead of `"DC-1042"`, or adds twice, and the test stays green. Fix: compare the whole list: `expect(onAdd.mock.calls, "...").toEqual([["DC-1042", 1]])`.

**3. Forgetting to await a user action.**

```jsx
user.click(screen.getByRole("button", { name: "Add to cart" }));
expect(onAdd.mock.calls.length, "one call").toBe(1);
```

Symptom: the check can fail with 0 calls, though the button works. The click hadn't finished when `expect` ran. Fix: `await user.click(...)`. Every `user` action returns a promise.

**4. Using getBy to check that something is gone.**

```jsx
expect(screen.getByText("Out of stock"), "should be gone").toBeNull();
```

Symptom: instead of your message, a long "Unable to find an element" error, because `getBy` throws before `expect` runs. Fix: `queryByText` returns `null`, so `expect(screen.queryByText("Out of stock"), "...").toBeNull()` works.

**5. Forgetting cleanup between tests.**

```jsx
// no afterEach(cleanup) in this file
```

Symptom: the second test fails, saying it found more than one matching button. The first test's card is still on the page. Fix: add `afterEach(cleanup);` at the top of the file.
