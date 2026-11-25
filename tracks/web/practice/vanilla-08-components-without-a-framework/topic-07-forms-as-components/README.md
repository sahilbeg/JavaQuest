# Forms as components

Finish `<custom-order-form>` in `src/order-form.js`. It already draws its form (in its light DOM, from `formHtml`) with three fields named `name`, `phone` and `qty`. Each field has an error paragraph, linked to it with `aria-describedby`.

1. **TODO 1:** `validateOrder({ name, phone, qty })` gets the three values as strings and returns an errors object. An empty object means everything is fine. The messages must be exactly:
   - `name`: `Please enter your name.` when it is empty or only spaces.
   - `phone`: `Please enter a 10-digit phone number.` unless it is exactly 10 digits once spaces are removed (`98765 43210` is fine).
   - `qty`: `Choose between 1 and 20 pieces.` unless it is a whole number from 1 to 20.
2. **TODO 2:** on submit, stop the browser from sending the form (no reload), and read the three values.
3. **TODO 3:** for each field: put its message in its error paragraph and set `aria-invalid="true"`, or, when it is fine, empty the paragraph and remove `aria-invalid`.
4. **TODO 4:** when there are errors, move focus to the first wrong field, in the order name, phone, qty. Nothing is sent.
5. **TODO 5:** when there are none, dispatch an `order-request` CustomEvent that bubbles, with `detail` `{ name, phone, qty }`: the name trimmed, the phone as digits only, and the quantity as a number. Then reset the form.
6. **TODO 6:** draw the form only the first time the element joins the page, so moving it keeps what the customer typed.

Open the preview and try a bad phone number, then a good order.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
