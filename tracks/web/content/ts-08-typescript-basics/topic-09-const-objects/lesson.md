## Hook

The price board at the stall and the price list in Meera Aunty's notebook say the same thing. When a new item arrives, someone has to change both. One busy Sunday, someone changes only the board, and the notebook is wrong for a month.

Code has the same problem. A list of item codes lives in an array, for the menu. The same codes live in a type, for the checker. This ticket writes the list once and lets TypeScript read the type off it.

## Concept

### The list written twice

Here is how the codes looked before:

```ts
type ItemCode = "CST" | "KEY" | "AMI" | "BAG" | "BTY";
const ITEM_CODES: ItemCode[] = ["CST", "KEY", "AMI", "BAG", "BTY"];
```

The array is for running code: drawing the menu, looping over items. The type is for the checker. Add a sixth item to the array, and TypeScript refuses it until someone also updates the type. Forget the array instead, and the menu never shows the item. Two copies of one fact always drift apart.

```callout jargon
A **single source of truth** means each fact is written in exactly one place, and everything else is worked out from it.
```

### Syntax: as const

```ts
const ITEM_CODES = ["CST", "KEY", "AMI", "BAG", "BTY"] as const;
```

Without `as const`, TypeScript infers `string[]` for this array: any number of any strings. With `as const`, it keeps every detail. The type becomes `readonly ["CST", "KEY", "AMI", "BAG", "BTY"]`: exactly these five strings, in this order, and nobody may change them.

It works on objects too:

```ts
const PRICES = { CST: 15000, KEY: 12000 } as const;
```

The type is `{ readonly CST: 15000; readonly KEY: 12000 }`. Every property is `readonly`, and every value keeps its literal type.

```callout jargon
**as const** after a value tells TypeScript to infer the narrowest type it can: literal types for every value, and `readonly` for every array and property.
```

### Syntax: typeof in a type

```ts
const ITEM_CODES = ["CST", "KEY", "AMI", "BAG", "BTY"] as const;
type Codes = typeof ITEM_CODES;
```

In a type position, `typeof` means "the type of this value". It's not the JavaScript `typeof` that gives `"string"` at run time. This one is erased like every other type. Here, `Codes` is `readonly ["CST", "KEY", "AMI", "BAG", "BTY"]`.

### Syntax: indexed access with [number]

```ts
const ITEM_CODES = ["CST", "KEY", "AMI", "BAG", "BTY"] as const;
type ItemCode = (typeof ITEM_CODES)[number];
```

`SomeArrayType[number]` means "the type of any entry". For the `as const` tuple, that's the union of all five literals: `"CST" | "KEY" | "AMI" | "BAG" | "BTY"`. Now the list is written once, and the type follows it.

```callout jargon
An **indexed access type** like `T[number]` or `T["paise"]` looks up the type of an entry or a property inside another type.
```

### Syntax: keyof

```ts
const PRICES = { CST: 15000, KEY: 12000, AMI: 45000 } as const;
type PricedCode = keyof typeof PRICES;
```

`keyof` gives the union of an object type's keys. Here, `PricedCode` is `"CST" | "KEY" | "AMI"`. A function that takes a `PricedCode` can always look its price up, because every key is guaranteed to exist.

Watch a new item travel from one list to every place that uses it:

```animate
title: One list, and everything follows it
kind: scene
nodes:
  - { id: list, label: "ITEM_CODES as const", emoji: "📜", value: "5 codes" }
  - { id: type, label: "ItemCode type", emoji: "🏷️", value: "5 choices" }
  - { id: menu, label: Menu on dhaaga.in, emoji: "📱", value: "5 items" }
  - { id: tsc, label: tsc, emoji: "🔍", value: idle }
steps:
  - say: For Rakhi, Kavya adds "RAK" to ITEM_CODES. That is the only line she changes.
    focus: [list]
    set: { list: "6 codes" }
  - say: ItemCode is worked out from the list, so it gains "RAK" at once. Nobody edits the type.
    send: { from: list, to: type, label: "(typeof ITEM_CODES)[number]" }
    set: { type: "6 choices" }
  - say: The menu loops over the same array when it runs, so it shows the new keychain too.
    send: { from: list, to: menu, label: "ITEM_CODES.map(...)" }
    set: { menu: "6 items" }
  - say: But PRICES has no price for RAK yet. tsc reads every use of ItemCode and finds the gap.
    send: { from: type, to: tsc, label: "PRICES[code]" }
    set: { tsc: "1 error" }
  - say: Kavya adds the price. Now every place agrees, and tsc is happy.
    send: { from: tsc, to: list, label: "add RAK's price", back: true }
    set: { tsc: "0 errors" }
```

