## Hook

The stock box at the stall has tubs with labels: "Keychains", "Coasters", "Booties". When someone drops a coaster set into the keychain tub, the evening count goes wrong, and nobody knows why.

In TypeScript, every box in your code gets a label too. The checker makes sure a number never lands in a text box. And most of the time, you don't even write the label: TypeScript reads it off the value.

## Concept

### Syntax: annotating a variable

```ts
let shopName: string = "Dhaaga & Co.";
```

The name, a colon, the type, then `=` and the value. You met this in the last ticket. From here on, the box `shopName` only accepts text.

### The three everyday types

Most values in the calculator are one of three **primitive types**:

| Type | Holds | Examples |
|---|---|---|
| `string` | text | `"DC-1042"`, `"Rani pink"`, `""` |
| `number` | any number, whole or not | `15000`, `-3`, `0.29` |
| `boolean` | yes or no | `true`, `false` |

They're the same kinds you met with `typeof` in web-03, and they're written in lower case.

```ts run
const shopName: string = "Dhaaga & Co.";
const coasterPaise: number = 15000;
const openToday: boolean = true;
console.log(typeof shopName, typeof coasterPaise, typeof openToday);
```

It prints `string number boolean`. The annotations are gone when it runs, but the values still have their kinds.

```callout jargon
A **primitive type** is a basic kind of value that isn't an object: `string`, `number` and `boolean` are the everyday ones. `null` and `undefined` are primitives too. You'll meet them in ticket 8.
```

### Inference: TypeScript reads the label off the value

You rarely need to write `: number`. When you give a box a value straight away, TypeScript works out the type from it:

```ts
let qty = 2;
qty = "3";
```

```text
error TS2322: Type 'string' is not assignable to type 'number'.
```

There's no annotation, yet line 2 is still an error. TypeScript saw `2` on line 1 and decided `qty` is a `number` for good. Hover over `qty` in VS Code, and it shows `let qty: number`.

```callout jargon
**Type inference** means TypeScript works out a type by itself, from the value you give. The type it picks is the **inferred type**.
```

Watch it infer a type for every box, line by line:

```animate
title: TypeScript infers a type for every box
kind: code
language: ts
code: |
  let qty = 2;
  const typed = "3";
  qty = Number(typed);
  const label = qty + " keychains";
  const enough = qty <= 5;
  console.log(label);
  console.log(enough);
steps:
  - line: 1
    vars: { qty: 2 }
    say: The value is 2, so TypeScript infers number. From now on, qty only takes numbers.
  - line: 2
    vars: { typed: '"3"' }
    say: Quotes make text, so typed is a string. It looks like a number, but it isn't one.
  - line: 3
    vars: { qty: 3 }
    say: Number(...) always gives a number, so the new value fits the number box. Putting typed in directly would be an error.
  - line: 4
    vars: { label: '"3 keychains"' }
    say: A number plus a string makes a string, so label is inferred as string.
  - line: 5
    vars: { enough: true }
    say: A comparison gives true or false, so enough is a boolean.
  - line: 6
    out: "3 keychains"
    say: The types are stripped before running. The values print as usual.
  - line: 7
    out: "true"
    say: Three keychains is no more than five, so enough is true.
```

### When to write the type yourself

Let inference do the work when a box gets its value straight away. Write the type yourself in two places:

- **Function parameters.** TypeScript can't guess what callers will pass. You'll type them in the next ticket.
- **A box declared before it gets a value.** There's nothing to infer from yet.

```ts
let total: number;
total = 15000 + 12000;
```

```callout tip
If the type you'd write is exactly what TypeScript infers, leave it out. `const qty: number = 2;` and `const qty = 2;` mean the same thing, and the second is easier to read.
```

### Syntax: any

```ts
let anything: any = "3";
```

`any` is a special type that switches the checker off. A box of type `any` accepts every value, and TypeScript lets you do anything with it, without a single warning:

```ts run
let price: any = "450";
const total = price + 30;
console.log(total);
```

