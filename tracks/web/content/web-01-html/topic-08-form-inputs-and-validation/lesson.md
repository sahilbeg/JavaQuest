## Hook

Raju's paper order slips have a trick. The phone box has exactly ten little squares, one per digit. The item box isn't a blank line: it's a row of tick boxes, one per product. Nobody can write "lots" as the item, and a five-digit phone number leaves squares empty, so Raju spots it at once.

Web forms can do the same. HTML has fields made for phone numbers, numbers and choices, and the browser can check them before anything is sent.

## Concept

So far every field is a plain text box, so customers can type anything. Today you'll pick the right **kind** of field for each question, and switch on the browser's own checks.

```callout jargon
**Validation** means checking that what someone typed makes sense before you use it: the phone has ten digits, the quantity is a number, nothing important is empty.
```

### Syntax: input types

```html
<input type="tel" id="order-phone" name="phone">
```

The `type` attribute changes what a field is. A few useful ones:

| type | For | What it does |
|---|---|---|
| `text` | Any short text | A plain one-line box |
| `tel` | Phone numbers | Phones show a number keypad |
| `email` | Email addresses | Checks there's an `@` and a domain |
| `number` | Amounts | Only numbers; up and down arrows |
| `date` | Dates | A calendar to pick from |

On a phone, the right type brings up the right keyboard. That alone saves customers a lot of fiddly typing.

```callout warning
`tel` doesn't check the number by itself, because phone numbers differ around the world. You add the check with `pattern`, below.
```

### Syntax: required

```html
<input type="text" id="order-name" name="name" required>
```

`required` means the field can't be left empty. It's a **boolean attribute**: it has no value. Being there switches it on, and leaving it out switches it off.

```callout jargon
A **boolean attribute** is an on/off switch. Writing `required` turns it on. Don't write `required="false"`: the attribute is still there, so it's still on.
```

If the customer presses Send with a required field empty, the browser stops. It shows a little message next to the field, like "Please fill out this field.", and sends nothing. The exact words depend on the browser. Watch it happen:

```animate
title: The browser checks before it sends
kind: scene
nodes:
  - { id: name, label: "Name (required)", emoji: "👤", row: 0, col: 0, value: "Asha" }
  - { id: phone, label: "Phone (pattern)", emoji: "📞", row: 1, col: 0, value: "98765" }
  - { id: browser, label: Browser, emoji: "🌐", row: 0, col: 2 }
  - { id: server, label: "/orders", emoji: "🖥️", row: 0, col: 4, value: "waiting" }
steps:
  - say: Asha fills in her name but types only five digits for her phone. She presses Send order.
    focus: [name, phone]
  - say: Before sending anything, the browser checks every field against its rules.
    send: [{ from: name, to: browser, label: "Asha" }, { from: phone, to: browser, label: "98765" }]
  - say: The name is there, so required is happy. But 98765 doesn't match the pattern of ten digits.
    set: { browser: "phone fails" }
    focus: [phone]
  - say: The browser stops. It shows a message by the phone box and sends nothing to the server.
    send: { from: browser, to: phone, label: "Please match the requested format.", back: true }
  - say: Asha adds the missing digits and presses Send again.
    set: { phone: "9876543210", browser: "all good" }
  - say: Every check passes now, so the order goes to the server.
    send: { from: browser, to: server, label: "POST /orders" }
    set: { server: "new order" }
```

### Syntax: min, max and step

```html
<input type="number" id="order-qty" name="qty" min="1" max="10" value="1">
```

On a `number` field, `min` and `max` set the smallest and largest allowed values. Here a customer can order 1 to 10, and a quick order can't ask for 0 or 500.

`value` sets the starting value, so the box already says 1. `step` sets the gap between allowed values; it's 1 by default, so only whole numbers pass.

For text, `minlength` and `maxlength` limit how many characters can be typed. `maxlength="500"` on a message box stops an essay.

### Syntax: pattern

```html
<input type="tel" id="order-phone" name="phone" pattern="[0-9]{10}">
```

`pattern` gives the exact shape the value must have. It uses a small language called a **regular expression**. You only need two pieces today:

- `[0-9]` means "one digit, from 0 to 9".
- `{10}` means "exactly ten of the thing before me".

So `[0-9]{10}` means exactly ten digits, and nothing else. `9876543210` passes. `98765`, `98765 43210` (with a space) and `9876abcdef` all fail.

```callout jargon
A **regular expression** (or **regex**) is a short code that describes a shape of text, like "ten digits". You'll use them more in JavaScript later.
```

The pattern must match the **whole** value. An empty field skips the pattern check, so add `required` too if the field must be filled.

### Syntax: a select

```html
<select id="order-item" name="item" required>
  <option value="">Choose an item</option>
  <option value="CST">Coaster set (4)</option>
  <option value="KEY">Keychain</option>
</select>
```

