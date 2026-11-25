## Hook

Meera Aunty used to take custom orders on paper slips. Name, item, how many, gift wrap or not. Some slips came back with "three-ish" written in the quantity box, or no name at all.

A printed form with boxes and tick marks fixes most of that. A typed form in React goes further: every field has a type, and TypeScript checks each one before a customer ever sees it.

## Concept

### Syntax: a controlled input

```tsx
const [name, setName] = useState("");

<input id="name" value={name} onChange={(e) => setName(e.target.value)} />
```

The input shows what is in state, and every keystroke updates the state. State is the single source of truth.

```callout jargon
A **controlled input** is a form field whose value comes from React state, with an `onChange` that updates that state. React is always in charge of what it shows.
```

Because the value lives in state, you can check it, change it or reset it from code at any time.

```callout warning
`value` without `onChange` makes a box nobody can type in, and React warns in the console. A controlled input always needs both.
```

### One typed object for the whole form

```tsx
interface OrderRequest {
  name: string;
  item: ItemCode;
  qty: number;
  giftWrap: boolean;
}

const [form, setForm] = useState<OrderRequest>({ name: "", item: "AMI", qty: 1, giftWrap: false });
```

Four separate `useState` calls would work. One object keeps the form together, and the interface says exactly what a finished request looks like. It is the same shape you send to the parent, or later to a server.

### Syntax: one update function for every field

```tsx
function update<K extends keyof OrderRequest>(key: K, value: OrderRequest[K]) {
  setForm({ ...form, [key]: value });
}
```

This is the generic constraint from ts-09 at work.

- `K extends keyof OrderRequest` means `key` must be one of `"name"`, `"item"`, `"qty"` or `"giftWrap"`.
- `OrderRequest[K]` is the type of that field. So `value` must match the field you name.
- `{ ...form, [key]: value }` copies the form and changes one field.

```tsx
update("qty", 3);     // fine: qty is a number
update("qty", "3");   // error: "3" is a string
update("colour", 2);  // error: there is no colour field
```

```callout jargon
**keyof** gives the union of an object type's property names. `keyof OrderRequest` is `"name" | "item" | "qty" | "giftWrap"`.
```

### Syntax: selects and checkboxes

```tsx
<select id="qty" value={form.qty} onChange={(e) => update("qty", Number(e.target.value))}>
  <option value="1">1</option>
  <option value="2">2</option>
</select>

<input id="gift" type="checkbox" checked={form.giftWrap} onChange={(e) => update("giftWrap", e.target.checked)} />
```

- A `<select>` is controlled the same way, with `value` and `onChange`.
- Every form value arrives as text. `e.target.value` is `"2"`, never `2`. Convert it with `Number(...)` before it goes into a number field.
- A checkbox uses `checked`, a boolean, instead of `value`.

### Text that should be a union

```tsx
function handleItem(e: ChangeEvent<HTMLSelectElement>) {
  const value = e.target.value;
  if (isItemCode(value)) {
    update("item", value);
  }
}
```

`e.target.value` is a `string`, but `item` must be an `ItemCode`. You could write `value as ItemCode`, but that only silences `tsc`. A type guard, like the ones from ts-09, really checks.

### Syntax: submitting the form

```tsx
import type { SubmitEvent } from "react";

function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
  e.preventDefault();
  onSend(form);
}

<form onSubmit={handleSubmit}>
  ...
  <button type="submit">Send request</button>
</form>
```

Pressing the submit button, or Enter in a text box, fires the form's `submit` event. By default the browser then reloads the page. `e.preventDefault()` stops that, so React stays in charge.

```callout tip
Older code and tutorials type this event as `FormEvent<HTMLFormElement>`. The current React types mark `FormEvent` as deprecated and give `onSubmit` a `SubmitEvent`. Both have `preventDefault()`.
```

### From keystroke to request

