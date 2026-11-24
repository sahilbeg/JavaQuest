## The design

Meera Aunty wants one page where customers ask for something made just for them. Sana drew the form, field by field:

| Field | Element | id and name | Rules |
|---|---|---|---|
| Your name | text input | `name` | required |
| Phone number | tel input | `phone` | required, exactly ten digits |
| Item | select | `item` | required; a "Choose an item" option, then the five items |
| Colour | five radio buttons in a fieldset | name `colour` | one must be picked |
| How many? | number input | `qty` | from 1 to 20, starting at 1 |
| Notes | textarea | `notes` | optional, 5 rows |
| Send request | submit button | | |

The item options use the codes as values: `CST` Coaster set (4), `KEY` Keychain, `AMI` Amigurumi (small toy), `BAG` Tote bag and `BTY` Baby booties.

The colour radios use these values, with these labels: `marigold` Marigold, `indigo` Indigo, `rani-pink` Rani pink, `mint` Mint, `off-white` Off-white. Give each radio an id like `colour-mint`.

```callout tip
This feature uses most of the project: labels, input types, required, pattern, min and max, a select, radio buttons in a fieldset, a textarea and a submit button.
```

Here's what happens to a request once the form is done:

```animate
title: A custom order request
kind: scene
nodes:
  - { id: customer, label: "Mrs. Rao", emoji: "👵", row: 0, col: 0 }
  - { id: form, label: "Request form", emoji: "📝", row: 0, col: 2, value: "empty" }
  - { id: server, label: "/custom-orders", emoji: "🖥️", row: 0, col: 4 }
  - { id: meera, label: "Meera Aunty", emoji: "🧶", row: 1, col: 4 }
steps:
  - say: Mrs. Rao wants Golu in Mint for her grandson's birthday. She fills in the form.
    send: { from: customer, to: form, label: "AMI, Mint, 1" }
    set: { form: "filled in" }
  - say: She forgets to pick a colour at first. The browser stops her, because the colour group is required.
    send: { from: form, to: customer, label: "Please select one of these options.", back: true }
  - say: She picks Mint and presses Send request. Every check passes now.
    send: { from: form, to: server, label: "POST /custom-orders" }
  - say: The server gets name, phone, item=AMI, colour=mint, qty=1 and the notes, all in one place.
    send: { from: server, to: meera, label: "new request" }
  - say: Meera Aunty sees the whole request at once, and starts stitching.
    focus: [meera]
```

## What you build

Write the fields inside the `form` in `index.html`. The page around it, with its header, main and footer, is ready.

### Syntax: a required radio group

```html
<fieldset>
  <legend>Colour</legend>
  <p><input type="radio" id="colour-marigold" name="colour" value="marigold" required> <label for="colour-marigold">Marigold</label></p>
  <p><input type="radio" id="colour-indigo" name="colour" value="indigo"> <label for="colour-indigo">Indigo</label></p>
</fieldset>
```

`required` on one radio makes the whole group required, because they share a `name`. The customer must pick one of them before the form can be sent.

1. Each field sits in its own `p`, with a `label` whose `for` matches the field's `id`.
2. The phone field is `type="tel"` with a `pattern` for exactly ten digits.
3. The colour radios share `name="colour"` and sit in a `fieldset` with the legend `Colour`.
4. The quantity is a `number` with `min="1"`, `max="20"` and `value="1"`.
5. The notes `textarea` has `rows="5"` and isn't required.
6. The form ends with `<button type="submit">Send request</button>`.

## Recap

- Every field gets a label joined by `for` and `id`, so everyone can fill in the form.
- Pick the type that fits each answer: `tel`, `number`, a `select`, or radio buttons.
- Let the browser catch mistakes with `required`, `pattern`, `min` and `max`, and remember the server must check again.

## Common mistakes

**1. Giving each colour radio its own name.**

```html
<input type="radio" id="colour-mint" name="mint" value="mint">
```

Then every colour can be picked at once, and the request may arrive with no colour at all. All five share `name="colour"`.

**2. Forgetting the empty first option.**

```html
<select id="item" name="item" required>
  <option value="CST">Coaster set (4)</option>
</select>
```

The first item is picked from the start, so `required` never stops anyone. Customers who didn't choose get coasters. Start with `<option value="">Choose an item</option>`.

**3. A max that's too small, or missing.**

```html
<input type="number" id="qty" name="qty" min="1">
```

Without `max="20"`, someone can ask for 500 booties. Meera Aunty can stitch at most 20 of anything in one order.
