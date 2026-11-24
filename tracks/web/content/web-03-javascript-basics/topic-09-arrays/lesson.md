## Hook

A WhatsApp order arrives, and Raju writes it on his notepad as a list: CST, KEY, KEY, AMI. Each line has its place. He can read the second line, cross off the last one, or count how many keychains are on it.

You met arrays briefly in the loops topic. This ticket shows everything a basket of item codes needs: adding, removing, finding, counting and printing.

## Concept

```callout jargon
An **array** is an ordered list of values kept in one box. Each value is called an **element**, and has a numbered position.
```

### Syntax: an array

```js
const basket = ["CST", "KEY", "AMI"];
```

Square brackets make an array, with commas between the elements. An array can hold strings, numbers, or anything else. `[]` is an empty array.

```js
const prices = [15000, 12000, 45000];
const empty = [];
```

### Syntax: reading by index

```js
const first = basket[0];
```

Like the characters of a string, elements are numbered from 0. `basket[0]` is the first element.

```js run
const basket = ["CST", "KEY", "AMI"];
console.log(basket[0]);
console.log(basket[2]);
console.log(basket[3]);
console.log(basket.length);
console.log(basket[basket.length - 1]);
```

This prints `CST`, `AMI`, `undefined`, `3` and `AMI`. There's no position 3, so reading it gives `undefined` instead of an error. The last element always sits at `length - 1`.

### Syntax: changing an element

```js
basket[1] = "BAG";
```

Assigning to a position replaces the element there. The array keeps its length.

### Syntax: push and pop

```js
basket.push("BTY");
```

- `push(x)` adds `x` to the end of the array.
- `pop()` removes the last element and hands it back.

Both change the array itself. Watch the basket grow:

```animate
title: Filling the basket
kind: code
language: js
code: |
  const basket = ["CST"];
  basket.push("KEY");
  basket.push("AMI");
  console.log(basket.length);
  console.log(basket[1]);
  console.log(basket.includes("BAG"));
steps:
  - line: 1
    objects: { b1: { type: Array, fields: { "0": '"CST"' } } }
    vars: { basket: "@b1" }
    say: An array with one coaster set in it. The box basket points to the list.
  - line: 2
    objects: { b1: { type: Array, fields: { "0": '"CST"', "1": '"KEY"' } } }
    say: push adds a keychain at the end, at position 1.
  - line: 3
    objects: { b1: { type: Array, fields: { "0": '"CST"', "1": '"KEY"', "2": '"AMI"' } } }
    say: Another push adds an amigurumi at position 2. basket is a const, but the list it points to can still grow.
  - line: 4
    out: "3"
    say: length counts the elements. There are 3 now.
  - line: 5
    out: KEY
    say: Position 1 is the second element, because counting starts at 0.
  - line: 6
    out: "false"
    say: includes looks through the whole list. There's no tote bag in it.
```

```js run
const basket = ["CST", "KEY"];
basket.push("BAG");
console.log(basket.length);
const last = basket.pop();
console.log(last);
console.log(basket.length);
```

This prints `3`, then `BAG`, then `2`.

### Syntax: includes and indexOf

```js
const hasKey = basket.includes("KEY");
```

- `includes(x)` is `true` when `x` is somewhere in the array.
- `indexOf(x)` gives the position of the first `x`, or `-1` when it isn't there.

```js run
const basket = ["CST", "KEY", "KEY"];
console.log(basket.includes("KEY"));
console.log(basket.includes("key"));
console.log(basket.indexOf("KEY"));
console.log(basket.indexOf("BAG"));
```

This prints `true`, `false`, `1` and `-1`. Both compare with `===`, so `"key"` doesn't match `"KEY"`. `indexOf` stops at the first match, so it can't count repeats. A loop can.

### Syntax: join and slice

```js
const line = basket.join(", ");
```

- `join(sep)` glues every element into one string, with `sep` between them.
- `slice(a, b)` makes a new array from position `a` up to, but not including, `b`. It doesn't change the original.

