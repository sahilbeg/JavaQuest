## Hook

A good counter clerk reads your form back before taking it. "Your PIN code has only five numbers. Can you check it?" She doesn't shout before you've finished writing, and she points at the exact box that's wrong.

That's the kind of form Raju needs: one that checks the address before an order goes out, at the right moment, and says exactly what to fix.

## Concept

### One state object for the whole form

In react-08 you built controlled inputs: the input shows a state value, and `onChange` updates it. With four fields, one object is tidier than four `useState` calls.

```jsx
const [values, setValues] = useState({ name: "", phone: "", line1: "", pincode: "" });
```

### Syntax: one change handler for every field

```jsx
function handleChange(e) {
  setValues({ ...values, [e.target.name]: e.target.value });
}
```

Each input has a `name` that matches a key in `values`. `[e.target.name]` is a **computed key**: the square brackets mean "use the value of this expression as the key". Typing in the `pincode` input sets `values.pincode`, and the spread keeps the other fields.

```jsx
<input id="pincode" name="pincode" value={values.pincode} onChange={handleChange} />
```

### Syntax: a validation function

```jsx
function validate(values) {
  const errors = {};
  if (values.name.trim() === "") errors.name = "Please enter your name.";
  return errors;
}
```

```callout jargon
**Validation** means checking that input follows the rules before you use it. A **validation function** takes the values and returns the problems it found. Here that's an object of error messages, one per bad field. An empty object means everything passed.
```

Keep it a plain function, outside the component. It doesn't need React, so it's easy to test, and the checkout page can reuse it.

### Syntax: checking digits with a pattern

```js
/^\d{10}$/.test("9876543210");
```

```callout jargon
A **regular expression** (or regex) is a pattern for text, written between slashes. `^` means "the start", `$` means "the end", `\d` means "one digit", and `{10}` means "exactly 10 of the one before". `[1-9]` means "one digit from 1 to 9".
```

So `/^\d{10}$/` means "exactly ten digits and nothing else". Indian PIN codes have six digits and never start with 0, so the PIN pattern is `/^[1-9]\d{5}$/`. People often type a phone number with a space, like `98765 43210`, so remove the spaces before testing.

```js run
const tenDigits = /^\d{10}$/;
for (const phone of ["98765 43210", "98765 4321", "98765-43210"]) {
  const digits = phone.replaceAll(" ", "");
  console.log(phone, tenDigits.test(digits));
}

const pin = /^[1-9]\d{5}$/;
for (const code of ["411007", "41100", "011007"]) {
  console.log(code, pin.test(code));
}
```

### When to show the errors

If you show every error on the first render, a fresh form greets the customer with four red messages before they've typed anything. That feels like being told off.

A friendly rule:

- Before the first submit, show nothing.
- When the customer presses Save, show every problem.
- After that, update the messages as they type, so each one disappears the moment its field is fixed.

You need one more piece of state, `tried`, which turns true on the first submit. The errors themselves are worked out on every render, from `values`. Don't keep them in state: they'd go out of date.

```jsx
const errors = validateAddress(values);
const shown = tried ? errors : {};
```

### Syntax: blocking a bad submit

```jsx
function handleSubmit(e) {
  e.preventDefault();
  setTried(true);
  if (Object.keys(errors).length > 0) return;
  onSubmit(values);
}
```

- `e.preventDefault()` stops the browser's own form submit, which would reload the page.
- `Object.keys(errors).length` counts the problems. If there are any, stop.
- Only a clean form reaches `onSubmit`.

```animate
title: Pressing Save on a half-filled form
kind: scene
nodes:
  - { id: form, label: Address form, emoji: "📝", value: "tried: false" }
  - { id: check, label: validateAddress, emoji: "🔍" }
  - { id: pin, label: PIN code field, emoji: "📮", value: "41100" }
  - { id: order, label: onSubmit, emoji: "📦", value: waiting }
steps:
  - say: The customer typed a 5-digit PIN code and pressed Save.
    focus: [form, pin]
  - say: The form checks the values, the same way it does on every render.
    send: { from: form, to: check, label: values }
  - say: validateAddress returns one problem, for the PIN code.
    send: { from: check, to: form, label: "{ pincode: ... }", back: true }
  - say: tried becomes true, so the errors are shown now. The PIN field gets its message.
    set: { form: "tried: true", pin: "PIN code needs 6 digits." }
  - say: There was an error, so onSubmit is never called. No bad parcel leaves the shop.
    focus: [order]
  - say: The customer adds the missing 7. The next render finds no errors, and the message disappears at once.
    set: { pin: "411007" }
```

