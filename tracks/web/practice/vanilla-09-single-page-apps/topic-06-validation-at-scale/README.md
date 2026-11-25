# Form validation at scale

Replace the checkout's tangle of `if`s with small rules that any form can reuse. Change only `src/validation.js`. `checkoutRules` is already written with your two rule makers.

A **rule** is a function that takes the typed value and returns an error message, or `null` when the value is fine.

1. **TODO 1:** `required(message)` returns a rule that gives `message` for an empty value or one with only spaces.
2. **TODO 2:** `matches(pattern, message)` returns a rule that tests the trimmed value with `pattern.test(...)`, and gives `message` when it doesn't match.
3. **TODO 3:** `validate(values, rules)` runs each field's rules in order on `values[field]` (`""` when missing). The first message wins. It returns an object of only the failing fields, such as `{ phone: "A phone number has 10 digits, like 98765 43210." }`, or `{}`.
4. **TODO 4:** `showErrors(form, errors)` goes through every element with a `name` attribute in the form. It sets `aria-invalid` to `"true"` or `"false"`, and puts the field's message, or `""`, in the element with id `<name>-error`.
5. **TODO 5:** `focusFirstError(form, errors)` focuses the first field with an error, in the order the fields appear in the form, and returns its name. With no errors it returns `null` and leaves focus alone.

Open the preview, press **Place order** with empty boxes, then fix them one at a time.
