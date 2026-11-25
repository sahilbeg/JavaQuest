# Typed form validation

Make the custom-order form check every field kindly, and send Meera Aunty a clean, typed request.

In `src/request.ts` (the types, `COLOURS`, `isColour` and `EMPTY_FORM` are done):

1. **TODO 1:** `validateRequest(form)` returns one message per bad field, and `{}` when all is well:

   | Field | Wrong when | Message |
   |---|---|---|
   | `name` | empty after trimming | `Please enter your name.` |
   | `phone` | not exactly 10 digits once spaces are removed | `Phone needs 10 digits, like 98765 43210.` |
   | `qty` | not whole digits after trimming, or below 1, or above 20 | `Order between 1 and 20.` |
   | `stitchedName` | longer than 12 characters after trimming | `Names can be up to 12 letters.` |

2. **TODO 2:** `toRequest(form)` returns the clean `CustomRequest`: the name trimmed, the phone without spaces, `qty` as a number, and the stitched name trimmed, or `null` when it is empty.

In `src/CustomOrderForm.tsx` (the fields and messages are drawn for you):

3. **TODO 3:** `update(field, value)` sets one field of the form state, keeping the others.
4. **TODO 4:** on submit, remember that the customer tried, so the messages show. When there are no errors, call `onSubmit(toRequest(form))`. Messages show only after the first press of **Send request**.
