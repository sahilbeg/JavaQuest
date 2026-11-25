## What happened

This morning, Meera Aunty's new price sheet went live. It's a spreadsheet, and when it's exported, some prices come out as numbers and some as text: `15000` for coasters, but `"12000"` for keychains and `" 35000 "`, with spaces, for baby booties.

The catalogue's type says so honestly: `price: number | string`. But the old `toPaise` was written when every price was a number, and it does this:

```ts
function toPaise(price: number | string): number {
  return price as number;
}
```

`as number` tells TypeScript "trust me, it's a number". TypeScript stops checking, and the text goes straight into the maths. Run the old code yourself:

```ts run
function oldToPaise(price: number | string): number {
  return price as number;
}
const keychain = oldToPaise("12000") + 0;
const booties = oldToPaise(" 35000 ") + 0;
console.log(keychain, typeof keychain);
console.log(keychain * 1, booties * 1);
```

It prints `120000 string`, then `120000 NaN`. `"12000" + 0` glues a `0` on the end, so a ₹120 keychain costs ₹1,200. `" 35000 " + 0` is `" 35000 0"`, which isn't a number at all, so the booties cost `NaN`. Mrs. Deshpande's keychain and coaster set came to `135000` paise instead of `27000`.

Watch the gluing happen on a line of two wrapped keychains:

```animate
title: The assertion lets text into the maths
kind: code
language: ts
code: |
  function oldToPaise(price: number | string): number {
    return price as number;
  }
  const unit = oldToPaise("12000") + 3000;
  const line = unit * 2;
  console.log(typeof unit, unit);
  console.log(line);
steps:
  - line: 4
    say: The keychain's price arrives from the sheet as the text "12000".
  - line: 2
    vars: { price: '"12000"' }
    say: as number changes nothing when the code runs. It only tells TypeScript to stop checking. price is still text.
  - line: 4
    vars: { price: ~, unit: '"120003000"' }
    say: Text plus a number glues. The ₹30 gift wrap is stuck on the end, and TypeScript, told to trust, says nothing.
  - line: 5
    vars: { line: 240006000 }
    say: Times turns the glued text into a number, so the mistake now looks like a real price.
  - line: 6
    out: "string 120003000"
    say: The unit price was never a number at all.
  - line: 7
    out: "240006000"
    say: Two wrapped keychains would cost ₹24,00,060. The real price is ₹300.
```

```callout analogy
It's like a stall helper who stops reading price tags because "they're always in rupees". The day a tag says "$5", the helper charges ₹5, and nobody notices until the evening count.
```

## What you need

Everything here comes from this project and the JavaScript ones before it:

- A union like `number | string` needs checking before you use what only one member has ([Narrowing](/t/ts-08-typescript-basics/topic-07-narrowing)).
- `Number(text)` turns text into a number. `Number("")` is `0`, and `Number("abc")` is `NaN` ([Basic types](/t/ts-08-typescript-basics/topic-02-basic-types)).
- `Number.isInteger` says whether a number is whole, and paise are always whole ([Numbers and paise](/t/web-03-javascript-basics/topic-02-numbers-and-paise)).
- `throw new Error("...")` stops the sale with a message, instead of charging a wrong amount ([Errors](/t/web-04-javascript-in-depth/topic-08-errors)).

### Syntax: a type assertion, as

```ts
const paise = price as number;
```

`value as Type` is a **type assertion**. It tells TypeScript to treat a value as a type, without any check. Like `!`, it is erased before running, so it can't change the value. It's the root of this incident. Whenever you're tempted to write `as`, check the value with `typeof` instead, and let narrowing do the work.

```callout jargon
A **type assertion** (`value as Type`) tells TypeScript to treat a value as a certain type. It does no checking at all, so a wrong assertion hides a bug instead of catching it.
```

### Syntax: Number.isFinite

```ts
const ok = Number.isFinite(rupees);
```

`Number.isFinite(x)` is `true` for any real number, whole or not, and `false` for `NaN` and `Infinity`. Use it when an amount may have decimals, like rupees. For paise, which must be whole, use `Number.isInteger`.

## The tiers

1. **Bronze** resolves the incident. `toPaise(price)` checks which member of the union it got. A number stays as it is, and text is trimmed and turned into a number: `" 35000 "` gives `35000`. The `as number` goes. A keychain is `12000` paise again, and Mrs. Deshpande's order comes to `27000`.
2. **Silver**: a bad price must stop the sale, not slip through. `toPaise` throws an `Error` whose message starts `Bad price` for anything that isn't a whole number of paise, 0 or more: `"abc"`, `""`, `"   "`, `"120.50"`, `"-100"` and `150.5`. Watch out: `Number("")` is `0`, so empty text needs its own check. `lineTotal` throws `Unknown item: XYZ` for a code that isn't in the rows, instead of charging nothing.
3. **Gold**: Meera Aunty's new sheet adds a rupee column, typed by hand. `rupeesToPaise(text)` ignores `₹`, commas and spaces, and rounds to the nearest paisa: `"₹1,299.50"` gives `129950`, `"0.29"` gives `29`, and `"₹1,00,000"` gives `10000000`. Text with no amount, or a negative amount, throws `Bad price`.

## Recap

- `as Type` switches the checker off for one value. It never changes the value itself.
- Data from outside, like a spreadsheet, a form or an API, can be any type. Type it honestly, as a union.
- Narrow with `typeof` before you use the value, and turn text into a number with `Number`.
- Check the number too: `Number.isInteger` for paise, `Number.isFinite` for rupees, and never accept `NaN`.
- A bad price should throw and stop the sale, not become `0` or `NaN` on a customer's bill.

## Common mistakes

**1. Swapping one assertion for another.** `return Number(price) as number;` looks safer, but `Number(" 35000 ")` is fine while `Number("")` is `0`, and `as` checks neither. Bronze passes, and silver shows the gap.

**2. Forgetting that empty text is 0.** `Number("")` and `Number("   ")` are both `0`, so a blank cell in the sheet would make an item free. Check for empty text after trimming, and treat it as a bad price.

**3. Using parseInt.** `parseInt("120.50")` gives `120`, and `parseInt("45O00")` gives `45`. It quietly reads as much as it can and drops the rest. `Number` refuses the whole text with `NaN`, which is what you want.

**4. Rounding rupees down.** `Math.floor(0.29 * 100)` is `28`, because `0.29 * 100` is `28.999999999999996`. Use `Math.round` to reach the nearest paisa.
