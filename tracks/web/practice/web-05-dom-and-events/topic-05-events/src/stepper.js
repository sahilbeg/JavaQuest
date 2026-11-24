// The item page's buttons.

/**
 * Wires a stepper: <div class="stepper"> with button.minus, span.qty and button.plus.
 * The quantity starts at 1 and stays between 1 and max. Returns a function that reads it.
 */
export function setupStepper(stepper, max) {
  let qty = 1;
  const minus = stepper.querySelector(".minus");
  const plus = stepper.querySelector(".plus");
  const shown = stepper.querySelector(".qty");

  function show() {
    // TODO 3: write qty into shown, disable minus when qty is 1, disable plus when qty is max
  }

  // TODO 1: when plus is clicked, add 1 if qty is below max, then show()
  // TODO 2: when minus is clicked, take 1 away if qty is above 1, then show()

  show();
  return () => qty;
}

/** A click writes "Added N to your cart" into status, reading N with readQty() at that moment. */
export function setupAddButton(button, status, readQty) {
  // TODO 4: add a click listener that writes `Added ${readQty()} to your cart` into status
}

/** Pressing Escape in the input empties it. */
export function setupEscapeClears(input) {
  // TODO 5: add a keydown listener; when event.key is "Escape", set input.value to ""
}
