# Typed styling props

Build Sana's two design pieces.

**`Button.tsx`**: its props are every normal `<button>` prop, plus `variant` (`"primary"`, `"secondary"` or `"danger"`) and `size` (`"sm"` or `"md"`).

1. **TODO 1:** Take `variant` (default `"primary"`), `size` (default `"md"`), `className` and `type` (default `"button"`) from the props, and collect the rest with `...rest`.
2. **TODO 2:** Build the classes: `btn`, `btn--<variant>`, `btn--<size>`, then the parent's `className` if there is one. For example `btn btn--danger btn--sm wide`.
3. **TODO 3:** Return `<button type={type} className={classes} {...rest} />`, so children, `onClick`, `disabled` and the rest reach the real button.

**`ColourSwatch.tsx`**

4. **TODO 4:** Give the swatch a `style` typed `CSSProperties`: its `backgroundColor` from `SWATCH_HEX`, and `width` and `height` from `size` (default 24, in pixels).

Press **Submit** to run the checks.
