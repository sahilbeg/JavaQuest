# Creating and removing elements

Build the counter's order slip in `src/slip.js`. An item looks like `{ code: "KEY", name: "Keychain", qty: 2 }`.

1. **TODO 1:** `slipLine(item)` returns a new line, **not** added to the page:

   ```html
   <li class="line" data-code="KEY">
     <span class="name">Keychain</span> × <span class="qty">2</span>
     <button type="button" class="remove" aria-label="Remove Keychain">Remove</button>
   </li>
   ```

   Put all text in with `textContent`, so a name with tags shows as text.
2. **TODO 2:** `addLine(slip, item)` adds a line for the item at the end of the slip, and returns that line.
3. **TODO 3:** `removeLine(slip, code)` removes the slip's line with that `data-code` and returns `true`. If the slip has no such line, it changes nothing and returns `false`.
4. **TODO 4:** `clearSlip(slip)` removes every line. The slip itself stays on the page.

The Remove buttons don't do anything yet: the next ticket wires up clicks. Open the preview to see the slip that `main.js` builds.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
