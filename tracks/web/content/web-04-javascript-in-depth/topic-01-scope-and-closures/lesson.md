## Hook

The stall keeps its order counter in a notebook on the counter. Anyone walking past can pick up the pen and write a new number. One day someone writes 17 again, and two customers get the same order number.

The fix is a locked box with a slot. You can ask it for the next number, but you can't reach inside. In JavaScript, that locked box is called a closure. This lesson shows how to build one.

## Concept

### Scope: where a name can be seen

Every variable lives somewhere in your code. The part of the code that can see it is its scope.

```callout jargon
**Scope** is the part of a program where a variable's name can be used. Outside its scope, the name means nothing.
```

A variable made at the top of a file, outside every function and block, can be seen almost everywhere. That sounds handy. It is also why Dhaaga's counter got reset: any code could change it.

### Syntax: block scope

```js
{
  const label = "Golu the elephant";
}
```

A block is the code between `{` and `}`. Variables made with `let` or `const` inside a block exist only inside it. The blocks of `if`, `for` and `while` work the same way.

```js run
const paise = 45000;
if (paise > 40000) {
  const label = "premium";
  console.log(label + " item");
}
console.log("Price: " + paise);
```

`label` is born when the block starts and gone when it ends. `paise` was made outside, so both lines can use it.

```callout jargon
A **block** is a group of statements inside curly braces `{ }`. `let` and `const` are **block-scoped**: they belong to the nearest block around them.
```

### Function scope: fresh variables every call

A function body is a block too. Its parameters and the variables made inside it belong to that call only.

```js run
function lineTotal(paise, qty) {
  const total = paise * qty;
  return total;
}
console.log(lineTotal(15000, 2));
console.log(lineTotal(89900, 1));
```

It prints `30000`, then `89900`. Each call gets its own `paise`, `qty` and `total`. When the call ends, the outside world can't see them.

### The scope chain: looking outward

Code inside a block can see its own variables, and every variable in the blocks around it. It can't look inward, into a smaller block.

```js run
const shop = "Aundh shop";
function welcome(name) {
  const message = "Welcome to the " + shop + ", " + name + "!";
  return message;
}
console.log(welcome("Asha"));
```

It prints `Welcome to the Aundh shop, Asha!`. JavaScript looked for `shop` inside `welcome`, didn't find it, and looked one level out.

```callout jargon
The **scope chain** is the path JavaScript follows to find a name: first the current block, then the block around it, out to the top of the file.
```

### Closures: a function remembers where it was born

A function can make and return another function. The inner function keeps access to the outer function's variables, even after the outer function has finished. Watch it happen.

```animate
title: A counter that remembers
kind: code
language: js
code: |
  function makeCounter(start) {
    let next = start;
    return function () {
      const id = next;
      next = next + 1;
      return id;
    };
  }
  const ticket = makeCounter(17);
  console.log(ticket());
  console.log(ticket());
steps:
  - line: 9
    vars: { start: 17 }
    say: We call makeCounter with 17.
  - line: 2
    vars: { next: 17 }
    say: Inside, a new variable called next starts at 17.
  - line: 3
    say: makeCounter returns the inner function. It doesn't run it yet.
  - line: 9
    vars: { start: ~, ticket: "(function)" }
    say: makeCounter has finished. But the function in ticket still holds on to next. That link is the closure.
  - line: 4
    vars: { id: 17 }
    say: Calling ticket() runs the inner function. It reads next from the scope it was born in.
  - line: 5
    vars: { next: 18 }
    say: It adds 1 to next. That changes the remembered variable itself, not a copy.
  - line: 10
    vars: { id: ~ }
    out: "17"
    say: The call returns 17, and console.log prints it.
  - line: 4
    vars: { id: 18 }
    say: The second call finds next at 18, because the last call changed it.
  - line: 5
    vars: { next: 19 }
    say: next moves on to 19, ready for the next order.
  - line: 11
    vars: { id: ~ }
    out: "18"
    say: The second call prints 18. No other code in the file can reach next.
```

```callout jargon
A **closure** is a function together with the variables it can see from where it was made. The function keeps those variables alive for as long as the function exists.
```

### Private state: variables nobody else can touch

The variable `next` above has no name anywhere outside `makeCounter`. Only the returned function can read or change it. That makes it **private state**.

```callout jargon
**State** is data a program remembers between steps, like a count or a total. **Private** state can only be changed through the functions you hand out.
```

