## Hook

Before Diwali, Meera Aunty counts every gift box twice against a checklist: "4 coasters, gift wrap, name tag". It takes a minute, and it catches the box with 3 coasters before a customer does.

Code can have a checklist too. You write down a few examples, like "an order of exactly ₹999 pays ₹60 delivery", as code. A tool runs every example after every change, in under a second. If one fails, you hear it from the tool, not from Raju.

Those examples are **tests**. The tool is **Vitest**.

## Concept

```callout jargon
A **test** is a small piece of code that runs your code with one example input and checks the answer. A **unit test** tests one small unit, like one function. A **test runner** finds your tests, runs them and reports which passed. **Vitest** is the test runner that fits Vite projects.
```

You have been reading tests all course: every ticket's checks are Vitest tests. In this ticket you see how they are built, and you pick the examples yourself.

A test is just code that throws when the answer is wrong. Here is a tiny test runner, made by hand, so you can see there is no magic:

```js run
function it(name, fn) {
  try {
    fn();
    console.log("✓ " + name);
  } catch (error) {
    console.log("✗ " + name + ": " + error.message);
  }
}

function expect(actual) {
  return {
    toBe(expected) {
      if (actual !== expected) {
        throw new Error(`expected ${actual} to be ${expected}`);
      }
    },
  };
}

function deliveryFee(subtotal) {
  return subtotal >= 99900 ? 0 : 6000;
}

it("charges ₹60 on a ₹450 order", () => expect(deliveryFee(45000)).toBe(6000));
it("charges ₹60 on exactly ₹999", () => expect(deliveryFee(99900)).toBe(6000));
```

The second example catches a real bug: `>=` makes exactly ₹999 free. Vitest's `it` and `expect` work the same way, with far better reports.

### Syntax: a test file

```js
import { describe, it, expect } from "vitest";
import { thing } from "../src/thing.js";

describe("what you are testing", () => {
  it("does something, said as a sentence", () => {
    expect(thing(input), "a message for when it fails").toBe(expected);
  });
});
```

- `describe` groups tests under a name. It is optional, but it makes the report readable.
- `it` is one test. Its name reads as a sentence: "it charges ₹60 on a ₹450 order".
- `expect(actual)` takes the value your code gave. The **matcher** after it, like `.toBe(6000)`, says what it should be.
- The second argument to `expect` is a message, shown when the test fails. Write it for a teammate who has never seen the code.

Test files end in `.test.js`, like `delivery.test.js`. Vitest finds them by that ending.

### Arrange, act, assert

Most tests have three short parts:

```js
import { it, expect } from "vitest";
import { orderTotal } from "../src/cart.js";

it("adds gift wrap to the total", () => {
  const lines = [{ code: "AMI", paise: 45000, qty: 1 }];   // arrange: set up the input
  const total = orderTotal(lines, { giftWrap: true });     // act: call the code
  expect(total, "₹450 plus ₹30 gift wrap").toBe(48000);    // assert: check the answer
});
```

One test, one idea. If a test checks five different things, its name cannot say which one broke.

### Syntax: matchers

```js
expect(actual).toBe(expected);
```

The matcher picks how to compare. These cover most tests:

| Matcher | Passes when | Example |
|---|---|---|
| `toBe(x)` | `actual === x` (same value, or the very same object) | `expect(fee).toBe(6000)` |
| `toEqual(x)` | same shape and contents, even if it is a different object | `expect(line).toEqual({ code: "KEY", qty: 2 })` |
| `toThrow(Type)` | calling the function throws that kind of error | `expect(() => deliveryFee(-1)).toThrow(RangeError)` |
| `toBeGreaterThan(n)` | `actual > n` | `expect(cases.length).toBeGreaterThan(3)` |
| `not.toBe(x)` | the opposite of `toBe` | `expect(name).not.toBe("")` |

```callout warning
For `toThrow`, wrap the call in an arrow function: `expect(() => deliveryFee(-1))`. Without the arrow, `deliveryFee(-1)` throws before `expect` even starts, and the test crashes instead of passing.
```

