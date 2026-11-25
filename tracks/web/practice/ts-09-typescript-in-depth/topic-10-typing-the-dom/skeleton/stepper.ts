// The quantity stepper on the item page, with proper DOM types.
//
//   <div class="stepper">
//     <button type="button" data-step="-1">-</button>
//     <input name="qty" type="number" value="1">
//     <button type="button" data-step="1">+</button>
//   </div>

/** A whole number from 1 to 10: rounds down, keeps it in range, and turns NaN into 1. */
export function clampQty(value: number): number {
  // TODO 1: NaN gives 1; otherwise round down with Math.floor and keep it between 1 and 10.
  return 0;
}

/** The <input> with this name inside root. Throws an Error "No input named qty" (with the real name) when there is none. */
export function findInput(root: ParentNode, name: string): HTMLInputElement {
  // TODO 2: root.querySelector<HTMLInputElement>(...) may give null. Check before you return it.
  return document.createElement("input");
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
  // TODO 3: find the qty box with findInput.
  // TODO 4: add one click listener to root. Narrow event.target with instanceof HTMLButtonElement and read dataset.step.
  // TODO 5: add an "input" listener to the box that clamps the typed value.
}
