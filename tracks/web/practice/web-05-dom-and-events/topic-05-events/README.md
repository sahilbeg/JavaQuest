# Events: click and friends

Make the item page's buttons work, in `src/stepper.js`.

`setupStepper(stepper, max)` wires one quantity stepper. The quantity starts at 1 and stays between 1 and `max`. It returns a function that reads the current quantity.

1. **TODO 1:** a click on `.plus` adds 1, but never goes above `max`.
2. **TODO 2:** a click on `.minus` takes 1 away, but never goes below 1.
3. **TODO 3:** `show()` writes the quantity into `.qty`, disables `.minus` at 1 and disables `.plus` at `max`. It already runs once at the start.

4. **TODO 4:** `setupAddButton(button, status, readQty)`: a click writes `Added 3 to your cart` into `status`, with the quantity from `readQty()` at the moment of the click.
5. **TODO 5:** `setupEscapeClears(input)`: pressing Escape in the gift note box empties it. Other keys change nothing.

Open the preview and try the + and − buttons.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
