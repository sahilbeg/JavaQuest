# Generic components

Build one `PickList<T>` in `PickList.tsx` that all three pickers can share. Kavya has already written its props, `PickListProps<T>`:

- `label`: the text for the `<legend>`
- `options`: the choices, of any type `T`
- `selected`: the chosen option, or `null`
- `getKey(option)`: a unique string for an option
- `renderOption(option)`: what to show inside its button
- `onPick(option)`: called when an option is tapped

Inside the `<fieldset className="pick-list">`, after the legend:

1. **TODO 1:** One `<button type="button">` per option, keyed with `getKey(option)`.
2. **TODO 2:** Inside each button, show `renderOption(option)`.
3. **TODO 3:** Clicking a button calls `onPick` with that option.
4. **TODO 4:** `aria-pressed` is true only for the selected option. Compare keys, not objects, because the selected option may be a different copy.

`main.tsx` uses it for yarn colours (strings) and gift boxes (objects). Hover over the props there to see `T` change. Press **Submit** to run the checks.
