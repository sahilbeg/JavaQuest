## Hook

Meera Aunty has one written pattern for the Golu elephant. From that one pattern, she has crocheted hundreds of elephants. Each elephant is its own toy, with its own stitches. But they all follow the same plan.

In the last ticket, `createBasket` was a homemade pattern. JavaScript has a proper one built in: the class. You write the plan once, then make as many carts as you need.

## Concept

### Syntax: class

```js
class Cart {}
const cart = new Cart();
```

A class is a blueprint for objects. `new Cart()` builds one new object from it. Class names start with a capital letter, so you can spot them.

```callout jargon
A **class** is a blueprint that describes what its objects hold and what they can do. An **instance** is one object built from it with `new`.
```

### Syntax: constructor

```js
class Cart {
  constructor(customer) {
    this.customer = customer;
  }
}
```

The constructor is a special method that runs once, when `new` builds the object. Inside it, `this` is the brand new object. Whatever you put on `this` becomes that object's data.

```js run
class Cart {
  constructor(customer) {
    this.customer = customer;
    this.items = 0;
  }
}
const asha = new Cart("Asha");
const ravi = new Cart("Ravi");
console.log(asha.customer, ravi.customer, asha.items);
```

It prints `Asha Ravi 0`. Two calls to `new`, two separate objects.

```callout jargon
A **constructor** is the method that sets up a new instance. Its arguments come from the brackets after `new Cart(...)`.
```

### Methods in a class

Methods go inside the class body, with no commas between them. Every instance can use them, and `this` is the instance the method was called on.

Watch `new` and a method call happen step by step.

```animate
title: new builds an object, and a method changes it
kind: code
language: js
code: |
  class StockItem {
    constructor(id, stock) {
      this.id = id;
      this.stock = stock;
    }
    sell() {
      this.stock = this.stock - 1;
    }
  }
  const golu = new StockItem("DC-1042", 3);
  golu.sell();
  console.log(golu.id, golu.stock);
steps:
  - line: 10
    objects: { o1: { type: StockItem, fields: {} } }
    say: new makes an empty StockItem object, then calls the constructor with it as this.
  - line: 3
    vars: { this: "@o1", id: '"DC-1042"', stock: 3 }
    objects: { o1: { type: StockItem, fields: { id: '"DC-1042"' } } }
    say: The constructor stores the id on the new object.
  - line: 4
    objects: { o1: { type: StockItem, fields: { id: '"DC-1042"', stock: 3 } } }
    say: Then it stores the stock. The object is ready.
  - line: 10
    vars: { this: ~, id: ~, stock: ~, golu: "@o1" }
    say: new hands back the finished object, and golu points to it.
  - line: 7
    vars: { this: "@o1" }
    objects: { o1: { type: StockItem, fields: { id: '"DC-1042"', stock: 2 } } }
    say: golu.sell() runs sell with this as golu, so golu's stock drops to 2.
  - line: 12
    vars: { this: ~ }
    out: "DC-1042 2"
    say: The object kept its new stock after the method finished.
```

### Syntax: getters

```js
class Cart {
  get total() {
    return 0;
  }
}
```

A getter is a method you read like a property, without brackets. Use one for a value worked out from other data, like a cart's total.

```js run
class Line {
  constructor(paise, qty) {
    this.paise = paise;
    this.qty = qty;
  }
  get total() {
    return this.paise * this.qty;
  }
}
const coasters = new Line(15000, 2);
console.log(coasters.total);
coasters.qty = 3;
console.log(coasters.total);
```

It prints `30000`, then `45000`. The getter works the value out fresh every time you read it, so it is never out of date.

```callout jargon
A **getter** is a method marked with `get`. Reading `obj.total` calls it and gives back what it returns.
```

### Syntax: private fields

```js
class Cart {
  #lines = [];
}
```

A field whose name starts with `#` is private. Only code inside the class body can read or change it. Any other code that tries gets a `SyntaxError` before the program even starts.

```js run
class YarnBall {
  #grams;
  constructor(colour, grams) {
    this.colour = colour;
    this.#grams = grams;
  }
  use(g) {
    if (g > this.#grams) {
      return false;
    }
    this.#grams = this.#grams - g;
    return true;
  }
  get grams() {
    return this.#grams;
  }
}
const mint = new YarnBall("Mint", 100);
console.log(mint.use(40), mint.use(80), mint.grams);
console.log(Object.keys(mint).join(", "));
```

