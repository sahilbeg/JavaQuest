# The template element and cloning

The card's markup now lives in the page, in `<template id="product-card">` (see `src/index.html`). Fill copies of it from data, in `src/cards.js`. A product looks like `{ id: "DC-1042", name: "Golu the elephant", paise: 45000, photo: "photos/DC-1042.jpg" }`.

`cardFromTemplate(template, product)` returns one filled `<li class="product">`, not yet on the page:

1. **TODO 1:** clone the template's content deeply and take its first element, the `<li>`. Never change the template itself.
2. **TODO 2:** set the card's `data-id`, and the photo's `src` (the product's `photo`) and `alt` (the product's `name`).
3. **TODO 3:** fill `.name` with the name and `.price` with `rupees(paise)` from `money.js`, as plain text. Give the `.add` button the `aria-label` `Add Golu the elephant to cart` (with the product's name).

`renderCards(list, template, products)`:

4. **TODO 4:** the list ends up holding exactly one card per product, in order. Drawing again replaces the old cards.

Open the preview to see the catalogue drawn from the template.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
