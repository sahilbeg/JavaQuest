# Forms with validation

Make the delivery address form check what the customer typed before the order goes out.

In `src/validateAddress.js`, return an object with one message per bad field (an empty object means all is well). Use these exact messages:

1. **TODO 1:** `name` is empty or only spaces: `Please enter your name.`
2. **TODO 2:** `phone` isn't exactly 10 digits once spaces are removed (so `98765 43210` is fine): `Phone needs 10 digits, like 98765 43210.`
3. **TODO 3:** `line1` is empty or only spaces: `Please enter your house and street.`
4. **TODO 4:** `pincode` (trimmed) isn't 6 digits starting with 1 to 9: `PIN code needs 6 digits.`

In `src/AddressForm.jsx`:

5. **TODO 5:** show no messages until the customer first presses **Save address**. After that, show the current errors, updating as they type.
6. **TODO 6:** on submit, don't let the browser reload the page. If there are errors, stop. Otherwise call `onSubmit` with cleaned values: names trimmed, the phone without spaces, the PIN code trimmed. For example `{ name: "Asha Rao", phone: "9876543210", line1: "12 Mint Lane, Aundh", pincode: "411007" }`.
7. **TODO 7:** a field with an error gets `aria-invalid="true"` and `aria-describedby` set to its message's id. A field without an error has neither.
