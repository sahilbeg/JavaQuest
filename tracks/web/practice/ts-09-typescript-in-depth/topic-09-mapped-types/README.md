# Mapped and template literal types

The custom-order form's types in `src/orderForm.ts` now follow the form by themselves: `FormErrors<T>` is a mapped type, and `ItemId` is a template literal type. Write the code that goes with them.

1. **TODO 1:** `isItemId(text)` is true when the text is `DC-` followed by one or more digits, and nothing else.
2. **TODO 2:** `mapValues(obj, fn)` returns a new object with the same keys, where each value is `fn(value, key)`. Don't change `obj`.
3. **TODO 3:** `validateOrder(form)` returns one message per bad field, and `{}` when everything is fine:
   - `name` empty or only spaces: `Please enter your name`
   - `phone` not exactly 10 digits once spaces are removed: `Phone needs 10 digits`
   - `pincode` not exactly 6 digits: `PIN code needs 6 digits`
   - `qty` not a whole number from 1 to 10: `Quantity must be 1 to 10`
4. **TODO 4:** `hasErrors(errors)` is true when there is at least one message.

`src/` is type-checked in strict mode before the checks run.
