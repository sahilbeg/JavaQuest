## Hook

When a custom order comes in on paper, Meera Aunty reads it before she starts crocheting. Is there a phone number she can call? Is the name to stitch on short enough to fit on an elephant's ear? A good form does that reading for her, kindly, before the order is sent.

This ticket builds the custom-order form with typed values, one typed message per field, and a typed, clean request at the end.

## Concept

### Two shapes: what is typed, and what is sent

Every input box holds text, even the "How many" box. So the form's state has a text field for each box:

```ts
interface RequestForm {
  name: string;
  phone: string;
  colour: Colour;
  qty: string;            // "2", as typed
  stitchedName: string;   // "" when left empty
}
```

What Meera Aunty needs is different: a real number, a phone without spaces, and `null` for "no name". That is a second type:

```ts
interface CustomRequest {
  name: string;
  phone: string;
  colour: Colour;
  qty: number;
  stitchedName: string | null;
}
```

```callout tip
Keep the two apart. The form type matches what the boxes hold. The request type matches what the rest of the app needs. A small function turns one into the other, after validation says it's safe.
```

### Errors, one per field

Each field can have at most one message. TypeScript can build that type from the form type:

```ts
type RequestErrors = Partial<Record<keyof RequestForm, string>>;
```

```callout jargon
**keyof RequestForm** is the union of the form's field names: `"name" | "phone" | "colour" | "qty" | "stitchedName"`. **Record<K, string>** is an object with a string for every key in K. **Partial** makes every key optional.
```

So `RequestErrors` is "an object that may have a message for any field, and for nothing else". `errors.phon = "..."` is a type error, so a misspelt field can't hide a message.

### Syntax: a validation function

```ts
function validate(form: RequestForm): RequestErrors {
  const errors: RequestErrors = {};
  if (form.name.trim() === "") errors.name = "Please enter your name.";
  // ... one check per field
  return errors;
}
```

Validation is a plain function: form in, errors out. No React, no state. That makes it easy to test, and easy to reuse on the server later.

```animate
title: Validation collects one message per bad field
kind: code
language: ts
code: |
  type Form = { name: string; phone: string };
  type Errors = Partial<Record<keyof Form, string>>;
  function validate(form: Form): Errors {
    const errors: Errors = {};
    if (form.name.trim() === "") errors.name = "Please enter your name.";
    if (!/^\d{10}$/.test(form.phone.replace(/ /g, ""))) errors.phone = "Phone needs 10 digits, like 98765 43210.";
    return errors;
  }
  console.log(JSON.stringify(validate({ name: " ", phone: "98765" })));
  console.log(JSON.stringify(validate({ name: "Asha", phone: "98765 43210" })));
steps:
  - line: 4
    vars: { errors: "{}" }
    say: Validation starts with an empty errors object. No messages means no problems.
  - line: 5
    vars: { errors: "{ name }" }
    say: The name is only a space. After trim it's empty, so name gets a message.
  - line: 6
    vars: { errors: "{ name, phone }" }
    say: 'The phone "98765" has only 5 digits, so phone gets a message too.'
  - line: 9
    out: '{"name":"Please enter your name.","phone":"Phone needs 10 digits, like 98765 43210."}'
    say: Two bad fields, two messages.
  - line: 10
    out: "{}"
    say: Asha's form is fine. The spaces in her phone are removed before counting, so it has 10 digits.
```

### Syntax: one update function for every field

```tsx
function update<K extends keyof RequestForm>(field: K, value: RequestForm[K]) {
  setForm({ ...form, [field]: value });
}
```

This one function sets any field, and TypeScript still checks each call. `K` is the field's name, and `RequestForm[K]` is that field's type:

```tsx
update("name", "Asha Rao");     // fine: name is a string
update("colour", "Mint");       // fine: "Mint" is a Colour
update("qty", 3);
update("colour", "Purple");
```

```text
error TS2345: Argument of type 'number' is not assignable to parameter of type 'string'.
error TS2345: Argument of type '"Purple"' is not assignable to parameter of type 'Colour'.
```

```callout jargon
**RequestForm[K]** is an **indexed access type**: "the type of the field named K". With `K` as `"qty"`, it's `string`.
```

### Syntax: typed change and submit events

```tsx
onChange={(event: ChangeEvent<HTMLInputElement>) => update("name", event.currentTarget.value)}
```

```tsx
function handleSubmit(event: FormEvent<HTMLFormElement>) {
  event.preventDefault();
  // ...
}
```

Import both as types from `"react"`. `currentTarget` is the element the handler is on, so for an input it is an `HTMLInputElement` and `.value` is a `string`.

A `<select>` gives a `string` too, not a `Colour`. Check it with a type guard before you store it:

```tsx
function isColour(value: string): value is Colour {
  return (COLOURS as readonly string[]).includes(value);
}
```

### Show messages after the first try

Messages that appear while someone is still typing their phone number feel like shouting. So keep one more piece of state:

```tsx
const [tried, setTried] = useState<boolean>(false);
const errors = validateRequest(form);
```

