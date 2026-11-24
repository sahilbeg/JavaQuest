## Hook

At the shop, Raju keeps a pad of order slips. Each slip has printed boxes: "Name", "Phone", "What would you like?". A customer fills them in, and Raju clips the slip to the order board.

A web **form** is that order slip. It has boxes with printed labels, and a button that sends the filled-in slip to Dhaaga. Right now the catalogue page has nowhere to type.

## Concept

A form collects what a visitor types and sends it to a **server**, the computer that runs the website. Each box in the form is a **field**. When the visitor presses the send button, the browser gathers every field's value and sends them all in one go.

```callout jargon
A **field** (or **form control**) is one place to type or choose something, like a text box. A **server** is the computer that receives the form and does something with it, like saving the order.
```

### Syntax: a form

```html
<form action="/orders" method="post">
  ...fields and a button...
</form>
```

`form` wraps all the fields that are sent together. Two attributes say where and how they go:

- `action` is the URL the form sends to. Here it's `/orders` on Dhaaga's own site.
- `method` is how it's sent. `post` sends the values hidden inside the request, which suits orders. `get` adds them to the end of the URL, which suits searches.

```callout tip
Dhaaga's orders server isn't built yet: that comes in a later project. If you press Send in the preview, you'll see an error page. That's expected. Today you build the form itself.
```

### Syntax: a text input

```html
<input type="text" id="order-name" name="name">
```

`input` makes a field. It's a void element, so it has no closing tag. Its `type` says what kind of field it is. `text` is a one-line text box.

The `name` attribute is the most important one. It's the label the server sees. If a customer types "Asha", the form sends `name=Asha`. **A field with no `name` is never sent at all.**

```callout jargon
A **name/value pair** is a field's `name` joined to whatever the visitor typed, like `phone=9876543210`. A form sends one pair per field.
```

### Syntax: a label

```html
<label for="order-name">Your name</label>
<input type="text" id="order-name" name="name">
```

A `label` is the printed word next to a box. To join a label to its field, give the field an `id`, and give the label a `for` with the **same** value.

Joining them does two things:

- A screen reader reads "Your name, edit text" when the customer reaches the box. Without the join, it says only "edit text".
- Clicking or tapping the label puts the cursor in the box. That's a bigger target on a small phone.

You can also wrap the field inside its label. That joins them too, without `for` and `id`:

```html
<label>Your name <input type="text" name="name"></label>
```

### Placeholder is not a label

```html
<input type="text" name="name" placeholder="e.g. Asha Patil">
```

`placeholder` puts grey hint text inside an empty box. It vanishes the moment someone types, so they forget what the box was for. Many screen readers don't read it reliably either. Use a placeholder for an example, never in place of a `label`.

### Syntax: a textarea

```html
<label for="order-message">What would you like?</label>
<textarea id="order-message" name="message" rows="4"></textarea>
```

`textarea` is a box for several lines of text, like a message. Unlike `input`, it has a closing tag. Any text between the tags becomes the starting value, so leave it empty.

`rows` sets how many lines tall the box is. People can still type more: the box scrolls.

### Syntax: a submit button

```html
<button type="submit">Send order</button>
```

A `button` with `type="submit"` sends its form. Pressing Enter in a text field does the same. The text inside says what will happen.

A `button` with `type="button"` does nothing by itself. JavaScript gives it a job later. Inside a form, always write the `type`, so it's clear which one you meant.

### What happens when you press Send

```animate
title: A form goes to the server
kind: scene
nodes:
  - { id: form, label: "Quick order form", emoji: "📝", row: 0, col: 0, value: "empty" }
  - { id: browser, label: Browser, emoji: "🌐", row: 0, col: 2 }
  - { id: server, label: "dhaaga.in /orders", emoji: "🖥️", row: 0, col: 4 }
  - { id: board, label: "Order board", emoji: "📌", row: 1, col: 4 }
steps:
  - say: Mr. Kulkarni types his name, his phone number and "Two tote bags, please".
    set: { form: "filled in" }
    focus: [form]
  - say: He presses Send order. That's a submit button, so the browser takes over.
    send: { from: form, to: browser, label: "submit" }
  - say: The browser collects one pair per field that has a name, like name=Kulkarni and phone=9876543210.
    set: { browser: "3 pairs" }
  - say: It reads the form's action and method, and sends a POST request to /orders.
    send: { from: browser, to: server, label: "POST /orders" }
  - say: The server saves the order, like a slip clipped to the board.
    show: [board]
    send: { from: server, to: board, label: "new order" }
  - say: It answers with a thank-you page, which the browser shows.
    send: { from: server, to: browser, label: "200 OK, thank you", back: true }
```

### Keeping each field on its own line

`input`, `label` and `button` sit inside a line of text, like words. Wrap each label and its field in a `p` or a `div` to put each pair on its own line. CSS can arrange them better later.

## Worked example

Here's a small form for the Diwali pop-up's mailing list:

```html
<form action="/newsletter" method="post">
  <p>
    <label for="signup-name">Your name</label>
    <input type="text" id="signup-name" name="name">
  </p>
  <p>
    <label for="signup-wish">What would you like to see at the pop-up?</label>
    <textarea id="signup-wish" name="wish" rows="3"></textarea>
  </p>
  <button type="submit">Keep me posted</button>
</form>
```

1. The form sends to `/newsletter` with `post`.
2. Each label's `for` matches its field's `id`: `signup-name` and `signup-wish`.
3. Each field has a `name`, so the server gets `name=...` and `wish=...`.
4. The `textarea` is three lines tall and starts empty.
5. The submit button's text says what it does.

## Try it

Paste the worked example into the editor's body:

- Click the words "Your name". The cursor jumps into the box, because the label is joined to it.
- Change the label's `for` to `signup-nme`, with a typo. Click the words again. Nothing happens now. Fix it.
- Add `placeholder="e.g. Asha Patil"` to the name input. Type something and watch the hint vanish.

## Recap

- A `form` wraps fields. `action` says where they're sent, and `method` says how.
- Every field needs a `name`, or its value is never sent.
- Join a `label` to its field with matching `for` and `id`, or by wrapping the field in the label.
- A placeholder is a hint, not a label.
- `textarea` takes several lines, and `<button type="submit">` sends the form.

## Common mistakes

**1. A field with no name.**

```html
<input type="text" id="order-phone">
```

The customer types a number, but the server never receives it. The form only sends fields that have a `name`. Add `name="phone"`.

**2. A label whose for doesn't match the id.**

```html
<label for="phone">Phone number</label>
<input type="text" id="order-phone" name="phone">
```

`for` must match the field's `id`, not its `name`. Here they don't match, so the screen reader says just "edit text". Write `for="order-phone"`.

**3. Using a placeholder instead of a label.**

```html
<input type="text" name="name" placeholder="Your name">
```

Once the customer starts typing, the hint is gone, and screen readers may never say it. Add a real `<label>` and keep the placeholder for an example, if any.

**4. Writing textarea as a void element.**

```html
<textarea id="order-message" name="message">
<button type="submit">Send order</button>
```

Without `</textarea>`, everything after it, even the button, turns into text inside the box. Always close it: `<textarea ...></textarea>`.
