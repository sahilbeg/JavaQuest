## What happened

Last month a freelancer built Dhaaga's new order page, `order.html`, in a hurry. It looks fine. Every box has grey hint text, and there's a big "Place order" box at the bottom.

Today Mrs. Desai tried it with her screen reader. This is what she heard:

```animate
title: Mrs. Desai tries the order page
kind: scene
nodes:
  - { id: reader, label: Screen reader, emoji: "🔊", row: 1, col: 0 }
  - { id: photos, label: "3 product photos", emoji: "🖼️", row: 0, col: 2 }
  - { id: fields, label: "4 fields", emoji: "📝", row: 1, col: 2 }
  - { id: div, label: "div: Place order", emoji: "🟧", row: 2, col: 2 }
  - { id: raju, label: "Raju's phone", emoji: "☎️", row: 1, col: 4 }
steps:
  - say: She opens the order page and asks for the list of headings. The first one is level 3, so she wonders what she missed.
    focus: [reader]
  - say: She moves through the page. The photos have no alt, so she hears "coasters dot jpg, golu dot jpg, tote bag dot jpg".
    send: { from: photos, to: reader, label: "golu dot jpg" }
  - say: She reaches the fields. None has a label, and the grey hints aren't read reliably. She hears "edit text" again and again.
    send: { from: fields, to: reader, label: "edit text, edit text" }
  - say: She presses Tab to find the Place order button. Tab skips the div completely, because a div isn't a button.
    send: { from: reader, to: div, label: "Tab? skipped" }
    set: { div: "unreachable" }
  - say: There's no way to send the order. She gives up and phones Raju instead.
    send: { from: reader, to: raju, label: "phone call" }
```

Every one of these problems has an HTML fix you already know from this project.

## What you need

- **Labels**: a `label` joined to each field by matching `for` and `id`.
- **A real button**: `<button type="submit">` inside the form. It can be reached with Tab and pressed with Enter.
- **Alt text**: describe each product photo. Give decoration `alt=""`.
- **Headings in order**: one `h1`, then `h2`, never skipping a level.
- **Landmarks**: `header`, `nav`, `main` and `footer` instead of plain `div`s.

### Syntax: a label for a select

```html
<label for="item">Item</label>
<select id="item" name="item" required>...</select>
```

A `select` needs a label just like an `input`. Its first option, "Choose an item", is not a label: once a real item is picked, it's gone.

```callout tip
Keep every `id`, `name` and attribute that's already in the form. The server and the checks still rely on them. You're adding labels around the fields, not rebuilding them.
```

## The tiers

1. **Bronze** resolves the incident: every field in the form, the three `input`s and the `select`, has a `label` joined to it with words in it. Placeholders don't count.
2. **Silver**: "Place order" is a real `<button type="submit">` inside the form, with no clickable `div` left on the page. Each product photo has alt text that describes it, and the stitched line has `alt=""`.
3. **Gold**: the page has one `h1`, `Place an order`, and no heading skips a level. It has a `header` with a `nav` inside, one `main` holding the `h1` and the form, and a `footer`.

## Recap

- A screen reader can only say what the HTML tells it: labels, alt texts, roles and headings.
- Placeholders, styled `div`s and big text look right, but they say nothing to assistive technology.
- Fix the field labels first: without them, nobody using a screen reader can order at all.

## Common mistakes

**1. Labels that don't match.**

```html
<label for="phone-number">Phone number</label>
<input type="tel" id="phone" name="phone">
```

The label's `for` must match the field's `id` exactly. Here it should be `for="phone"`.

**2. Keeping the div and adding a button too.**

Leave no clickable `div` behind. Delete the `div` and its `onclick`, and use one `<button type="submit">Place order</button>` inside the form.

**3. Making the site name the h1.**

The site's name, Dhaaga & Co., can sit in the `header` as a plain paragraph. This page's `h1` says what the page is for: `Place an order`.
