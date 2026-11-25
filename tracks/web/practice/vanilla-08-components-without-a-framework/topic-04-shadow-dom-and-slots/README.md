# Shadow DOM and slots

Build `<dhaaga-card>` in `src/dhaaga-card.js`: its look lives in its own shadow root, and the page fills it through slots.

```html
<dhaaga-card sale>
  <h3 slot="title">Golu the elephant</h3>
  <p>Crocheted in Indigo, about 15 cm tall.</p>
  <p slot="price">₹450.00</p>
</dhaaga-card>
```

1. **TODO 1:** in the constructor, attach an **open** shadow root, once, and put `CARD_HTML` inside it.
2. **TODO 2:** `CARD_HTML` starts with a `<style>` that has a `:host` rule with `display: block`. Everything else sits inside `<article part="card">`.
3. **TODO 3:** inside the article, in this order: a `<header>` holding `<slot name="title">`, a default `<slot>` (no name), and a `<footer>` holding `<slot name="price">Price on request</slot>`. Keep the badge span that is already there.
4. **TODO 4:** the shadow root's `.badge` ("Diwali offer") shows only while the card has a `sale` attribute, and hides again when it is removed.

Moving a card to another place on the page must not throw.

Open the preview: the page's dashed purple `article` rule can't reach the card's own `<article>`. The slotted `<h3>` is still the page's element, so the page's `h3` rule still paints it.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