- `errors` is worked out on every render, from `form`. It's never stored in state, so it can't get out of date.
- Messages only show when `tried` is true. Pressing Send sets it.
- After that, each message disappears the moment the field is fixed, because `errors` is worked out again as the customer types.

```animate
title: Messages wait for the first Send
kind: scene
nodes:
  - { id: form, label: The form, emoji: "📝", value: "phone: 9876" }
  - { id: check, label: validateRequest, emoji: "🔍", value: "phone ❌" }
  - { id: screen, label: What shows, emoji: "👀", value: "no messages" }
steps:
  - say: The customer is typing. validateRequest already knows the phone is too short, on every render.
    focus: [form, check]
  - say: But tried is false, so nothing red shows yet.
    focus: [screen]
  - say: The customer presses Send. tried becomes true, and the phone message appears under the box.
    set: { screen: "Phone needs 10 digits" }
  - say: They type the last six digits. The next render finds no problem with the phone.
    set: { form: "phone: 98765 43210", check: "all good", screen: "no messages" }
  - say: Send again. No errors, so the clean request goes to Meera Aunty.
    send: { from: form, to: screen, label: "onSubmit(request)" }
```

### Syntax: from form to clean request

```ts
declare function toRequest(form: RequestForm): CustomRequest;
```

Call it only after validation found nothing. It trims the name, removes spaces from the phone, turns `"2"` into `2`, and turns an empty stitched name into `null`. Its return type promises the rest of the app exactly the shape it expects.

## Worked example

Here's a smaller form for a different job: a customer review, with a star rating typed as text.

```tsx
import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";

interface ReviewForm { rating: string; text: string }
type ReviewErrors = Partial<Record<keyof ReviewForm, string>>;

function validateReview(form: ReviewForm): ReviewErrors {
  const errors: ReviewErrors = {};
  const stars = Number(form.rating);
  if (!Number.isInteger(stars) || stars < 1 || stars > 5) errors.rating = "Pick 1 to 5 stars.";
  if (form.text.trim().length > 200) errors.text = "Keep it under 200 letters.";
  return errors;
}

export function ReviewBox({ onSend }: { onSend: (stars: number, text: string) => void }) {
  const [form, setForm] = useState<ReviewForm>({ rating: "", text: "" });
  const [tried, setTried] = useState<boolean>(false);
  const errors = validateReview(form);

  function update<K extends keyof ReviewForm>(field: K, value: ReviewForm[K]) {
    setForm({ ...form, [field]: value });
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setTried(true);
    if (Object.keys(errors).length === 0) onSend(Number(form.rating), form.text.trim());
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <label>
        Stars <input value={form.rating} onChange={(e: ChangeEvent<HTMLInputElement>) => update("rating", e.currentTarget.value)} />
      </label>
      {tried && errors.rating && <p role="alert">{errors.rating}</p>}
      <button type="submit">Send review</button>
    </form>
  );
}
```

- The form type is all text; `onSend` gets a real number.
- `errors` is computed while rendering, and the messages wait for `tried`.
- `Object.keys(errors).length === 0` means "no field has a message".
- `noValidate` turns off the browser's own pop-up checks, so the customer only sees our kind messages.

## Try it

Open the preview and press **Send request** with everything empty. Messages appear under the name and phone. Type a name: its message disappears as you type.

Then try `update("qty", 3)` somewhere in `CustomOrderForm.tsx` and read the type error. Remove it again.

## Recap

- Type the form as the boxes hold it (all text), and the result as the app needs it (numbers, `null`).
- `Partial<Record<keyof Form, string>>` types "at most one message per field, and no unknown fields".
- Validation is a plain function: form in, errors out. Work it out on every render; don't store it in state.
- One generic `update<K extends keyof Form>(field: K, value: Form[K])` sets any field and still checks each value's type.
- Show messages after the first Send, and send the clean request only when there are no errors.

## Common mistakes

**1. Keeping errors in state.**

```tsx
const [errors, setErrors] = useState<RequestErrors>({});
```

Now every change must remember to call `setErrors`. Miss one, and a fixed phone still shows its red message.

Fix: `const errors = validateRequest(form);` on every render.

**2. Updating a field with a string key.**

```tsx
function update(field: string, value: string) {
  setForm({ ...form, [field]: value });
}
```

TypeScript accepts `update("phon", "98765 43210")`, which adds a field nobody reads. The real phone stays empty.

Fix: `update<K extends keyof RequestForm>(field: K, value: RequestForm[K])`.

**3. Casting the select's value.**

```tsx
update("colour", event.currentTarget.value as Colour);
```

It compiles, but nothing checks the value. An edited page could send `"Purple"`.

Fix: check it with `isColour(value)` first.

**4. Sending the form instead of the request.**

```tsx
onSubmit(form);
```

You see `Argument of type 'RequestForm' is not assignable to parameter of type 'CustomRequest'` with a note that `qty` is a string. TypeScript caught the text "2" heading for code that adds numbers.

Fix: `onSubmit(toRequest(form));`.
