## Hook

The Aundh shop keeps its stock register in a drawer behind the counter. Anyone can ask Raju how many elephants are left. But only Raju writes in the register, and he checks every delivery and every sale before he does.

You built classes in JavaScript in the catalogue-engine project. TypeScript adds the rules that make a class work like that drawer: which fields exist, who may touch them, and what the class promises to the rest of the code.

## Concept

### Syntax: typed fields

```ts
class YarnBall {
  colour: string;
  grams: number;
  constructor(colour: string, grams: number) {
    this.colour = colour;
    this.grams = grams;
  }
}
```

In TypeScript, every field is declared in the class body with its type, before the constructor uses it. Leave the declaration out, and `this.colour = colour` is an error:

```text
error TS2339: Property 'colour' does not exist on type 'YarnBall'.
```

Strict mode also checks that every field gets a value. Declare `grams: number;` and forget to set it in the constructor, and you see:

```text
error TS2564: Property 'grams' has no initializer and is not definitely assigned in the constructor.
```

A field can also start with a value right where it is declared: `stamps = 0;`. TypeScript infers its type from that value.

### Syntax: public and private

```ts
class YarnBall {
  public colour: string;
  private grams: number;
}
```

An **access modifier** says who may use a field or method. `public` (the default) means anyone. `private` means only code inside this class.

```callout jargon
An **access modifier** (`public`, `private` or `protected`) is a word before a field or method that says which code may use it. `protected` also lets classes that extend this one use it.
```

Outside the class, reading or writing a private field is an error:

```text
error TS2341: Property 'grams' is private and only accessible within class 'YarnBall'.
```

`private` is a TypeScript check only: it disappears when the code runs. JavaScript's `#grams`, which you met before, is private at run time too. Both are fine; many TypeScript teams use `private` for its readable error messages.

### Syntax: readonly fields

```ts
class YarnBall {
  readonly colour: string;
}
```

A `readonly` field can be set in its declaration or in the constructor, and never again.

```text
error TS2540: Cannot assign to 'colour' because it is a read-only property.
```

Use it for things that never change once an object exists, like an item's id or a shop's name.

### Watching a typed class work

```animate
title: Only the class's own methods touch grams
kind: code
language: ts
code: |
  class YarnBall {
    readonly colour: string;
    private grams: number;
    constructor(colour: string, grams: number) {
      this.colour = colour;
      this.grams = grams;
    }
    use(g: number): boolean {
      if (g > this.grams) return false;
      this.grams -= g;
      return true;
    }
    get left(): number {
      return this.grams;
    }
  }
  const mint = new YarnBall("Mint", 100);
  console.log(mint.use(40), mint.use(80), mint.left);
steps:
  - line: 17
    objects: { y1: { type: YarnBall, fields: {} } }
    vars: { mint: "@y1" }
    say: new makes a YarnBall. The constructor will set both fields, which strict mode insists on.
  - line: 6
    objects: { y1: { type: YarnBall, fields: { colour: '"Mint"', grams: 100 } } }
    say: colour is readonly, and grams is private. From now on, only YarnBall's own methods may change grams.
  - line: 10
    objects: { y1: { type: YarnBall, fields: { colour: '"Mint"', grams: 60 } } }
    say: use(40) checks there is enough yarn, then takes 40 g away and returns true.
  - line: 9
    say: use(80) asks for more than the 60 g left, so it returns false and changes nothing.
  - line: 14
    say: The getter left lets other code read the grams, without being able to write them.
  - line: 18
    out: "true false 60"
    say: Outside the class, mint.grams = 500 would be a type error. The only way in is through use.
```

### Syntax: implements

```ts
interface StockStore {
  stockOf(id: string): number;
  sell(id: string, qty: number): boolean;
}
class StallStock implements StockStore {
  stockOf(id: string): number {
    return 0;
  }
  sell(id: string, qty: number): boolean {
    return false;
  }
}
```

`implements` is a promise: this class has everything the interface lists, with the right types. If a method is missing, TypeScript says so at the class:

