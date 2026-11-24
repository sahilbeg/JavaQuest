# Refs: focus and measuring

Fix Sana's three small things with refs.

**Search** (`src/SearchBar.jsx`)

1. **TODO 1:** make a ref with `useRef(null)` and attach it to the input.
2. **TODO 2:** pressing **Search** moves the focus into the input.
3. **TODO 3:** pressing **Clear** empties the box (already done) and also moves the focus back into it.

**Product card** (`src/ProductCard.jsx`)

4. **TODO 4:** after the card is on the page, measure its width with `getBoundingClientRect()`. Under 400 pixels, its `data-layout` is `stack`; otherwise it's `row`. Measure before the browser paints.

**Notify me** (`src/NotifyButton.jsx`)

5. **TODO 5:** a second tap cancels the first tap's timer, so the message stays for a full 3 seconds after the latest tap.
6. **TODO 6:** keep the timer's id in a ref, and cancel a waiting timer when the button leaves the page.
