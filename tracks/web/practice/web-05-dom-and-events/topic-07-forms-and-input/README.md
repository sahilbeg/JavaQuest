# Forms and input events

Make the order form check what customers type, in `src/order-form.js`. `onlyDigits(text)` is already written for you.

`validate({ name, phone, pin })` returns an object with one message per bad field, and `{}` when everything is fine:

1. **TODO 1:** `name` is empty after trimming: `"Please enter your name."`
2. **TODO 2:** `phone`, with its spaces removed, is not exactly 10 digits: `"Enter a 10-digit phone number."`
3. **TODO 3:** `pin`, trimmed, is not exactly 6 digits: `"Enter a 6-digit PIN code."`

`setupStitchCounter(input, counter, max)` shows how many letters are in the name-to-stitch box:

4. **TODO 4:** `show()` writes `5/12` (length/max) into the counter, and puts the class `over` on the counter only when the length is above `max`.
5. **TODO 5:** `show()` runs on every `input` event.

`setupOrderForm(form, onOrder)` handles the form being sent:

6. **TODO 6:** listen for `submit` on the form, and stop the page from reloading.
7. **TODO 7:** read `name`, `phone` and `pin` with `FormData` and `validate` them. For each of the three fields, write its message (or `""`) into the element with its `data-error-for`, and set its input's `aria-invalid` to `"true"` or `"false"`.
8. **TODO 8:** when there are no errors, call `onOrder({ name, phone, pin })` once, with the name and PIN trimmed and the phone without spaces, like `{ name: "Anjali Deshpande", phone: "9876543210", pin: "411007" }`.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