`select` makes a drop-down list. Each `option` is one choice. The text between the tags is what the customer sees. The `value` is what the server receives, so it gets the short code `CST`, not the long name.

The first option has an empty value. With `required`, the customer can't send the form until they pick a real item.

### Syntax: radio buttons

```html
<fieldset>
  <legend>Delivery</legend>
  <input type="radio" id="delivery-pickup" name="delivery" value="pickup">
  <label for="delivery-pickup">Pick up at the Aundh shop</label>
  <input type="radio" id="delivery-pune" name="delivery" value="pune">
  <label for="delivery-pune">Deliver in Pune</label>
</fieldset>
```

**Radio buttons** let the customer pick exactly one choice from a small group. Picking one un-picks the others, like the buttons on an old radio.

- Every radio in a group shares the **same `name`**. That's what makes them one group.
- Each has its own `value`, which is what gets sent: `delivery=pune`.
- Each needs its own label, joined with `for` and `id`.
- Add `checked` to one of them to pick it at the start.

`fieldset` draws a box around related fields, and `legend` gives that box a title. A screen reader reads the legend with each radio: "Delivery, Deliver in Pune, radio button, 2 of 2". Without it, "Deliver in Pune" has no question attached.

### Syntax: a checkbox

```html
<input type="checkbox" id="order-wrap" name="giftWrap" value="yes">
<label for="order-wrap">Gift wrap (₹30)</label>
```

A **checkbox** is a yes/no choice that stands alone. Ticking it sends `giftWrap=yes`. Leaving it empty sends nothing at all for that field. Use checkboxes when the customer may pick none, one or several.

### The browser's check is not enough

Built-in validation is a big help for honest customers who make typos. But anyone can switch it off in DevTools, or send a request without the page at all. The server must always check everything again. You'll build those checks in later projects.

## Worked example

Here's a booking form for the Diwali crochet workshop:

```html
<form action="/workshop" method="post">
  <p>
    <label for="ws-email">Email</label>
    <input type="email" id="ws-email" name="email" required>
  </p>
  <p>
    <label for="ws-seats">Seats</label>
    <input type="number" id="ws-seats" name="seats" min="1" max="4" value="1">
  </p>
  <fieldset>
    <legend>Session</legend>
    <input type="radio" id="ws-morning" name="session" value="morning" checked>
    <label for="ws-morning">Morning, 10:00</label>
    <input type="radio" id="ws-evening" name="session" value="evening">
    <label for="ws-evening">Evening, 17:00</label>
  </fieldset>
  <button type="submit">Book</button>
</form>
```

1. `type="email"` checks for an `@`, and `required` stops an empty box.
2. Seats is a `number` from 1 to 4, starting at 1.
3. The two radios share `name="session"`, so picking one un-picks the other. Morning starts picked.
4. The `legend` tells screen reader users that the two choices answer "Session".

## Try it

Paste the worked example into the editor's body, then press **Book** in the preview each time:

- Leave the email empty. The browser stops you with a message.
- Type `asha` with no `@`. The browser stops you again.
- Set seats to 9. Too many: `max` is 4.
- Change one radio's `name` to `slot`. Now you can pick both, because they're no longer one group. Change it back.

## Recap

- Pick the right `type`: `tel`, `email`, `number` and `date` give better keyboards and checks.
- `required` stops an empty field. `min` and `max` limit numbers. `pattern` sets an exact shape, like `[0-9]{10}`.
- A `select` gives a drop-down. Each `option`'s `value` is what gets sent.
- Radio buttons share one `name` and pick exactly one choice. Group them in a `fieldset` with a `legend`.
- A checkbox is a separate yes/no choice. The browser's checks help, but the server must check again.

## Common mistakes

**1. Radio buttons with different names.**

```html
<input type="radio" id="delivery-pickup" name="pickup" value="pickup">
<input type="radio" id="delivery-pune" name="pune" value="pune">
```

Each radio is now its own group, so a customer can pick both, and neither can be un-picked. Give every radio in the group the same name: `name="delivery"`.

**2. Thinking tel checks the number.**

```html
<input type="tel" name="phone" required>
```

This accepts "lots" as a phone number. `tel` only changes the keyboard. Add `pattern="[0-9]{10}"` to check the shape.

**3. Writing required="false".**

```html
<input type="email" name="email" required="false">
```

The field is still required. A boolean attribute is on whenever it's present, whatever its value. To make the field optional, delete `required` completely.

**4. Options with no value.**

```html
<option>Coaster set (4)</option>
```

With no `value`, the browser sends the visible text, so the server gets `item=Coaster set (4)` instead of the code. Write `<option value="CST">Coaster set (4)</option>`.