Two objects with the same contents are not `toBe` each other, because they are two different objects. Use `toEqual` for objects and arrays.

### Choosing good cases

A test only catches the bugs its examples reach. The two bugs in the ticket hid in places most people never try: exactly ₹999, and the size of the number.

```callout jargon
A **boundary** is the value where the answer changes, like 99900 paise for free delivery. An **edge case** is an input at the very edge of what is allowed, like an empty cart.
```

For any rule, pick cases from each of these:

- **A normal case:** one ₹450 amigurumi.
- **Each side of every boundary:** exactly 99900, and 99901.
- **The edges:** an empty cart (0), a very big order.
- **Bad input:** a negative number, or 450.5 paise.

Picking real amounts also catches typos. A ₹150 coaster set pays 6000 paise. A function that returns 60 fails that case at once.

### Syntax: a table of cases

```js
it.each(cases)("$name", ({ subtotal, expected }) => {
  expect(deliveryFee(subtotal)).toBe(expected);
});
```

When many tests have the same shape, write the examples as data, in an array:

```js
export const cases = [
  { name: "a ₹150 coaster set pays ₹60", subtotal: 15000, expected: 6000 },
  { name: "exactly ₹999 still pays ₹60", subtotal: 99900, expected: 6000 },
];
```

`it.each(cases)` makes one test per object. `$name` in the test's name is replaced by each case's `name`. Adding a test is now adding one line.

### Do your tests catch bugs?

A test that passes on broken code is worse than no test: it makes you feel safe. One way to check your tests is to try them against broken versions of the code on purpose.

```callout jargon
A **mutant** is a copy of your code with one small mistake put in on purpose, like `>=` instead of `>`. If a test fails on the mutant, the test **catches** it. A test suite that catches every mutant is strong.
```

This ticket's checks do exactly that with your table of cases:

```animate
title: Your cases against the broken versions
kind: scene
nodes:
  - { id: cases, label: Your cases, emoji: "📋", value: "0, 15000, 99900, 99901", row: 0, col: 1 }
  - { id: good, label: Correct fee, emoji: "✅", row: 1, col: 0 }
  - { id: ge, label: ">= instead of >", emoji: "🐛", row: 1, col: 1 }
  - { id: rupees, label: "60, not 6000", emoji: "🐛", row: 1, col: 2 }
  - { id: never, label: Never free, emoji: "🐛", row: 1, col: 3 }
steps:
  - say: First, every case must pass on the correct fee. Otherwise the case itself is wrong.
    send: { from: cases, to: good, label: run all }
    set: { good: all pass }
  - say: Now the mutant that makes exactly ₹999 free. The 99900 case expects 6000 and gets 0. Caught.
    send: { from: cases, to: ge, label: run all }
    set: { ge: caught by 99900 }
  - say: The mutant that charges 60 paise. The 15000 case expects 6000 and gets 60. Caught.
    send: { from: cases, to: rupees, label: run all }
    set: { rupees: caught by 15000 }
  - say: The mutant that is never free. The 99901 case expects 0 and gets 6000. Caught.
    send: { from: cases, to: never, label: run all }
    set: { never: caught by 99901 }
  - say: Every mutant fails at least one case. These four lines would have caught both of this month's bugs.
    focus: [cases]
```

### Syntax: running the tests

```text
npx vitest
npx vitest run
```

Plain `npx vitest` starts **watch mode**: it runs the tests, then waits, and runs them again every time you save. `vitest run` runs them once and stops, which is what a CI robot needs. Most projects have `"test": "vitest run"` in their scripts, so `npm test` does it.

When everything passes, the report is short:

```text
 ✓ test/delivery.test.js (8 tests) 4ms

 Test Files  1 passed (1)
      Tests  8 passed (8)
```

When a test fails, Vitest names it, shows your message, and shows what it expected and what it got:

```text
 FAIL  test/delivery.test.js > deliveryFee > charges ₹60 on a ₹450 order
AssertionError: one amigurumi (45000 paise) pays 6000 paise delivery: expected 60 to be 6000 // Object.is equality

- Expected
+ Received

- 6000
+ 60
```