To hand out more than one function, return an object that holds them. Each function in the object shares the same private variables.

### Every call makes a new closure

Each call to the outer function makes a fresh set of variables. Two counters made by two calls never share a number.

```js run
function makeTally(place) {
  let count = 0;
  return function () {
    count = count + 1;
    return place + ": " + count;
  };
}
const stall = makeTally("FC Road stall");
const shop = makeTally("Aundh shop");
console.log(stall());
console.log(stall());
console.log(shop());
```

The stall reaches 2 while the shop is still on 1. Each has its own `count`.

### var: the old way, and its loop trap

Older code uses `var`. A `var` ignores blocks: it belongs to the whole function, or the whole file. In a loop, that means every turn shares one variable.

```js run
const withVar = [];
for (var i = 1; i <= 3; i++) {
  withVar.push(function () { return i; });
}
const withLet = [];
for (let j = 1; j <= 3; j++) {
  withLet.push(function () { return j; });
}
console.log(withVar[0](), withVar[1](), withVar[2]());
console.log(withLet[0](), withLet[1](), withLet[2]());
```

It prints `4 4 4`, then `1 2 3`. The three `var` functions all see the one `i`, which ended at 4. With `let`, each turn of the loop gets its own `j`.

```callout tip
Use `const` by default, `let` when the value must change, and never `var` in new code.
```

## Worked example

A yarn ball tracker keeps the grams left for one colour. Nothing outside can set the grams by mistake.

```js run
function makeYarnBall(colour, grams) {
  let left = grams;
  return {
    use: function (g) {
      if (g > left) {
        return false;
      }
      left = left - g;
      return true;
    },
    report: function () {
      return colour + ": " + left + " g";
    },
  };
}

const marigold = makeYarnBall("Marigold", 100);
const indigo = makeYarnBall("Indigo", 50);
console.log(marigold.use(40));
console.log(indigo.use(80));
console.log(marigold.report());
console.log(indigo.report());
console.log(marigold.left);
```

Line by line:

1. `makeYarnBall` makes a private `left` from `grams`, then returns an object with two functions.
2. `marigold.use(40)` has enough yarn, so `left` drops to 60 and it prints `true`.
3. `indigo.use(80)` asks for more than 50 g, so it prints `false` and changes nothing.
4. The reports print `Marigold: 60 g` and `Indigo: 50 g`. Each ball has its own `left`.
5. `marigold.left` prints `undefined`. The object has no property called `left`. The variable is hidden in the closure.

## Try it

Add a third call to `shop()`, then predict what all four lines print. Next, make `makeTally` return an object with `add` and `reset` functions that share the same `count`.

```js run
function makeTally(place) {
  let count = 0;
  return function () {
    count = count + 1;
    return place + ": " + count;
  };
}
const stall = makeTally("FC Road stall");
const shop = makeTally("Aundh shop");
console.log(stall());
console.log(stall());
console.log(shop());
```

## Recap

- Scope is where a name can be seen. `let` and `const` belong to the nearest block, and a function call gets fresh variables.
- Code looks outward along the scope chain to find a name, never inward.
- A closure is a function that keeps the variables from where it was made, even after the outer function returned.
- Closures give you private state: only the functions you hand out can change it.
- `var` ignores blocks, so loop functions all share one variable. Use `let` and `const`.

## Common mistakes

**1. Using a variable outside its block.**

```js run crash
const paise = 89900;
if (paise > 50000) {
  const label = "big order";
}
console.log(label);
```

Node stops with `ReferenceError: label is not defined`. `label` died when the `if` block ended. Fix: declare it before the block with `let label = "";`, then assign it inside.

**2. Putting the counter inside the inner function.**

```js run
function makeOrderNumbers(start) {
  return function () {
    let next = start;
    const id = "order-" + next;
    next = next + 1;
    return id;
  };
}
const nextOrder = makeOrderNumbers(17);
console.log(nextOrder());
console.log(nextOrder());
```

It prints `order-17` twice. Each call of the inner function makes a brand new `next`. Fix: move `let next = start;` up into `makeOrderNumbers`, outside the returned function.

**3. Using var in a loop that makes functions.**

```js
const makers = [];
for (var i = 0; i < 3; i++) {
  makers.push(function () { return i; });
}
```

Every function returns `3`, the value `i` had when the loop ended. Fix: write `let i`, so each turn of the loop has its own `i`.
