## Hook

In a big railway station, you don't wander from platform to platform hoping to find your train. You read the board. It tells you where each train goes and which platform it leaves from.

In a big TypeScript codebase, the types are the board. They tell you what every piece of data looks like, where it comes from and where it goes. Read them first, and the 400 files stop being a maze.

## Concept

### Types are the map

The general method for any big codebase is: run it, find the entry point (the file where the app starts), and follow one feature through. TypeScript adds a shortcut. The data's types are usually in a few files, like `types.ts`, `api/types.ts` or `models/`, and they describe the whole business in a few hundred lines.

So read those files first. For dhaaga.in, `Order`, `CartLine`, `Item` and `Payment` tell you more in five minutes than an hour of clicking through components:

```ts
export interface Order {
  id: string;
  customer: Customer;
  lines: CartLine[];
  payment: Payment;
  status: OrderStatus;
}
```

Every screen that shows an order works with this shape. Learn it, and you can read those screens.

### The editor knows the way

```callout jargon
A **language server** is the part of your editor that understands TypeScript while you type. It powers the red underlines, the hovers and the jumps between files.
```

Because the editor understands the types, it can answer questions no text search can. In VS Code:

| Question | Do this |
|---|---|
| What is this value's type? | Hover over it |
| Where is this defined? | Go to Definition (F12, or Ctrl/Cmd + click) |
| Where is this type's shape written? | Go to Type Definition |
| Who uses this? | Find All References (right-click, or Shift + Alt + F12) |
| Rename it everywhere, safely | Rename Symbol (F2) |
| Who calls this function? | Show Call Hierarchy |

Hovering is the cheapest of all. Hover over a variable in an unfamiliar component, and you know its shape without opening another file.

### References, not text search

A text search for `total` finds every place the letters appear: comments, other variables called `total`, words in strings. **Find All References** finds only the real uses of one particular `total`, the one you clicked on.

The same goes for renaming. Search and replace can change a comment, a CSS class and an unrelated variable. **Rename Symbol** changes exactly the uses of that one thing, across every file, and nothing else.

```callout tip
Use text search when you're looking for words, like an error message a customer saw. Use references when you're looking for a thing in the code.
```

### Change the type, follow the errors

This is the most useful trick in a typed codebase. To find everything a change touches, **make the change in the type first**, then let the compiler list the places:

```animate
title: Adding a delivery slot to every order
kind: scene
nodes:
  - { id: type, label: Order type, emoji: "📐", value: "no slot", row: 1, col: 0 }
  - { id: tsc, label: tsc --noEmit, emoji: "🔍", value: idle, row: 1, col: 2 }
  - { id: api, label: api/orders.ts, emoji: "🔌", value: ok, row: 0, col: 4 }
  - { id: form, label: CheckoutForm.tsx, emoji: "📝", value: ok, row: 1, col: 4 }
  - { id: fixture, label: test fixtures, emoji: "🧪", value: ok, row: 2, col: 4 }
steps:
  - say: Add a required field to the type, slot, which is "morning" or "evening". No other file changes yet.
    set: { type: "slot added" }
    focus: [type]
  - say: Run the type checker over the whole project.
    send: { from: type, to: tsc, label: "npm run typecheck" }
    set: { tsc: checking }
  - say: The parser that builds orders from the API now misses slot. That's the first error.
    send: { from: tsc, to: api, label: "TS2741" }
    set: { api: error }
  - say: The checkout form builds an Order without a slot. Second error. It needs a slot picker.
    send: { from: tsc, to: form, label: "TS2741" }
    set: { form: error }
  - say: The test builder makes orders too, so it needs a default slot. That's every place, and nothing was missed.
    send: { from: tsc, to: fixture, label: "TS2741" }
    set: { fixture: error, tsc: "3 errors" }
  - say: Fix them one by one until the list is empty. The error count is the size of the change.
    set: { api: fixed, form: fixed, fixture: fixed, tsc: "0 errors" }
```

The error list is a to-do list nobody could write by hand. Its length is a good first estimate of how big the change is.

### Imports, aliases and barrels

Big codebases have a few import habits that confuse newcomers:

```ts
import { formatRupees } from "@/lib/money";
import { Button, Card, Dialog } from "@/components";
```

```callout jargon
A **path alias** like `@/` is a short name for a folder, set in tsconfig's `paths` option. `@/lib/money` might mean `src/lib/money.ts`. A **barrel** is an `index.ts` file that re-exports many modules, so one import line can fetch them all.
```

Go to Definition follows aliases and barrels for you, so press F12 rather than guessing the folder. If an alias doesn't resolve, look for `paths` in `tsconfig.json`.

### Declaration files

