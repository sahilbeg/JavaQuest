// <custom-order-form>: a custom-order form that checks itself and sends an order-request event.

let nextId = 1;

/** The form's markup. prefix keeps the ids unique when a page has two forms. */
export function formHtml(prefix) {
  return `
    <form novalidate>
      <label for="${prefix}-name">Your name</label>
      <input id="${prefix}-name" name="name" autocomplete="name" aria-describedby="${prefix}-name-error">
      <p id="${prefix}-name-error" class="error"></p>

      <label for="${prefix}-phone">Phone</label>
      <input id="${prefix}-phone" name="phone" type="tel" autocomplete="tel" aria-describedby="${prefix}-phone-error">
      <p id="${prefix}-phone-error" class="error"></p>

      <label for="${prefix}-qty">How many pieces</label>
      <input id="${prefix}-qty" name="qty" type="number" min="1" max="20" value="1" aria-describedby="${prefix}-qty-error">
      <p id="${prefix}-qty-error" class="error"></p>

      <button type="submit">Send request</button>
    </form>`;
}

/** The fields, in the order they appear. */
const FIELDS = ["name", "phone", "qty"];

/** { name, phone, qty } as strings in; an object with one message per wrong field out. */
export function validateOrder(values) {
  // TODO 1: name: "Please enter your name." when empty after trim;
  //         phone: "Please enter a 10-digit phone number." unless 10 digits once spaces are removed;
  //         qty: "Choose between 1 and 20 pieces." unless Number(qty) is a whole number from 1 to 20
  return {};
}

export class CustomOrderForm extends HTMLElement {
  connectedCallback() {
    // TODO 6: return early when this element already holds a form, so a move doesn't wipe what was typed
    this.innerHTML = formHtml(`order-${nextId++}`);
    this.querySelector("form").addEventListener("submit", (event) => this.#onSubmit(event));
  }

  #onSubmit(event) {
    const form = this.querySelector("form");
    // TODO 2: call event.preventDefault(), then read each field's value with form.elements.namedItem(name).value
    // TODO 3: validate; for each field, put its message (or "") in its error paragraph and set or remove aria-invalid
    // TODO 4: with errors, focus the first wrong field (in the order of FIELDS) and stop
    // TODO 5: without errors, dispatch "order-request" (bubbles: true) with { name: trimmed, phone: digits only, qty: a number },
    //         then form.reset()
  }
}

if (!customElements.get("custom-order-form")) {
  customElements.define("custom-order-form", CustomOrderForm);
}
