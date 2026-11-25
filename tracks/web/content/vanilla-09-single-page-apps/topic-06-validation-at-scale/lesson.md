## Hook

When a WhatsApp order comes in, Raju checks it against a little card pinned by the phone: name, phone number, PIN code, address. Same card, same order, every time. If two things are wrong, he asks about the first one, then the next.

Your checkout needs that card. Today each check is written by hand, in a different style, with different messages. This ticket turns them into small rules that every form can share.

## Concept

### The trouble with if after if

A first checkout often looks like this:

```js
function checkCheckout(values) {
  if (values.name === "") return "Invalid!";
  if (values.phone.length < 10) return "Bad phone";
  if (!/^\d{6}$/.test(values.pin)) return "";
  return null;
}
```

It reports only one problem at a time, its messages don't help, and the next form copies it with new mistakes. With three forms in the app, there are three different ideas of what "required" means.

### A rule is a function

```callout jargon
A **validation rule** checks one thing about one value. Here a rule is a function: it takes the typed text and returns an error message, or `null` when the text is fine.
```

```js
const nameNeeded = (value) => (value.trim() === "" ? "Please tell us your name." : null);
```

Returning the message, not `true` or `false`, means the rule carries its own friendly words wherever it is used.

### Syntax: a rule maker

```js
function required(message) {
  return (value) => (value.trim() === "" ? message : null);
}
```

```callout jargon
A **rule factory** is a function that makes rules. You give it the details, such as the message, and it returns a new rule. It works because of closures, from web-04: the returned function remembers `message`.
```

Now `required("Please give your address.")` and `required("Please tell us your name.")` are two rules from one recipe. A second factory, `matches(pattern, message)`, makes rules that test the text with a regular expression.

### A schema: rules for every field

```callout jargon
A **schema** is a plain object that describes what valid data looks like. Here it maps each field's name to its list of rules.
```

```js
const checkoutRules = {
  name: [required("Please tell us your name.")],
  pin: [required("Please give your PIN code."), matches(/^[1-9]\d{5}$/, "A PIN code has 6 digits, like 411004.")],
};
```

Adding a field to a form means adding one line to its schema. The code that runs the checks never changes, which is what lets validation grow to many forms.

### First message wins

Each field's rules run in order. The first message stops that field, so the customer reads one clear problem per box, the most basic one first. An empty PIN says "Please give your PIN code", not "A PIN code has 6 digits".

```animate
title: Running one field's rules
kind: code
language: js
code: |
  const required = (msg) => (v) => (v.trim() === "" ? msg : null);
  const digits = (msg) => (v) => (/^\d+$/.test(v) ? null : msg);
  const rules = [required("Needed"), digits("Digits only")];
  let message = null;
  for (const rule of rules) {
    message = rule("41l004");
    if (message !== null) break;
  }
  console.log(message);
steps:
  - line: 1
    say: required is a rule factory. It makes rules that fail on empty text.
  - line: 2
    say: digits makes rules that fail unless every character is a digit.
  - line: 3
    objects: { r: { type: Array, fields: { "0": "required rule", "1": "digits rule" } } }
    vars: { rules: "@r" }
    say: The field's rule list. Order matters, because the first message wins.
  - line: 4
    vars: { message: "null" }
    say: No message yet.
  - line: 6
    say: 'The customer typed "41l004", with a letter l instead of a 1. The required rule runs first.'
  - line: 7
    say: The text isn't empty, so required gives null, and the loop moves on.
  - line: 6
    vars: { message: '"Digits only"' }
    say: The digits rule spots the letter and gives its message.
  - line: 7
    say: A message! The loop stops here. Any later rules don't run for this field.
  - line: 9
    out: "Digits only"
    say: This is the one message shown under the PIN box.
```

### Syntax: Object.entries for a schema

```js
for (const [field, rules] of Object.entries(checkoutRules)) {
  console.log(field, rules.length);
}
```

`Object.entries(obj)` gives a list of `[key, value]` pairs. Destructuring them in the loop gives each field's name and its rules together.

### Errors everyone can use

