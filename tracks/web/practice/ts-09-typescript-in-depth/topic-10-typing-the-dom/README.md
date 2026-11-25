# Typing the DOM

Rewrite the item page's quantity stepper in `src/stepper.ts`, with proper DOM types. The stepper looks like this:

```html
<div class="stepper">
  <button type="button" data-step="-1">-</button>
  <input name="qty" type="number" value="1">
  <button type="button" data-step="1">+</button>
</div>
```

1. **TODO 1:** `clampQty(value)` returns a whole number from 1 to 10: round down, keep it in range, and turn `NaN` into 1.
2. **TODO 2:** `findInput(root, name)` returns the `<input>` with that `name` inside `root`. When there is none, throw an `Error` with the message `No input named qty` (with the real name).
3. **TODO 3 to 5:** `setupStepper(root, onChange)` finds the `qty` box, then:
   - listens for clicks on `root`. A click on a button with `data-step` adds that step to the quantity. Clicks on anything else do nothing.
   - listens for the box's `input` event, and uses the typed number.
   - either way, the quantity goes through `clampQty`, is written back into the box, and is passed to `onChange`.

`src/` is type-checked in strict mode before the checks run, so `querySelector` results must be checked for `null`, and `event.target` must be narrowed before use.
