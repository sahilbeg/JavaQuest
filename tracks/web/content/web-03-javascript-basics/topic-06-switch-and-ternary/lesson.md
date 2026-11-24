## Hook

Behind the stall counter hangs the price board: CST ₹150, KEY ₹120, AMI ₹450, BAG ₹899, BTY ₹350. When a customer says a code, Raju runs his finger down the board and reads one line.

You could write that as a long `if`, `else if`, `else if` chain. When one value picks one of many options, JavaScript has a neater tool: `switch`. And for a quick choice between just two values, there's an even shorter one.

## Concept

### Syntax: switch

```js
switch (value) {
  case "A":
    // runs when value === "A"
    break;
  case "B":
    // runs when value === "B"
    break;
  default:
    // runs when no case matched
}
```

`switch` looks at one value and jumps to the `case` that matches it. The code under that case runs until a `break`, which jumps out of the whole switch. `default` catches every value that no case matched.

Watch a switch handle a payment at the counter:

```animate
title: Jumping to the matching case
kind: code
language: js
code: |
  const mode = "UPI";
  switch (mode) {
    case "CASH":
      console.log("Count the notes");
      break;
    case "UPI":
      console.log("Check the payment message");
      break;
    default:
      console.log("Ask how they want to pay");
  }
  console.log("Payment noted");
steps:
  - line: 1
    vars: { mode: '"UPI"' }
    say: The customer pays by UPI.
  - line: 2
    say: switch takes the value in mode and looks for a case that matches it.
  - line: 3
    say: Is mode "CASH"? No, so the code under this case is skipped.
  - line: 6
    say: Is mode "UPI"? Yes, this case matches.
  - line: 7
    out: Check the payment message
    say: JavaScript runs the code under the matching case.
  - line: 8
    say: break jumps straight out of the switch. Nothing below it inside the switch runs.
  - line: 12
    out: Payment noted
    say: The program carries on after the switch's closing brace.
```

### Why break matters

Without `break`, JavaScript doesn't stop at the next `case`. It keeps running every line below, into the other cases. This is called **falling through**.

```js run
const mode = "CASH";
switch (mode) {
  case "CASH":
    console.log("Count the notes");
  case "UPI":
    console.log("Check the payment message");
  default:
    console.log("Ask how they want to pay");
}
```

This prints all three lines, even though the customer paid cash. Once a case matches, the labels below it are ignored. Only `break` (or `return`) stops it.

```callout jargon
**Falling through** means running on from one case into the next, because there was no `break`. It's almost always a mistake.
```

### Syntax: return inside a switch

```js
function paymentLabel(mode) {
  switch (mode) {
    case "CASH":
      return "Cash at the counter";
    case "UPI":
      return "UPI";
    default:
      return "Unknown payment";
  }
}
```

Inside a function, `return` leaves the whole function at once. So a case that returns doesn't need a `break` after it. This is the neatest way to turn a code into a value.

### Syntax: sharing one case

```js
switch (mode) {
  case "UPI":
  case "CARD":
    console.log("Check the bank app");
    break;
}
```

Write two labels one after the other, with nothing between them, and they share the same code. Here both `"UPI"` and `"CARD"` print `Check the bank app`. This is the one useful kind of falling through.

### switch compares with ===

`switch` matches with strict equality, `===`. So the type has to match too:

```js run
const typed = "1";
switch (typed) {
  case 1:
    console.log("one");
    break;
  default:
    console.log("no match");
}
```

This prints `no match`, because the text `"1"` is not `===` the number `1`. Capitals count as well: `"ami"` won't match `case "AMI"`.

### Syntax: the ternary operator

```js
const result = condition ? valueIfTrue : valueIfFalse;
```

The **ternary operator** is a tiny if-else that gives back a value. Read it as: "is this true? Then this one. Otherwise, that one."

```js run
const qty = 1;
console.log(`${qty} ${qty === 1 ? "keychain" : "keychains"}`);
const subtotal = 120000;
const delivery = subtotal > 99900 ? 0 : 6000;
console.log(delivery);
```

This prints `1 keychain`, then `0`. The ternary fits anywhere a value can go, even inside `${...}`.

```callout jargon
**Ternary** means "made of three parts": the condition, the value if true, and the value if false. It's the only JavaScript operator with three parts.
```

### Which one to use

- One value, many fixed options, like item codes or payment modes: `switch`.
- Ranges and mixed conditions, like `hour >= 10 && hour < 20`: `if` and `else if`.
- A quick pick between two values: the ternary.

When a choice needs several lines of code in each branch, use a normal `if`. It's easier to read than a long ternary.

## Worked example

Where can a customer shop on each day of the week?

```js run
const day = "Mon";
let where;
switch (day) {
  case "Sat":
  case "Sun":
    where = "the FC Road stall or the Aundh shop";
    break;
  case "Mon":
    where = "WhatsApp only";
    break;
  default:
    where = "the Aundh shop";
}
console.log(`${day}: ${where}`);
console.log(day === "Mon" ? "Shop closed today" : "Shop open today");
```

1. Line 2 makes an empty `let` box, to be filled by the switch.
2. `"Sat"` and `"Sun"` share one case. `day` is `"Mon"`, so neither matches.
3. `case "Mon"` matches. `where` gets `"WhatsApp only"`, and `break` jumps out.
4. Line 14 prints `Mon: WhatsApp only`.
5. Line 15 uses a ternary to pick one of two messages and prints `Shop closed today`.

## Try it

Run it. Then change the last line to `paymentLabel("upi".toUpperCase())`, and add a `case "CARD":` that returns `"Card"`.

```js run
function paymentLabel(mode) {
  switch (mode) {
    case "CASH":
      return "Cash at the counter";
    case "UPI":
      return "UPI";
    default:
      return "Unknown payment";
  }
}
console.log(paymentLabel("CASH"));
console.log(paymentLabel("upi"));
```

## Recap

- `switch` jumps to the `case` that is `===` to its value, and `default` catches everything else.
- Every case needs a `break` or a `return`, or it falls through into the next case.
- Two labels in a row share the same code, like `case "Sat": case "Sun":`.
- `condition ? a : b` picks one of two values, and fits anywhere a value can go.

## Common mistakes

**1. Forgetting break.**

```js
const code = "KEY";
switch (code) {
  case "KEY":
    console.log("Keychain");
  case "BAG":
    console.log("Tote bag");
    break;
}
```

This prints `Keychain`, then `Tote bag`. The KEY case fell through into BAG. Add `break;` after each case, or `return` from it.

**2. A case with the wrong type.**

```js
const qty = "2";
switch (qty) {
  case 2:
    console.log("A pair");
    break;
  default:
    console.log("Some other number");
}
```

This prints `Some other number`, because the text `"2"` is not `===` the number `2`. Convert first: `switch (Number(qty))`.

**3. A ternary with too much in it.**

```js
const paise = 45000;
const band = paise < 20000 ? "small" : paise < 50000 ? "medium" : "large";
console.log(band);
```

This prints `medium`, but it's hard to read and easy to break. When there are more than two options, use `if`, `else if`, `else` or a `switch`.