A red border alone tells nothing to a screen reader user, or to someone who can't see red well. Each error needs:

- **Words**, in an element right under the field, such as `<span id="phone-error">`.
- **`aria-invalid="true"`** on the field, so screen readers say "invalid".
- **`aria-describedby="phone-error"`** on the field, so the message is read out with it.

When the field is fixed, set `aria-invalid="false"` and empty the message. Old messages that stay after a fix are as confusing as none.

### Move focus to the first problem

After a failed submit, the customer may not even see the errors, which can be off the screen. Move the focus to the first field with an error, in the order the fields appear. The page scrolls to it, the screen reader reads it, and the keyboard is ready to type.

```callout jargon
**Focus** is the element that receives the keyboard right now. `element.focus()` moves it there, and `document.activeElement` tells you where it is.
```

### When to check

- **On submit:** always check every field.
- **When leaving a field** (the `blur` or `focusout` event): check that field, but only once the customer has typed in it. Errors on boxes they haven't reached yet feel like being told off.
- **On every keystroke:** only to clear an error as it gets fixed, never to show a new one mid-word.

```callout warning
The browser's checks help the customer. They don't protect the shop: anyone can send a request without your page. The server must check everything again.
```

## Worked example

The gift-card form has a note and a sender's name. It needs a new rule maker, and reuses the old one:

```js
function maxLength(limit, message) {
  return (value) => (value.trim().length > limit ? message : null);
}

const giftRules = {
  note: [required("Write a short note for the card."), maxLength(80, "Keep the note under 80 letters, so it fits on the card.")],
  from: [required("Who is the gift from?")],
};

const errors = validate({ note: "", from: "Asha" }, giftRules);
```

1. `maxLength` is a third rule factory. It follows the same shape: give details, get back a rule.
2. The schema reuses `required`, so "empty" means the same thing on every form.
3. `validate` is the same function the checkout uses. Here it gives `{ note: "Write a short note for the card." }`.

## Try it

Run this, then add a third rule to `pinRules` that fails for PIN codes outside Pune (they start with `411` or `412`), with its own message. Check `"560001"`, a Bengaluru PIN.

```js run
const required = (message) => (value) => (value.trim() === "" ? message : null);
const matches = (pattern, message) => (value) => (pattern.test(value.trim()) ? null : message);

const pinRules = [required("Please give your PIN code."), matches(/^[1-9]\d{5}$/, "A PIN code has 6 digits.")];

function check(value) {
  for (const rule of pinRules) {
    const message = rule(value);
    if (message !== null) return message;
  }
  return "ok";
}

console.log(check(""));
console.log(check("4110"));
console.log(check("411004"));
```

## Recap

- A rule is a function from the typed value to a message, or `null`. Rule factories like `required(message)` make rules.
- A schema maps each field to its rules. One `validate` function runs any schema, so new forms need no new checking code.
- Each field's rules run in order, and the first message wins.
- Show each message in words under its field, set `aria-invalid`, link it with `aria-describedby`, and clear it once fixed.
- Focus the first field with an error. Check on submit and on leaving a field, and check again on the server.

## Common mistakes

**1. Rules that say only true or false.**

```js
const pinOk = (value) => /^[1-9]\d{5}$/.test(value);
```

The form knows something is wrong but has no words to show, so it falls back to "Invalid!". Fix: return the message when the rule fails, and `null` when it passes.

**2. Letting the last message win.**

```js
for (const rule of fieldRules) {
  errors[field] = rule(value);
}
```

Every rule overwrites the one before, and a passing last rule writes `null` over a real problem. Fix: stop at the first message that isn't `null`.

**3. Showing errors with colour only.**

```css
input.bad { border-color: red; }
```

A screen reader says nothing, and a colour-blind customer sees a normal box. Fix: put the words under the field and set `aria-invalid="true"`.

**4. Forgetting to clear fixed fields.**

```js
for (const [name, message] of Object.entries(errors)) {
  form.querySelector(`#${name}-error`).textContent = message;
}
```

This only touches fields that have errors now. A fixed name still says "Please tell us your name." Fix: go through every field, and write `""` for the ones without an error.
