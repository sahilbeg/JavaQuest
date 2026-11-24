## Hook

Look at any price tag in an Indian shop: ₹1,299.00. There's the ₹ sign, a comma after the thousands, and always two digits for the paise. Nobody writes 1299, or 150.5, or 129900.

Dhaaga's code keeps money in whole paise, which is perfect for sums. But customers read rupees. This ticket turns paise into prices that look right, as the very last step before they reach the screen.

## Concept

### From paise to rupees

Dividing by 100 turns paise into rupees. The answer is a plain number, and it doesn't look like a price yet:

```js run
console.log(129900 / 100);
console.log(15050 / 100);
console.log(15000 / 100);
```

This prints `1299`, `150.5` and `150`. There's no ₹ sign, no comma, and the paise digits come and go. Two tools fix that.

### Syntax: toFixed

```js
const text = rupees.toFixed(2);
```

`toFixed(2)` turns a number into text with exactly 2 digits after the decimal point.

```js run
const paise = 15050;
console.log(paise / 100);
console.log((paise / 100).toFixed(2));
console.log((89900 / 100).toFixed(2));
console.log(typeof (89900 / 100).toFixed(2));
```

This prints `150.5`, `150.50`, `899.00` and `string`. Notice the brackets around `paise / 100`: the division must happen before `toFixed` runs.

### toFixed gives text

`toFixed` hands back a **string**, not a number. That's right for showing a price, and wrong for doing more maths with it. `"150.00" + "30.00"` glues into `"150.0030.00"`. So do every sum in paise first, and format once, at the end.

### Syntax: Intl.NumberFormat

```js
const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });
```

`Intl.NumberFormat` is a number formatter built into JavaScript. You tell it two things once: the **locale**, which is the country's way of writing numbers, and the style. Then its `format` method turns any number into text.

```js run
const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });
console.log(inr.format(1299));
console.log(inr.format(150.5));
console.log(inr.format(123456.78));
console.log(inr.format(0));
```

This prints `₹1,299.00`, `₹150.50`, `₹1,23,456.78` and `₹0.00`. The ₹ sign, the commas and the two decimals all come for free.

```callout jargon
A **locale** is a code for a language and region, like `"en-IN"` for English in India. It decides how numbers, dates and money are written.
```

`new` makes a new formatter object. You'll meet `new` properly in web-04. For now, read it as "make me a formatter with these settings".

### Indian grouping: lakhs and crores

In India, big numbers are grouped differently. After the first three digits, commas come every two digits: 1,00,000 is one lakh, and 1,00,00,000 is one crore. The `"en-IN"` locale knows this:

```js run
const amount = 123456.78;
console.log(new Intl.NumberFormat("en-IN").format(amount));
console.log(new Intl.NumberFormat("en-US").format(amount));
```

This prints `1,23,456.78`, then `123,456.78`. The second line uses the American way. Dhaaga's customers expect the first.

Watch one price go from paise to the screen:

```animate
title: From paise to a price tag
kind: code
language: js
code: |
  const paise = 129900;
  const rupees = paise / 100;
  const plain = rupees.toFixed(2);
  const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });
  const pretty = inr.format(rupees);
  console.log(plain);
  console.log(pretty);
steps:
  - line: 1
    vars: { paise: 129900 }
    say: The price of an order, stored the safe way, in whole paise.
  - line: 2
    vars: { rupees: 1299 }
    say: Divide by 100 once, at the very end. 129900 paise is 1299 rupees.
  - line: 3
    vars: { plain: '"1299.00"' }
    say: toFixed(2) makes a string with two decimals. Notice the quotes. It's text now, not a number.
  - line: 4
    vars: { inr: "(a formatter)" }
    say: Make an en-IN currency formatter. It knows the ₹ sign and the Indian commas.
  - line: 5
    vars: { pretty: '"₹1,299.00"' }
    say: format turns the rupees into a finished price, ready for a customer.
  - line: 6
    out: "1299.00"
    say: toFixed alone gives two decimals, but no sign and no comma.
  - line: 7
    out: "₹1,299.00"
    say: The formatter gives the whole thing.
```

### Make the formatter once

Building a formatter takes a little work. Make it once, outside your functions, and reuse it for every price:

```js
const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

function price(paise) {
  return inr.format(paise / 100);
}
```

### Syntax: padStart and padEnd

```js
const column = text.padStart(10);
```

A printed receipt needs its prices in a neat column. `padEnd(n)` adds spaces after a string until it is `n` characters long. `padStart(n)` adds them in front. A second argument pads with another character.

```js run
console.log("Keychain".padEnd(12) + "|");
console.log("₹120.00".padStart(10) + "|");
console.log("7".padStart(3, "0"));
```

This prints `Keychain    |`, then `   ₹120.00|`, then `007`. A string that's already long enough is left as it is.

## Worked example

A receipt with labels on the left and prices lined up on the right:

```js run
const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });
const lines = [
  { label: "Tote bag", paise: 89900 },
  { label: "Gift wrap", paise: 3000 },
  { label: "Delivery", paise: 6000 },
];
let total = 0;
for (const line of lines) {
  console.log(line.label.padEnd(14) + inr.format(line.paise / 100).padStart(12));
  total = total + line.paise;
}
console.log("Total".padEnd(14) + inr.format(total / 100).padStart(12));
```

1. Line 1 makes the formatter once, before anything else.
2. Each line of the receipt pads its label to 14 characters, then pads its price to 12, so every price ends in the same column.
3. The total is added up in paise, `98900`, while the loop runs.
4. Only the last line turns the total into `₹989.00`.

## Try it

Run it. Then add a stitched name for `5000` paise, and change the amigurumi to a quantity of 3.

```js run
const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });
const amigurumi = 45000 * 1;
const giftWrap = 3000;
const total = amigurumi + giftWrap;
console.log("Amigurumi: " + inr.format(amigurumi / 100));
console.log("Gift wrap: " + inr.format(giftWrap / 100));
console.log("Total: " + inr.format(total / 100));
```

## Recap

- Keep money in paise for every sum, and divide by 100 only when you show it.
- `(paise / 100).toFixed(2)` gives text with two decimals, like `"150.50"`.
- `new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" })` makes a formatter, and `format` gives `₹1,299.00`.
- The `"en-IN"` locale groups big numbers in lakhs: `₹1,23,456.78`.
- `padEnd` and `padStart` add spaces to line up receipt columns.

## Common mistakes

**1. Formatting paise as if they were rupees.**

```js
const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });
console.log(inr.format(89900));
```

This prints `₹89,900.00` for a ₹899 tote bag. The formatter expects rupees. Divide first: `inr.format(89900 / 100)`.

**2. Doing maths with formatted text.**

```js
const bag = (899).toFixed(2);
const wrap = (30).toFixed(2);
console.log(bag + wrap);
```

This prints `899.0030.00`, because `toFixed` gave two strings and `+` glued them. Add the paise first, then format the total once.

**3. Rounding rupee decimals with toFixed.**

```js run
console.log((1.005).toFixed(2));
console.log((2.675).toFixed(2));
```

This prints `1.00` and `2.67`, not `1.01` and `2.68`. The decimals are stored as close guesses just below the real values. Do any rounding in whole paise first. Then `toFixed` and the formatter only ever show an amount that's already exact.

**4. Using the wrong locale.**

```js
const usd = new Intl.NumberFormat("en-US");
console.log(usd.format(123456));
```

This prints `123,456`, which an Indian customer reads oddly. Use `"en-IN"` to get `1,23,456`.
