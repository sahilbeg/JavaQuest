// The quantity stepper on the item page, with proper DOM types.
//
//   <div class="stepper">
//     <button type="button" data-step="-1">-</button>
//     <input name="qty" type="number" value="1">
//     <button type="button" data-step="1">+</button>
//   </div>

/** A whole number from 1 to 10: rounds down, keeps it in range, and turns NaN into 1. */
export function clampQty(value: number): number {
  if (Number.isNaN(value)) {
    return 1;
  }
  return Math.min(10, Math.max(1, Math.floor(value)));
}

/** The <input> with this name inside root. Throws an Error "No input named qty" (with the real name) when there is none. */
export function findInput(root: ParentNode, name: string): HTMLInputElement {
  const input = root.querySelector<HTMLInputElement>(`input[name="${name}"]`);
  if (input === null) {
    throw new Error(`No input named ${name}`);
  }
  return input;
}

/**
 * Wires up a stepper inside root:
 * - a click on a button with data-step adds that step to the quantity;
 * - typing in the qty box (its "input" event) uses the typed number.
 * Either way the quantity goes through clampQty, is written back into the box, and is passed to onChange.
 * Clicks on anything that isn't a button with data-step do nothing.
 * Throws "No input named qty" when root has no qty box.
 */
export function setupStepper(root: HTMLElement, onChange: (qty: number) => void): void {
  const input = findInput(root, "qty");

  const update = (qty: number): void => {
    const clamped = clampQty(qty);
    input.value = String(clamped);
    onChange(clamped);
  };

  root.addEventListener("click", (event) => {
    const target = event.target;
    if (!(target instanceof HTMLButtonElement)) {
      return;
    }
    const step = target.dataset.step;
    if (step === undefined) {
      return;
    }
    update(Number(input.value) + Number(step));
  });

  input.addEventListener("input", () => {
    update(Number(input.value));
  });
}