It prints `true false 60`, then `colour`. The grams can only change through `use`, so no page can set them to a wrong number. Private fields don't even show up in `Object.keys`.

```callout jargon
A **private field** is a property whose name starts with `#`. Only the class's own methods can reach it. It is the class version of the closure trick from the first ticket.
```

### A class is still objects underneath

An instance is an ordinary object. You can read its public fields with a dot and check where it came from with `instanceof`.

```js run
class Cart {
  constructor(customer) {
    this.customer = customer;
  }
}
const cart = new Cart("Asha");
console.log(typeof cart);
console.log(cart instanceof Cart);
console.log(cart.customer);
```

It prints `object`, `true` and `Asha`.

## Worked example

A small order-line class with a constructor, a method, a getter and a private field.

```js run
class OrderLine {
  #qty;
  constructor(code, paise, qty) {
    this.code = code;
    this.paise = paise;
    this.#qty = qty;
  }
  addOne() {
    this.#qty = this.#qty + 1;
    return this;
  }
  get qty() {
    return this.#qty;
  }
  get total() {
    return this.paise * this.#qty;
  }
  describe() {
    return this.#qty + " x " + this.code + " = " + this.total + " paise";
  }
}

const bags = new OrderLine("BAG", 89900, 1);
bags.addOne().addOne();
console.log(bags.describe());
console.log(bags.qty, bags.total);
```

Line by line:

1. `#qty` is declared at the top of the class, so the class can use it.
2. The constructor stores the code and the price as public fields, and the quantity as a private one.
3. `addOne` raises the quantity and returns `this`, so two calls chain. The quantity goes 1, 2, 3.
4. `describe` reads `this.total`, a getter, without brackets. It prints `3 x BAG = 269700 paise`.
5. Outside the class, `bags.qty` works through the getter, but nothing outside can set `#qty` directly. The last line prints `3 269700`.

## Try it

Add a `removeOne()` method to `OrderLine` that lowers the quantity, but never below 0. Then add a getter `isEmpty` that is `true` when the quantity is 0.

```js run
class OrderLine {
  #qty;
  constructor(code, paise, qty) {
    this.code = code;
    this.paise = paise;
    this.#qty = qty;
  }
  get total() {
    return this.paise * this.#qty;
  }
}
const golu = new OrderLine("AMI", 45000, 2);
console.log(golu.total);
```

## Recap

- A class is a blueprint, and `new ClassName(...)` builds one instance from it.
- The constructor sets up each new instance. Inside it, `this` is the new object.
- Methods sit in the class body without commas. `this` is the instance they were called on.
- A getter (`get total() { }`) is read like a property: `cart.total`, without brackets.
- Private fields start with `#`. Only the class's own code can use them, which keeps the data safe.

## Common mistakes

**1. Forgetting new.**

```js run crash
class Cart {
  constructor(customer) {
    this.customer = customer;
  }
}
const cart = Cart("Asha");
```

Node stops with `TypeError: Class constructor Cart cannot be invoked without 'new'`. Fix: `const cart = new Cart("Asha");`.

**2. Leaving out this inside a method.**

```js run crash
class Cart {
  constructor() {
    this.lines = [];
  }
  count() {
    return lines.length;
  }
}
console.log(new Cart().count());
```

Node stops with `ReferenceError: lines is not defined`. Inside a method, a bare name means a variable, not a field. Fix: `return this.lines.length;`.

**3. Calling a getter with brackets.**

```js run crash
class Cart {
  get total() {
    return 0;
  }
}
const cart = new Cart();
console.log(cart.total());
```

Node stops with `TypeError: cart.total is not a function`. `cart.total` already ran the getter and gave a number. Fix: `cart.total`, with no brackets.

**4. Reaching for a private field from outside.**

```js nocompile
class Cart {
  #lines = [];
}
const cart = new Cart();
console.log(cart.#lines);
```

Node refuses to run the file: `SyntaxError: Private field '#lines' must be declared in an enclosing class`. Fix: add a method or getter to the class that hands out what other code needs, like a copy of the lines.
