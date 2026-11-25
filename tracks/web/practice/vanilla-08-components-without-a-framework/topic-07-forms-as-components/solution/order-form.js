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
  const errors = {};
  if (values.name.trim() === "") {
    errors.name = "Please enter your name.";
  }
  if (!/^\d{10}$/.test(values.phone.replace(/\s/g, ""))) {
    errors.phone = "Please enter a 10-digit phone number.";
  }
  const qty = Number(values.qty);
  if (!Number.isInteger(qty) || qty < 1 || qty > 20) {
    errors.qty = "Choose between 1 and 20 pieces.";
  }
  return errors;
}

export class CustomOrderForm extends HTMLElement {
  connectedCallback() {
    if (this.querySelector("form") !== null) {
      return;
    }
    this.innerHTML = formHtml(`order-${nextId++}`);
    this.querySelector("form").addEventListener("submit", (event) => this.#onSubmit(event));
  }

  #onSubmit(event) {
    const form = this.querySelector("form");
    event.preventDefault();
    const field = (name) => form.elements.namedItem(name);
    const values = Object.fromEntries(FIELDS.map((name) => [name, field(name).value]));
    const errors = validateOrder(values);

    for (const name of FIELDS) {
      const input = field(name);
      this.querySelector(`#${input.id}-error`).textContent = errors[name] ?? "";
      if (errors[name] === undefined) {
        input.removeAttribute("aria-invalid");
      } else {
        input.setAttribute("aria-invalid", "true");
      }
    }

    const firstWrong = FIELDS.find((name) => errors[name] !== undefined);
    if (firstWrong !== undefined) {
      field(firstWrong).focus();
      return;
    }

    this.dispatchEvent(new CustomEvent("order-request", {
      detail: { name: values.name.trim(), phone: values.phone.replace(/\s/g, ""), qty: Number(values.qty) },
      bubbles: true,
    }));
    form.reset();
  }
}

if (!customElements.get("custom-order-form")) {
  customElements.define("custom-order-form", CustomOrderForm);
}
