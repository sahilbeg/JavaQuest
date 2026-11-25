## What happened

Arjun runs your mock TypeScript loop today, and he plays the interviewer. He hands you a previous candidate's answers to three rounds. Every answer type-checks in strict mode. Every one still failed the round.

1. **The TypeScript round.** "When a customer logs in, merge the cart saved on the server with the one in the browser." The answer changes the saved cart, goes past 5 of one item, keeps old prices and keeps empty lines.
2. **The React round.** "Write a typed `useStoredState` hook that remembers a value in localStorage." The answer trusts whatever is stored, never saves, and shows the guest's cart after Meera logs in.
3. **The type design round.** "Design a typed event bus with `on`, `once` and `emit`." The types are lovely. Unsubscribing twice removes someone else's handler, and `once` does nothing.

Your job: fix each answer the way an interviewer expects. The lesson of the loop is the candidate's mistake: "it compiles" is where the work starts, not where it ends.

### Why the first answer changes the saved cart

`const merged = saved` doesn't copy anything. It's a second name for the same array, and `match.qty += ...` changes the saved line itself:

```animate
title: A second name, not a copy
kind: code
language: ts
code: |
  const saved = [{ id: "DC-1042", qty: 1 }];
  const merged = saved;
  merged[0].qty += 2;
  console.log(saved[0].qty);
  console.log(merged === saved);
steps:
  - line: 1
    vars: { saved: '[{ id: "DC-1042", qty: 1 }]' }
    say: The saved cart has one Golu.
  - line: 2
    vars: { merged: "same array as saved" }
    say: merged looks like a new cart, but it points at the very same array.
  - line: 3
    say: Adding 2 Golus to merged's line changes the one and only line object.
  - line: 4
    out: "3"
    say: The saved cart now says 3. Anything else holding it, like React state, changed behind its back.
  - line: 5
    out: "true"
    say: They are one array. Typing the parameter readonly CartLine[] makes tsc refuse the candidate's merged.push(...), and new line objects avoid the rest.
```

### Why unsubscribing twice hurts someone else

The candidate's unsubscribe is `list.splice(list.indexOf(handler), 1)`. The second time, the handler is gone, `indexOf` gives `-1`, and `splice(-1, 1)` means "remove the last item":

```animate
title: splice with -1 removes the last handler
kind: code
language: ts
code: |
  const handlers = ["badge", "toast"];
  handlers.splice(handlers.indexOf("badge"), 1);
  console.log(handlers.join(", "));
  handlers.splice(handlers.indexOf("badge"), 1);
  console.log(handlers.length);
steps:
  - line: 1
    vars: { handlers: '["badge", "toast"]' }
    say: Two handlers listen for "added". The cart badge and a toast message.
  - line: 2
    vars: { handlers: '["toast"]' }
    say: The badge unsubscribes. indexOf finds it at 0, and splice removes it.
  - line: 3
    out: "toast"
    say: So far, so good. Only the toast is left.
  - line: 4
    vars: { handlers: "[]" }
    say: The badge's unsubscribe runs again, from a second cleanup. indexOf gives -1, and splice(-1, 1) removes the last item, the toast.
  - line: 5
    out: "0"
    say: The toast never shows again, and nothing told anyone why.
```

## What you need

You learned each idea in an earlier ticket. Here they meet in one incident.

- New arrays and objects instead of changing old ones: [Destructuring, spread and rest](/t/web-04-javascript-in-depth/topic-04-destructuring-and-spread) and [Map and Set](/t/web-04-javascript-in-depth/topic-11-maps-and-sets).
- Checking stored data instead of trusting it: [Typed APIs end to end](/t/tsreact-12-professional-typescript/topic-03-typed-apis-end-to-end) and [Strict mode and tsconfig in depth](/t/tsreact-12-professional-typescript/topic-02-strictness).
- Effects that rerun when an input changes: [Typed effects](/t/tsreact-11-typed-react-in-depth/topic-01-typed-effects).
- Generic types with `keyof` and indexed types like `Events[K]`: [Mapped and template literal types](/t/ts-09-typescript-in-depth/topic-09-mapped-types).
- The interview method: [TypeScript interviews](/t/tsreact-12-professional-typescript/topic-08-interviews).

### Syntax: a typed event map

```ts
interface StoreEvents {
  added: { id: string; qty: number };
  paid: { orderId: string; paise: number };
}

const bus = createEventBus<StoreEvents>();
bus.on("paid", (payment) => console.log(payment.orderId));
```

The interface maps each event name to its payload type. In `on<K extends keyof Events>(type: K, handler: (payload: Events[K]) => void)`, `K` is the event name, and `Events[K]` is its payload. So `bus.on("paid", ...)` knows `payment` has an `orderId`, and `bus.emit("paid", { id: "DC-1042" })` is a type error.

```callout jargon
**Copy-on-write** means never changing a shared list in place. To add or remove an item, build a new list and swap it in. Anyone still looping over the old list keeps a list that can't change under them.
```

## The tiers

1. **Bronze** resolves the incident. `mergeCarts` keeps one line per item, adds quantities up to 5, takes the browser's newer name and price, keeps saved lines first, drops empty lines, and changes neither cart.
2. **Silver**: the React round. `useStoredState` keeps stored data only when it's good JSON that `isValid` accepts, saves every change to `localStorage`, and reads the new key's value when the key changes.
3. **Gold**: the type design round. `on` returns an unsubscribe that removes exactly its own subscription and is safe to call twice, `emit` survives handlers that unsubscribe while it runs, and `once` fires once and can be cancelled.

The exact rules are in the README.

## Recap

- "It compiles" is the start of an answer. Try edge cases by hand: limits, old data, empty lines, calling things twice.
- `const merged = saved` is a second name, not a copy. Build new objects, and type parameters `readonly` so the compiler refuses changes.
- Data from localStorage is `unknown` until checked. Parse it as `unknown`, check it with a type guard, and fall back to the initial value.
- An effect with `[key]` in its dependencies reruns when the key changes, so a hook can follow a new customer.
- A typed event bus maps names to payloads with `Events[K]`. Give each subscription its own identity, and replace lists instead of changing them while they may be looped over.

## Common mistakes

**1. Copying the array but not the lines.**

```ts
const merged = [...saved];
merged[0].qty += line.qty;
```

Symptom: the bronze check says the saved cart changed. The array is new, but its line objects are the old ones. Fix: build a new line, `{ ...line, qty: ... }`.

**2. Trusting the stored value with as.**

```ts
const cart = JSON.parse(stored) as CartLine[];
```

Symptom: the silver check says a stored object, or a qty of `"2"`, came back as a cart. Fix: `const parsed: unknown = JSON.parse(stored); return isValid(parsed) ? parsed : initial;`.

**3. Setting state but never saving it.**

```ts
function save(next: T) {
  setValue(next);
}
```

Symptom: the silver check says nothing was written under the key. On a real phone, the cart empties on refresh. Fix: `localStorage.setItem(key, JSON.stringify(next));` before `setValue(next)`.

**4. Unsubscribing by position.**

```ts
const off = () => list.splice(list.indexOf(handler), 1);
```

Symptom: the gold check says a second unsubscribe removed the toast handler. Fix: `listeners[type] = (listeners[type] ?? []).filter((h) => h !== entry);`, which removes nothing when the entry is already gone.

**5. Looping over a list that handlers can change.**

```ts
for (let i = 0; i < list.length; i++) list[i](payload);
```

Symptom: the gold check says the second handler missed the event, because the first one removed itself and everything shifted left. Fix: copy-on-write. `on` and unsubscribe build new lists, so `emit` loops over one that never changes.