```text
error TS2420: Class 'StallStock' incorrectly implements interface 'StockStore'.
```

```callout jargon
A class **implements** an interface when it has every member the interface lists. Other code can then accept any `StockStore`, whether it's the stall's, the shop's, or a fake one in a test.
```

### Syntax: parameter properties

```ts
class Shop {
  constructor(public readonly name: string, private opensAt: number) {}
}
```

A **parameter property** is a shortcut: a modifier in front of a constructor parameter declares the field and sets it in one go. This is the same as writing both fields out and assigning them.

```callout jargon
A **parameter property** is a constructor parameter with `public`, `private`, `protected` or `readonly` in front of it. TypeScript turns it into a field.
```

You'll see it in many codebases. But it's one of the few TypeScript features that isn't just types, so it can't simply be stripped away. Node's built-in TypeScript support refuses it, and some teams switch it off. This course writes fields out in full.

## Worked example

Meera Aunty wants a loyalty card for the stall: every 10th purchase is free.

```ts run
interface Card {
  readonly phone: string;
  stamp(): boolean;
}

class LoyaltyCard implements Card {
  readonly phone: string;
  private stamps = 0;

  constructor(phone: string) {
    this.phone = phone;
  }

  stamp(): boolean {
    this.stamps += 1;
    if (this.stamps === 10) {
      this.stamps = 0;
      return true;
    }
    return false;
  }

  get stampsSoFar(): number {
    return this.stamps;
  }
}

const card = new LoyaltyCard("98765 43210");
let free = 0;
for (let purchase = 1; purchase <= 12; purchase++) {
  if (card.stamp()) {
    free += 1;
  }
}
console.log(card.phone, free, card.stampsSoFar);
```

Line by line:

1. `Card` is the promise: a phone number nobody can change, and a `stamp` method.
2. `LoyaltyCard implements Card`, so TypeScript checks it has both, with the right types.
3. `private stamps = 0` starts at 0, and its type, `number`, is inferred. Only `stamp` changes it.
4. The 10th stamp returns `true` and starts the count again.
5. After 12 purchases there was 1 free one, and 2 stamps are on the new round. It prints `98765 43210 1 2`.

## Try it

Add a `private` field `lastVisit: string` to `LoyaltyCard`, set it in `stamp(day: string)`, and read it through a getter. Then try `card.stamps = 9;` after the class, and read the error in your editor.

```ts run
class Counter {
  private count = 0;
  add(): number {
    this.count += 1;
    return this.count;
  }
}
const visits = new Counter();
visits.add();
console.log(visits.add());
```

## Recap

- Declare every field in the class body with its type. Strict mode checks that each one gets a value.
- `private` fields and methods can only be used inside the class; `public` is the default.
- `readonly` fields are set once, in the declaration or the constructor.
- `class X implements Y` promises that X has everything interface Y lists, and TypeScript checks it.
- Parameter properties, like `constructor(private name: string)`, declare and set a field in one go, but they aren't plain JavaScript once the types are stripped.

## Common mistakes

**1. Assigning a field you never declared.**

```ts
class Shop {
  constructor(name: string) {
    this.name = name;
  }
}
```

```text
error TS2339: Property 'name' does not exist on type 'Shop'.
```

Fix: declare it first, in the class body: `name: string;`.

**2. Making the data public and hoping nobody misuses it.**

```ts
class StockBook {
  counts = new Map<string, number>();
}
const book = new StockBook();
book.counts.set("DC-1042", -2);
```

It compiles, and the stall now has -2 elephants. Fix: `private counts`, plus methods like `sell` that check before they change anything.

**3. Forgetting a member the interface needs.**

```ts
interface StockStore {
  stockOf(id: string): number;
  sell(id: string, qty: number): boolean;
}
class StallStock implements StockStore {
  stockOf(id: string): number {
    return 0;
  }
}
```

```text
error TS2420: Class 'StallStock' incorrectly implements interface 'StockStore'.
```

The class promised a `sell` method and doesn't have one. Fix: add `sell(id: string, qty: number): boolean`.