```animate
title: Asha fills in the form
kind: scene
nodes:
  - { id: fields, label: "Form fields", emoji: "📝" }
  - { id: update, label: "update(key, value)", emoji: "🔧" }
  - { id: state, label: "form state", emoji: "🧠", value: "qty: 1, giftWrap: false" }
  - { id: submit, label: "handleSubmit", emoji: "📮" }
  - { id: parent, label: "onSend", emoji: "🧶" }
steps:
  - say: Asha picks 3 in the quantity list. The select's value arrives as the text "3".
    send: { from: fields, to: update, label: "Number(\"3\")" }
  - say: update stores the number 3. tsc checked that qty takes a number.
    send: { from: update, to: state, label: "qty: 3" }
    set: { state: "qty: 3, giftWrap: false" }
  - say: She ticks gift wrap. The checkbox gives checked, a boolean.
    send: { from: fields, to: update, label: "giftWrap: true" }
    set: { state: "qty: 3, giftWrap: true" }
  - say: She presses Send. handleSubmit stops the page reload first.
    send: { from: fields, to: submit, label: "preventDefault()" }
  - say: The name is filled in, so the whole typed object goes to the parent in one call.
    send: { from: state, to: parent, label: "onSend(form)" }
```

## Worked example

A delivery address form with two fields. It refuses PIN codes that are not six digits.

```tsx
import { useState } from "react";
import type { SubmitEvent } from "react";

interface Address {
  line: string;
  pin: string;
}

export function AddressForm({ onSave }: { onSave: (address: Address) => void }) {
  const [address, setAddress] = useState<Address>({ line: "", pin: "" });
  const [error, setError] = useState("");

  function update<K extends keyof Address>(key: K, value: Address[K]) {
    setAddress({ ...address, [key]: value });
  }

  function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!/^\d{6}$/.test(address.pin)) {
      setError("A PIN code has six digits");
      return;
    }
    setError("");
    onSave(address);
  }

  return (
    <form onSubmit={handleSubmit}>
      <label htmlFor="line">Address</label>
      <input id="line" value={address.line} onChange={(e) => update("line", e.target.value)} />
      <label htmlFor="pin">PIN code</label>
      <input id="pin" value={address.pin} onChange={(e) => update("pin", e.target.value)} />
      <p className="error">{error}</p>
      <button type="submit">Save address</button>
    </form>
  );
}
```

Line by line:

- `useState<Address>` types the whole form at once.
- `update` works for both fields. `update("pin", 411007)` would be a type error, because a PIN is kept as text.
- `handleSubmit` stops the reload, checks the PIN, and only then calls `onSave`.
- A PIN stays a `string` on purpose. It is a label made of digits, and nobody does maths with PIN codes.

## Try it

The same typed `update`, without React. Run it, then try `update(form, "qty", "4")` and think about what `tsc` would say.

```ts run
interface OrderRequest {
  name: string;
  qty: number;
  giftWrap: boolean;
}

function update<K extends keyof OrderRequest>(form: OrderRequest, key: K, value: OrderRequest[K]): OrderRequest {
  return { ...form, [key]: value };
}

let form: OrderRequest = { name: "", qty: 1, giftWrap: false };
form = update(form, "name", "Asha Rao");
form = update(form, "qty", Number("3"));
form = update(form, "giftWrap", true);
console.log(JSON.stringify(form));
```

It prints `{"name":"Asha Rao","qty":3,"giftWrap":true}`. The `3` has no quotes: it is a real number, so a total can be worked out from it.

## Recap

- A controlled input has `value` from state and an `onChange` that sets it. State is the single source of truth.
- Keep a form in one typed object: `useState<OrderRequest>(...)`.
- `update<K extends keyof OrderRequest>(key: K, value: OrderRequest[K])` changes one field, and `tsc` checks the value fits it.
- Form values arrive as text. Use `Number(...)` for numbers, `checked` for checkboxes, and a type guard for unions.
- Submit with `onSubmit` on the form, and call `e.preventDefault()` first.

## Common mistakes

**1. Storing a number field as text.**

```tsx
update("qty", e.target.value);
```

`tsc` says `Argument of type 'string' is not assignable to parameter of type 'number'.` That is the bug from the ticket, caught early. Write `update("qty", Number(e.target.value))`.

**2. Forgetting preventDefault.**

```tsx
function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
  onSend(form);
}
```

The browser reloads the page after sending, and everything typed is lost. Call `e.preventDefault()` first.

**3. Reading value from a checkbox.**

```tsx
update("giftWrap", e.target.value);
```

A checkbox's `value` is the text `"on"`, ticked or not. `tsc` refuses it, because `giftWrap` is a boolean. Use `e.target.checked`.

**4. Silencing the type with as.**

```tsx
update("item", e.target.value as ItemCode);
```

It type-checks, but nothing stops a wrong value from getting in. Check with a type guard, `if (isItemCode(value))`, so only real codes are stored.
