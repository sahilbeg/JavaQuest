# Forms: sending what the customer types

Build the quick order form at the bottom of `index.html`, in the Quick order section.

1. **TODO 1:** give the `<form>` the attributes `action="/orders"` and `method="post"`.
2. **TODO 2:** in a `<p>`, add `<label for="order-name">Your name</label>` and a text input with `id="order-name"` and `name="name"`.
3. **TODO 3:** in a `<p>`, add a label `Phone number` and a text input with `id="order-phone"` and `name="phone"`.
4. **TODO 4:** in a `<p>`, add a label `What would you like?` and a `<textarea>` with `id="order-message"`, `name="message"` and `rows="4"`. Leave it empty, and remember `</textarea>`.
5. **TODO 5:** add `<button type="submit">Send order</button>` at the end of the form.

Each label's `for` must match its field's `id`. Click a label in the preview: the cursor should jump into its box. The orders server isn't built yet, so pressing Send in the preview shows an error page.
