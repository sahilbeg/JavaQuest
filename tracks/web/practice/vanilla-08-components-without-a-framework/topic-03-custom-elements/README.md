# Custom elements: your own HTML tags

Turn the stock badge into a real tag, `<stock-badge count="3">`, in `src/stock-badge.js`.

1. **TODO 1:** watch the `count` attribute with `static observedAttributes`, so the badge draws again whenever it changes.
2. **TODO 2:** a `count` property: reading it gives the attribute as a number (0 when there is none), and setting it sets the attribute.
3. **TODO 3:** the badge draws itself when it joins the page.
4. **TODO 4:** `render()` draws the badge from its count:
   - `0`: the text `Sold out`, and the class `out`.
   - `1` to `5`: the text `Only 3 left` (with the real number), and the class `low`.
   - above `5`: the text `In stock`, and neither class.
   When the count changes, the old class goes away.
5. **TODO 5:** register the class as the tag `stock-badge`, only if that name isn't registered yet.

Open the preview and move the slider: every badge follows its count.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
