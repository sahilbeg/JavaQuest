import { el } from "./dom.js";
import { rupees } from "./money.js";

export const STEPS = ["Address", "Payment", "Done"];
const HEADINGS = { Address: "Delivery address", Payment: "Pay by UPI", Done: "Thank you!" };

/** The first problem with an address, or "" when it is fine. */
export function addressProblem({ name, phone, pin }) {
  if (name.trim() === "") {
    return "Enter your name.";
  }
  if (!/^\d{10}$/.test(phone.replace(/\s/g, ""))) {
    return "Enter a 10-digit phone number.";
  }
  if (!/^\d{6}$/.test(pin.trim())) {
    return "Enter a 6-digit PIN code.";
  }
  return "";
}

/** True for UPI IDs like meera@okaxis: two or more letters, digits, dots, dashes or underscores, an @, then two or more letters. */
export function isValidUpiId(text) {
  return /^[\w.-]{2,}@[A-Za-z]{2,}$/.test(text.trim());
}

/** One labelled input. */
function field(id, label, attributes = {}) {
  return el("p", { class: "field" }, el("label", { for: id }, label), el("input", { id, name: id, ...attributes }));
}

/** Draws the three checkout steps into section. */
export function mountCheckout(section, { totalPaise, orderId, onPlaced, onBack }) {
  const stepItems = STEPS.map((step) => el("li", {}, step));
  const heading = el("h2", { id: "checkout-heading", tabindex: "-1" });
  const error = el("p", { id: "checkout-error", role: "alert" });
  const body = el("div", { id: "checkout-body" });
  section.replaceChildren(el("ol", { class: "steps", "aria-label": "Checkout steps" }, ...stepItems), heading, error, body);

  function backButton() {
    const button = el("button", { type: "button", class: "back" }, "Back to the shop");
    button.addEventListener("click", () => onBack());
    return button;
  }

  function addressForm() {
    const form = el(
      "form",
      { novalidate: true },
      field("name", "Name", { autocomplete: "name" }),
      field("phone", "Phone", { type: "tel", autocomplete: "tel" }),
      field("pin", "PIN code", { inputmode: "numeric", autocomplete: "postal-code" }),
      el("button", { type: "submit" }, "Continue"),
      backButton(),
    );
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      const problem = addressProblem({
        name: form.querySelector("#name").value,
        phone: form.querySelector("#phone").value,
        pin: form.querySelector("#pin").value,
      });
      if (problem !== "") {
        error.textContent = problem;
        return;
      }
      showStep("Payment");
    });
    return form;
  }

  function paymentForm() {
    const form = el(
      "form",
      { novalidate: true },
      el("p", {}, `Total: ${rupees(totalPaise)}`),
      field("upi", "UPI ID", { autocomplete: "off", placeholder: "name@bank" }),
      el("button", { type: "submit" }, `Pay ${rupees(totalPaise)}`),
    );
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      if (!isValidUpiId(form.querySelector("#upi").value)) {
        error.textContent = "Enter a UPI ID like name@bank.";
        return;
      }
      onPlaced({ orderId, totalPaise, payment: "UPI" });
      showStep("Done");
    });
    return form;
  }

  function donePanel() {
    return el("div", {}, el("p", {}, `Your order ${orderId} is placed. We'll send updates on WhatsApp.`), backButton());
  }

  function showStep(step) {
    heading.textContent = HEADINGS[step];
    error.textContent = "";
    body.replaceChildren(step === "Address" ? addressForm() : step === "Payment" ? paymentForm() : donePanel());
    stepItems.forEach((item, index) => {
      if (STEPS[index] === step) {
        item.setAttribute("aria-current", "step");
      } else {
        item.removeAttribute("aria-current");
      }
    });
    heading.focus();
  }

  showStep("Address");
}
