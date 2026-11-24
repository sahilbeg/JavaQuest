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
    shown.textContent = String(qty);
    minus.disabled = qty <= 1;
    plus.disabled = qty >= max;
  }

  plus.addEventListener("click", () => {
    if (qty < max) {
      qty += 1;
    }
    show();
  });

  minus.addEventListener("click", () => {
    if (qty > 1) {
      qty -= 1;
    }
    show();
  });

  show();
  return () => qty;
}

/** A click writes "Added N to your cart" into status, reading N with readQty() at that moment. */
export function setupAddButton(button, status, readQty) {
  button.addEventListener("click", () => {
    status.textContent = `Added ${readQty()} to your cart`;
  });
}

/** Pressing Escape in the input empties it. */
export function setupEscapeClears(input) {
  input.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      input.value = "";
    }
  });
}