It prints `45030`. The price was text, so `+` glued instead of adding. With `let price: number`, TypeScript would have refused the text on line 1.

```callout jargon
**any** is the type that turns type checking off for a value. It's an escape hatch, and every `any` is a place where bugs can hide.
```

Under **strict mode**, the setting every Dhaaga project uses, TypeScript won't let a value become `any` without you saying so. A parameter with no type is an error:

```text
error TS7006: Parameter 'paise' implicitly has an 'any' type.
```

### Converting between types

Values often need to change type: text from a form becomes a number, a number becomes text for a label. The functions from web-03 still work, and TypeScript knows the type each one gives back:

| Code | Gives back |
|---|---|
| `Number("4")` | `number`: `4` |
| `String(15000)` | `string`: `"15000"` |
| `(45000 / 100).toFixed(2)` | `string`: `"450.00"` |
| `Number.isInteger(4)` | `boolean`: `true` |

```ts run
const typed: string = " 4 ";
const qty: number = Number(typed.trim());
const rupees: string = (45000 / 100).toFixed(2);
console.log(qty * 2);
console.log(rupees);
console.log(Number.isInteger(qty));
```

It prints `8`, `450.00` and `true`. Notice that `toFixed` gives a `string`. It's for showing, not for more maths.

## Worked example

Raju wants a label for the shelf, with the price and how many are left:

```ts run
const name: string = "Golu the elephant";
const paise: number = 45000;
const handmade: boolean = true;
let left = 3;
left = left - 1;
const label = `${name}: ₹${(paise / 100).toFixed(2)}, ${left} left`;
console.log(label);
console.log(typeof label, typeof left, typeof handmade);
```

1. Lines 1 to 3 annotate each box. They'd be inferred the same way, but here the labels make the plan clear.
2. Line 4 has no annotation. TypeScript infers `number` from `3`.
3. Line 5 subtracts one. The result is a number, so it fits.
4. Line 6 builds a template literal, so `label` is inferred as `string`.
5. Line 7 prints `Golu the elephant: ₹450.00, 2 left`.
6. Line 8 prints `string number boolean`.

## Try it

Run this, then change `typed` to `"three"` and run it again. What does it print now, and why doesn't TypeScript complain?

```ts run
const typed: string = "3";
const qty: number = Number(typed);
const keychainPaise: number = 12000;
console.log(qty * keychainPaise);
```

## Recap

- `string`, `number` and `boolean` are the everyday types, written in lower case.
- `let name: type = value` annotates a box, and the box then only accepts that type.
- When a box gets a value straight away, TypeScript infers its type, so you can usually leave the annotation out.
- `any` switches the checker off. Strict mode refuses values that become `any` by accident.
- `Number(...)`, `String(...)` and `toFixed` change a value's type, and TypeScript knows what each gives back.

## Common mistakes

**1. Writing String instead of string.**

```ts
function shout(text: string): string {
  return text.toUpperCase();
}
const toyName: String = "Golu";
shout(toyName);
```

```text
error TS2345: Argument of type 'String' is not assignable to parameter of type 'string'.
  'string' is a primitive, but 'String' is a wrapper object. Prefer using 'string' when possible.
```

The capital-letter `String`, `Number` and `Boolean` are something else. Always use the lower-case names.

**2. Putting text from a form into a number box.**

```ts
const typed: string = "2";
const qty: number = typed;
```

```text
error TS2322: Type 'string' is not assignable to type 'number'.
```

What people type is always text. Convert it first: `const qty: number = Number(typed);`.

**3. Expecting toFixed to give a number.**

```ts
const rupees: number = (45000 / 100).toFixed(2);
```

```text
error TS2322: Type 'string' is not assignable to type 'number'.
```

`toFixed` gives text, ready to show. Keep the maths in paise, and call `toFixed` only when you build the label.

**4. Using any to make an error go away.**

```ts
let price: any = "450";
const total = price + 30;
```

The error is gone, and so is the warning about the bug: `total` is the text `"45030"`. Give the box its real type, `let price: number`, and fix the value instead.