### Enums, and why Dhaaga skips them

TypeScript also has **enums**, a way to name a fixed set of values. You'll see them in older codebases:

```ts
enum PaymentMode {
  Cash = "CASH",
  Upi = "UPI",
  Card = "CARD",
}
const mode = PaymentMode.Upi;
```

An enum isn't only a type. TypeScript turns it into a real JavaScript object, so it isn't simply erased. That's why Node can't run enums when it only strips types, and some build tools need extra settings for them. A literal union with `as const` does the same job with plain JavaScript values. That's what Dhaaga uses.

```callout jargon
An **enum** is a TypeScript feature that names a fixed set of values and creates a real object for them at run time. A literal union only exists for the checker.
```

### as const doesn't freeze anything

`as const` is a type, so it's erased too. TypeScript refuses `ITEM_CODES.push("RAK")`, but at run time the array is an ordinary array. Code that skips the checker, like a plain `.js` file, could still change it. If you need it locked at run time too, wrap it in `Object.freeze(...)` as well.

## Worked example

How many small toys can Meera Aunty make in each colour? The colours are written once:

```ts run
const COLOURS = ["Marigold", "Indigo", "Rani pink", "Mint", "Off-white"] as const;
type Colour = (typeof COLOURS)[number];

const GRAMS = { Marigold: 130, Indigo: 85, "Rani pink": 0, Mint: 300, "Off-white": 40 } as const;

function toysIn(colour: Colour): number {
  return Math.floor(GRAMS[colour] / 40);
}

for (const colour of COLOURS) {
  console.log(`${colour}: ${toysIn(colour)}`);
}
```

1. `COLOURS` is the single source of truth. `Colour` is worked out from it: five literal strings.
2. `GRAMS` has a key for every colour. If it missed one, `GRAMS[colour]` would be a type error, because that colour couldn't be looked up.
3. `toysIn("Pink")` would be refused, because `"Pink"` isn't a `Colour`.
4. The loop goes through the same array at run time and prints `Marigold: 3`, `Indigo: 2`, `Rani pink: 0`, `Mint: 7` and `Off-white: 1`.

## Try it

Run this. Then add `"RAK"` to `CODES` and run it again. The run works, but `tsc` would point at one line. Which one, and how would you fix it?

```ts run
const CODES = ["CST", "KEY", "BTY"] as const;
type Code = (typeof CODES)[number];
const PRICE = { CST: 15000, KEY: 12000, BTY: 35000 } as const;
for (const code of CODES) {
  const c: Code = code;
  console.log(c, PRICE[c]);
}
```

## Recap

- Write each fixed list once, and work its type out from it, so the two can never drift apart.
- `as const` keeps literal types and makes arrays and properties `readonly`.
- `typeof value` in a type gives the value's type, and `(typeof LIST)[number]` turns a list into a union.
- `keyof typeof OBJ` gives the union of an object's keys.
- Enums create real objects at run time. Dhaaga uses `as const` and literal unions instead.

## Common mistakes

**1. Forgetting as const.**

```ts
const CODES = ["CST", "KEY", "AMI"];
type Code = (typeof CODES)[number];
```

There's no error, but `CODES` is a plain `string[]`, so `Code` is just `string`. Every text is now a valid "code", and the checker can't catch a typo like `"CTS"`. Add `as const`.

**2. Using a value's name as a type.**

```ts
const CODES = ["CST", "KEY", "AMI"] as const;
type Code = CODES[number];
```

```text
error TS2749: 'CODES' refers to a value, but is being used as a type here. Did you mean 'typeof CODES'?
```

Values and types live in different worlds. Use `typeof` to bring a value's type across: `(typeof CODES)[number]`.

**3. Changing an as const list.**

```ts
const CODES = ["CST", "KEY", "AMI"] as const;
CODES.push("RAK");
```

```text
error TS2339: Property 'push' does not exist on type 'readonly ["CST", "KEY", "AMI"]'.
```

The list is fixed on purpose. Add the new code where the list is written, in the source.

**4. Running an enum where types are only stripped.**

```ts
enum Mode {
  Cash = "CASH",
}
console.log(Mode.Cash);
```

`tsc` is happy, but Node's type stripping refuses the file with an `ERR_UNSUPPORTED_TYPESCRIPT_SYNTAX` error, because an enum isn't just a type. Use `const MODES = ["CASH"] as const;` instead.
