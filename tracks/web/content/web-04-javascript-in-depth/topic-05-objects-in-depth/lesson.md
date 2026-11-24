## Hook

Raju writes order-17 in the order book. Later he wants a similar order, so he points at the same page and says "that one, but with 3". If he writes the 3 on that page, order-17 changes too. He needed a photocopy, not a second finger pointing at the same page.

JavaScript objects work like that page. This lesson shows how objects carry their own functions, how `this` finds the right object, and how to make a real copy.

## Concept

### Syntax: methods

```js
const basket = {
  owner: "Asha",
  hello() {
    return "Hello, " + this.owner;
  },
};
```

A method is a function stored as a property of an object. `hello() { ... }` is the short way to write `hello: function () { ... }`. You call it with a dot: `basket.hello()`.

```callout jargon
A **method** is a function that lives on an object. It usually works with that object's own data.
```

### this: the object before the dot

Inside a method, `this` means the object the method was called on. It lets one method work for every basket.

```js run
const basket = {
  owner: "Asha",
  lines: [],
  add(code, paise) {
    this.lines.push({ code: code, paise: paise });
    return this;
  },
  count() {
    return this.lines.length;
  },
};
basket.add("CST", 15000).add("KEY", 12000);
console.log(basket.owner + " has " + basket.count() + " lines");
```

It prints `Asha has 2 lines`. `add` returns `this`, the basket itself, so a second `.add(...)` can follow straight on. That is called chaining.

```callout jargon
**this** is a special word inside a function. In a method call like `basket.add(...)`, it means `basket`, the object before the dot.
```

### When this gets lost

`this` is decided at the moment of the call, by what is before the dot. If you take a method off its object and call it alone, nothing is before the dot.

```js
const count = basket.count;
count(); // TypeError: Cannot read properties of undefined (reading 'lines')
```

Arrow functions have no `this` of their own. They use the `this` of the code around them. So never write a method as an arrow function; use the `count() { }` form.

### Objects are shared by reference

A variable doesn't hold an object. It holds a reference: an arrow pointing to the object. Copying the variable copies the arrow, not the object. Watch both variables point at one order.

```animate
title: Two names, one order
kind: code
language: js
code: |
  const order17 = { id: "order-17", qty: 1 };
  const order18 = order17;
  order18.qty = 3;
  console.log(order17.qty);
  const order19 = { ...order17 };
  order19.qty = 5;
  console.log(order17.qty, order19.qty);
steps:
  - line: 1
    objects: { o1: { type: Object, fields: { id: '"order-17"', qty: 1 } } }
    vars: { order17: "@o1" }
    say: order17 points to a new object.
  - line: 2
    vars: { order18: "@o1" }
    say: This copies the arrow, not the object. Both names now point at the same order.
  - line: 3
    objects: { o1: { type: Object, fields: { id: '"order-17"', qty: 3 } } }
    say: Changing qty through order18 changes the one shared object.
  - line: 4
    out: "3"
    say: So order17 shows 3 as well. This is Raju's bug.
  - line: 5
    objects: { o2: { type: Object, fields: { id: '"order-17"', qty: 3 } } }
    vars: { order19: "@o2" }
    say: Spread builds a second object and copies the properties into it.
  - line: 6
    objects: { o2: { type: Object, fields: { id: '"order-17"', qty: 5 } } }
    say: Now a change through order19 only touches the copy.
  - line: 7
    out: "3 5"
    say: The original keeps 3, and the copy has 5.
```

```callout jargon
A **reference** is a pointer to an object in memory. Objects and arrays are passed around by reference, so two variables can point to the same one.
```

### Comparing objects with ===

`===` on two objects asks "are these the same object?", not "do they look the same?".

```js run
const a = { code: "CST" };
const b = { code: "CST" };
const c = a;
console.log(a === b);
console.log(a === c);
console.log(a.code === b.code);
```

It prints `false`, `true`, `true`. `a` and `b` look alike but are two objects. To compare contents, compare the fields, like an id.

### Syntax: shallow copy

```js
const copy = { ...order };
```

Spread copies the top level. But if a property holds another object or array, the copy gets the same reference to it. The inner things are still shared.

```js run
const order17 = { id: "order-17", lines: [{ code: "AMI", qty: 2 }] };
const order18 = { ...order17, id: "order-18" };
console.log(order18.lines === order17.lines);
```

It prints `true`. Both orders hold the very same `lines` array.

```callout jargon
A **shallow copy** copies only the top level of an object. Objects inside it are shared with the original. A **deep copy** copies every level, so nothing is shared.
```

### Syntax: deep copy