```js run
const basket = ["CST", "KEY", "AMI", "BAG"];
console.log(basket.join(" + "));
console.log(basket.slice(1, 3).join(", "));
console.log(basket.length);
```

This prints `CST + KEY + AMI + BAG`, then `KEY, AMI`, then `4`. The basket still has all 4 elements after `slice`.

### Printing an array

`console.log` shows an array with brackets and quotes, which is handy for checking your work:

```js run
const basket = ["CST", "KEY"];
console.log(basket);
console.log(String(basket));
```

This prints `[ 'CST', 'KEY' ]`, then `CST,KEY`. For a customer, use `join` and choose the separator yourself.

### Two names, one array

A box doesn't hold the array itself. It holds a **reference**: an arrow pointing to the array. Copying the box copies the arrow, not the list.

```js run
const basket = ["CST"];
const sameBasket = basket;
sameBasket.push("KEY");
console.log(basket.length);
console.log(["CST"] === ["CST"]);
```

This prints `2`, then `false`. Both names point to one array, so a push through either one shows in both. And `===` on arrays asks "is this the very same array?", not "do they hold the same things?". Two separate lists are never `===`.

```callout jargon
A **reference** is an arrow from a box to an array or object somewhere in memory. Two boxes can point to the same array.
```

### const arrays can still change

`const basket` means the box will always point to the same array. It doesn't freeze the array, so `push`, `pop` and `basket[1] = "BAG"` all still work. What you can't do is point `basket` at a different array.

## Worked example

Price a WhatsApp basket using two arrays side by side:

```js run
const codes = ["CST", "KEY", "AMI", "BAG", "BTY"];
const prices = [15000, 12000, 45000, 89900, 35000];
const basket = ["AMI", "KEY", "KEY"];
let total = 0;
for (const code of basket) {
  const position = codes.indexOf(code);
  total = total + prices[position];
}
console.log(basket.join(", "));
console.log(total);
```

1. `codes` and `prices` line up: the price of `codes[2]` is `prices[2]`.
2. For each code in the basket, `indexOf` finds its position in `codes`.
3. `"AMI"` is at position 2, so it adds `prices[2]`, which is 45000. Each `"KEY"` adds 12000.
4. It prints `AMI, KEY, KEY`, then `69000`.

Keeping two lists in line like this is fragile. The next topic shows a neater way.

## Try it

Run it. Then push `"BAG"` before the first `console.log`, and finally `pop()` it off again.

```js run
const basket = ["CST", "KEY"];
basket.push("AMI");
console.log(basket.join(", "));
console.log(basket.length + " items");
console.log(basket.includes("BAG") ? "Has a tote bag" : "No tote bag");
```

## Recap

- An array is a list in square brackets, like `["CST", "KEY"]`, numbered from 0.
- `length` counts the elements, and the last one is at `length - 1`.
- `push` adds to the end and `pop` removes from the end. Both change the array.
- `includes` checks for a value, `indexOf` finds its position or `-1`, and `join` makes one string.
- A box holds a reference to an array, so two names can share one array, and `===` compares references.

## Common mistakes

**1. Reading one past the end.**

```js
const basket = ["CST", "KEY"];
console.log(basket[basket.length]);
```

This prints `undefined`. The positions are 0 and 1, but `length` is 2. The last element is `basket[basket.length - 1]`.

**2. Comparing two arrays with ===.**

```js
const a = ["CST"];
const b = ["CST"];
console.log(a === b);
```

This prints `false`, even though they hold the same code. `===` checks whether both boxes point to the very same array. Compare the elements instead, for example with `a.join() === b.join()`.

**3. Pointing a const at a new array.**

```js
const basket = ["CST"];
basket = ["CST", "KEY"];
```

JavaScript says `TypeError: Assignment to constant variable.` Change the array that's already there with `basket.push("KEY")`.

**4. Forgetting that includes is exact.**

```js
const basket = ["KEY"];
console.log(basket.includes("key"));
```

This prints `false`. Clean typed codes first, for example with `trim()` and `toUpperCase()`, before you search.