`Object.is equality` is how `toBe` compares; read it as `===`. Your message comes first, so whoever reads the report knows what the rule was.

## Worked example

Here is one test of the delivery rule, read line by line. It is from this ticket's checks.

```js
import { describe, it, expect } from "vitest";
import { deliveryFee } from "../src/delivery.js";

describe("deliveryFee", () => {
  it("still charges ₹60 on exactly ₹999, because free delivery starts above ₹999", () => {
    expect(deliveryFee(99900), "99900 paise is not above ₹999, so delivery is 6000").toBe(6000);
  });

  it("refuses a negative subtotal", () => {
    expect(() => deliveryFee(-6000), "a negative subtotal should throw a RangeError").toThrow(RangeError);
  });
});
```

1. `import { describe, it, expect } from "vitest"` brings in Vitest's three main tools.
2. `import { deliveryFee } from "../src/delivery.js"` loads your function. Tests sit in `test/`, next to `src/`, so the path starts with `..`.
3. The first `it` sits right on the boundary. Its name says the rule, and its message says why 6000 is right.
4. The second `it` wraps the call in `() =>`, so `toThrow` can catch the error.

Now your fix, in two files:

- `src/delivery.js`: write `deliveryFee`. An empty cart costs nothing. Up to and including ₹999, delivery is ₹60. Above ₹999, it is free. A negative or non-whole subtotal throws a `RangeError`.
- `src/deliveryCases.js`: write at least four cases. The checks run them on the correct fee, on your own `deliveryFee`, and on five broken versions. Each broken version must fail at least one of your cases.

## Try it

Press Run on the hand-made test runner at the top of the Concept.

1. Change `>=` to `>` in its `deliveryFee`. Run again: both lines should show ✓.
2. Add a third `it` for an empty cart: `deliveryFee(0)` should be `0`. Run it. Does this `deliveryFee` pass?
3. Change `toBe`'s check to `actual != expected`, and test `expect("6000").toBe(6000)`. Why does it now pass, and why is that bad?

## Recap

- A test runs your code with one example and checks the answer. Vitest finds files ending in `.test.js` and runs them.
- `describe` groups tests, `it` is one test named as a sentence, and `expect(actual, message)` with a matcher checks the answer.
- `toBe` compares with `===`, `toEqual` compares contents, and `toThrow` needs the call wrapped in `() =>`.
- Good cases sit on both sides of every boundary and at the edges, with real amounts. `it.each` turns a table of cases into tests.
- A good test fails on broken code. `npx vitest` watches, and `vitest run` runs once.

## Common mistakes

**1. Calling the function inside expect when testing a throw.**

```js
expect(deliveryFee(-6000)).toThrow(RangeError);
```

What you see: the test fails with the `RangeError` itself, as if the code were broken. `deliveryFee(-6000)` threw before `expect` could catch it.

Fix: wrap the call in an arrow function: `expect(() => deliveryFee(-6000)).toThrow(RangeError)`.

**2. Comparing objects with toBe.**

```js
expect(firstLine(cart)).toBe({ code: "KEY", qty: 2 });
```

What you see: `AssertionError: expected { code: 'KEY', qty: 2 } to be { code: 'KEY', qty: 2 }`, with a hint that the values are equal but not the same object.

Fix: use `toEqual` for objects and arrays. It compares the contents.

**3. Only testing the easy middle.**

```js
export const cases = [
  { name: "a ₹450 order pays ₹60", subtotal: 45000, expected: 6000 },
  { name: "a ₹2,000 order is free", subtotal: 200000, expected: 0 },
];
```

What you see: both pass, on the correct code and on the `>=` mutant. Exactly ₹999 slips through again.

Fix: add a case on each side of every boundary: 99900 and 99901. Add an empty cart too.

**4. Copying the code's answer into the test.**

You run `deliveryFee(99900)`, see `0`, and write `expected: 0` into the case.

What you see: a green test that checks the bug is still there.

Fix: work out the expected answer from the rule, not from the code. "Above ₹999" means 99900 pays ₹60, so `expected: 6000`.