```js
const copy = structuredClone(order);
```

`structuredClone` copies every level. It works for plain data: objects, arrays, text, numbers and dates. It can't copy functions.

You can also copy by hand, level by level. This shows exactly what is new:

```js run
const order17 = { id: "order-17", lines: [{ code: "AMI", qty: 2 }] };
const order18 = {
  ...order17,
  id: "order-18",
  lines: order17.lines.map((line) => ({ ...line })),
};
order18.lines[0].qty = 3;
console.log(order17.lines[0].qty, order18.lines[0].qty);
```

It prints `2 3`. The new `lines` array holds new line objects, so the change stays in order-18.

## Worked example

A basket with methods, and a safe copy of an order.

```js run
function createBasket(owner) {
  return {
    owner: owner,
    lines: [],
    add(code, paise, qty) {
      this.lines.push({ code: code, paise: paise, qty: qty });
      return this;
    },
    count() {
      return this.lines.reduce((sum, line) => sum + line.qty, 0);
    },
  };
}

const asha = createBasket("Asha");
const ravi = createBasket("Ravi");
asha.add("AMI", 45000, 2).add("KEY", 12000, 1);
ravi.add("BAG", 89900, 1);
console.log(asha.owner + " has " + asha.count() + " in the basket");
console.log(ravi.owner + " has " + ravi.count() + " in the basket");

const order17 = { id: "order-17", customer: { name: "Mr. Fernandes" }, lines: [{ code: "AMI", qty: 2 }] };
const order18 = structuredClone(order17);
order18.id = "order-18";
order18.lines[0].qty = 3;
console.log(order17.id, order17.lines[0].qty);
console.log(order18.id, order18.lines[0].qty);
```

Line by line:

1. `createBasket` returns a new object each time, with its own empty `lines` array.
2. `asha.add(...)` runs with `this` as `asha`, so the lines go into Asha's basket only. Each `add` returns `this`, so the calls chain.
3. `count` adds up the quantities: `Asha has 3 in the basket`, and `Ravi has 1 in the basket`.
4. `structuredClone` copies order-17 at every level, including the customer and the lines.
5. Changing order-18 leaves order-17 alone: it still prints `order-17 2`, then `order-18 3`.

## Try it

Add a `total()` method to the basket that returns the paise for all lines. Then make `order18` with `{ ...order17 }` instead of `structuredClone`, and see what happens to order-17's quantity.

```js run
function createBasket(owner) {
  return {
    owner: owner,
    lines: [],
    add(code, paise, qty) {
      this.lines.push({ code: code, paise: paise, qty: qty });
      return this;
    },
  };
}
const asha = createBasket("Asha");
asha.add("AMI", 45000, 2);
console.log(asha.lines.length);
```

## Recap

- A method is a function on an object. Write it as `name() { ... }` and call it with a dot.
- Inside a method, `this` is the object before the dot. Taking the method away from its object loses `this`, and arrow functions never have their own.
- Variables hold references to objects. `=` copies the reference, so both names change the same object, and `===` checks for the same object.
- `{ ...obj }` is a shallow copy: inner objects are still shared.
- `structuredClone(obj)`, or copying each level by hand, makes a deep copy.

## Common mistakes

**1. Writing a method as an arrow function.**

```js
const basket = {
  owner: "Asha",
  hello: () => "Hello, " + this.owner,
};
basket.hello();
```

In a module file, this throws `TypeError: Cannot read properties of undefined (reading 'owner')`. The arrow took `this` from the file around it, not from `basket`. Fix: write `hello() { return "Hello, " + this.owner; }`.

**2. Copying an object with =.**

```js run
const order17 = { id: "order-17", qty: 1 };
const order18 = order17;
order18.id = "order-18";
console.log(order17.id);
```

It prints `order-18`. There was only ever one object. Fix: `const order18 = { ...order17, id: "order-18" };`.

**3. Trusting spread to copy nested data.**

```js run
const order17 = { id: "order-17", lines: [{ code: "AMI", qty: 1 }] };
const order18 = { ...order17, id: "order-18" };
order18.lines[0].qty = 3;
console.log(order17.lines[0].qty);
```

It prints `3`. The `lines` array is shared, so order-17 changed. Fix: `structuredClone(order17)`, or copy the lines too with `lines: order17.lines.map((line) => ({ ...line }))`.

**4. Comparing two objects with ===.**

```js run
const saved = { code: "CST", qty: 2 };
const typed = { code: "CST", qty: 2 };
console.log(saved === typed);
```

It prints `false`, though they look the same. Fix: compare the fields that matter, such as `saved.code === typed.code && saved.qty === typed.qty`.