### Errors everyone can find

A red message near a field is easy to miss if you can't see colours, or can't see the screen at all. Two attributes link the message to its field for screen readers:

```jsx
<input
  id="pincode"
  aria-invalid={error ? "true" : undefined}
  aria-describedby={error ? "pincode-error" : undefined}
/>
{error && <span id="pincode-error">{error}</span>}
```

```callout jargon
**`aria-invalid="true"`** tells assistive technology that a field's value is wrong. **`aria-describedby`** points at the id of another element whose text describes the field. A screen reader reads "PIN code, invalid, PIN code needs 6 digits." when the customer reaches the box.
```

Passing `undefined` leaves the attribute out completely, which is what a valid field should have.

Also put `noValidate` on the `<form>`. It switches off the browser's own pop-up messages, so your messages are the only ones.

### Clean the values before you send them

The checks allow a space in the phone number and spaces around the name. Clean those up when you submit, so the order system always gets the same shape: `"9876543210"`, not `"98765 43210"`.

```callout tip
Big forms in real projects often use a library such as **React Hook Form**, with a schema library like **Zod** for the rules. They do the same steps you just learned: hold the values, validate, decide when to show errors, and link messages to fields.
```

## Worked example

```jsx
const EMPTY = { name: "", phone: "", line1: "", pincode: "" };

export function AddressForm({ onSubmit }) {
  const [values, setValues] = useState(EMPTY);
  const [tried, setTried] = useState(false);

  const errors = validateAddress(values);
  const shown = tried ? errors : {};

  function handleChange(e) {
    setValues({ ...values, [e.target.name]: e.target.value });
  }

  function handleSubmit(e) {
    e.preventDefault();
    setTried(true);
    if (Object.keys(errors).length > 0) return;
    onSubmit({
      name: values.name.trim(),
      phone: values.phone.replaceAll(" ", ""),
      line1: values.line1.trim(),
      pincode: values.pincode.trim(),
    });
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <Field label="Name" name="name" value={values.name} error={shown.name} onChange={handleChange} />
      <Field label="PIN code" name="pincode" value={values.pincode} error={shown.pincode} onChange={handleChange} />
      <button type="submit">Save address</button>
    </form>
  );
}
```

- `errors` is worked out fresh on every render, so it's never out of date.
- `shown` is empty until the first submit, then it is the real errors.
- `handleSubmit` uses the `errors` from this render, which match the values on screen.
- `onSubmit` gets cleaned values only when nothing is wrong.

## Try it

Open the preview and press Save on the empty form. All four messages appear. Fill in the PIN code and watch only its message disappear. Then type `98765 43210` as the phone and save: the result below the form shows the phone without its space.

Now change `const shown = tried ? errors : {};` to `const shown = errors;` and reload the preview. Every message shouts at you before you've typed a thing. Put it back.

## Recap

- Keep a form's values in one state object, with one change handler that uses `[e.target.name]`.
- A validation function is a plain function that returns an object of messages. Empty means valid.
- Regular expressions check shapes: `/^\d{10}$/` for ten digits, `/^[1-9]\d{5}$/` for a PIN code.
- Work out errors on every render, show them only after the first submit, and block `onSubmit` while any remain.
- Link each message to its field with `aria-invalid` and `aria-describedby`, and send cleaned values.

## Common mistakes

**1. Showing every error straight away.**

```jsx
const errors = validateAddress(values);
// ...
<Field label="Name" error={errors.name} />
```

The empty form opens with four messages before the customer has typed anything.

Fix: keep a `tried` flag and pass `tried ? errors : {}` to the fields.

**2. Forgetting preventDefault.**

```jsx
function handleSubmit(e) {
  setTried(true);
  // ...
}
```

Pressing Save reloads the whole page, and everything typed is lost. The browser did its own old-style form submit.

Fix: call `e.preventDefault();` first.

**3. Keeping the errors in state.**

```jsx
const [errors, setErrors] = useState({});
function handleSubmit(e) {
  e.preventDefault();
  setErrors(validateAddress(values));
}
```

The customer fixes the PIN code, but its message stays until the next submit. The saved errors are from the old values.

Fix: work them out during render, `const errors = validateAddress(values);`, so they always match what's on screen.

**4. Testing the phone with its spaces still in.**

```jsx
if (!/^\d{10}$/.test(phone)) errors.phone = "Phone needs 10 digits, like 98765 43210.";
```

A correct number typed as `98765 43210` is rejected, because a space is not a digit. Customers are told to type it like the example, and it still fails.

Fix: test `phone.replaceAll(" ", "")`.