```callout jargon
A **declaration file** ends in `.d.ts`. It describes types for code that lives somewhere else, without any code of its own.
```

You'll meet three kinds:

- **Library types** in `node_modules/@types/...` or inside the package itself. Going to the definition of `useState` opens React's `.d.ts`.
- **Project globals**, like `vite-env.d.ts`, which types `import.meta.env`.
- **Generated types**, such as `api.generated.ts` made from the backend's OpenAPI file. Don't edit these by hand. Find the command that generates them, usually in `package.json`.

When a type looks wrong in a `.d.ts` from a library, the fix is almost never in that file. Check the version, or wrap the library in a small typed helper of your own.

## Worked example

Kavya asks: how big is "orders get a delivery slot"? Here's how you'd answer in under an hour, in a store you barely know.

1. **Find the type.** Search the code for `interface Order`. Go to Definition lands in `src/types/order.ts`. Hover over `CartLine` and `Payment` to see their shapes too.
2. **See who uses it.** Find All References on `Order` lists 23 places: the API parser, the checkout, the account page, the admin screens, the tests.
3. **Make the change in the type.**

```ts
export type DeliverySlot = "morning" | "evening";

export interface Order {
  id: string;
  customer: Customer;
  lines: CartLine[];
  payment: Payment;
  status: OrderStatus;
  slot: DeliverySlot;
}
```

4. **Run the checker.** `npm run typecheck` reports 6 errors in 4 files: the API parser, the checkout form, the order builder in tests, and an admin export. The other 19 references only read orders, so they keep working.
5. **Answer Kavya.** "Four files need changes: parsing the slot, a slot picker at checkout, the test builder and the admin export. The account page should show the slot, which tsc can't know, so I'll add that by hand. About two days, with tests."

Step 5 matters. The compiler finds every place that **breaks**. It can't find places that **should** change but still compile, like a screen that ought to show the new field. Those come from reading the feature, and asking.

## Try it

This compares a text search with real references, on three tiny files. Run it.

```ts run
const files: Record<string, string> = {
  "src/cart.ts": "export function total(lines: CartLine[]) { return lines.reduce((s, l) => s + l.paise * l.qty, 0); }",
  "src/Checkout.tsx": "const { total } = cartTotals(lines); // total shown on the Pay button",
  "src/report.ts": "import { total } from './cart.ts'; console.log(total(orders));",
};

for (const [path, text] of Object.entries(files)) {
  const hits = text.split("total").length - 1;
  console.log(path + ": " + hits);
}
```

It prints:

```text
src/cart.ts: 1
src/Checkout.tsx: 2
src/report.ts: 2
```

The text search finds 5 hits. But both hits in `Checkout.tsx` are a different `total` and a comment. Find All References on the function in `cart.ts` would list only 3: its definition and the two uses in `report.ts`.

1. Which file would Rename Symbol change if you renamed the function to `cartTotal`?
2. What would a plain search and replace of `total` do to `Checkout.tsx`?

## Recap

- In a big typed codebase, read the data types first. They describe the whole business in a few files.
- Let the editor navigate: hover for types, Go to Definition, Find All References, Rename Symbol and the call hierarchy.
- Prefer references and Rename Symbol over text search and replace when you're looking for a thing in the code.
- To size a change, change the type first and run `tsc --noEmit`. The error list is the to-do list, but it can't find places that still compile.
- Know path aliases (`@/`), barrels (`index.ts`) and declaration files (`.d.ts`), and never edit generated types by hand.

## Common mistakes

**1. Starting from the screens.**

```text
Opened 40 components to work out what an order looks like.
```

Symptom: a day gone, and the shape is still unclear. Fix: Go to Definition on `Order` and read the types first.

**2. Renaming with search and replace.**

```text
Replace all: total -> cartTotal (57 changes)
```

Symptom: comments, a CSS class and an unrelated `total` in the checkout change too, and the build breaks in odd places. Fix: Rename Symbol (F2), which changes only that one thing.

**3. Making the type optional to avoid the errors.**

```ts
interface Order { slot?: DeliverySlot }
```

Symptom: everything compiles, and every order from the old checkout has no slot, so deliveries go out at random times. Fix: make it required, follow the errors, and add an optional field only if old orders really have none.

**4. Editing a generated file.**

```ts
// api.generated.ts, edited by hand
```

Symptom: the next time anyone regenerates the types, your change silently disappears. Fix: change the source (the OpenAPI file or the backend), and run the generator.

**5. Trusting the error list to be the whole change.**

```text
tsc is clean, so the slot feature is done.
```

Symptom: the slot is saved, but the account page never shows it, and customers ask Raju when their parcel comes. Fix: the compiler finds what breaks; you still read the feature for what should change.
