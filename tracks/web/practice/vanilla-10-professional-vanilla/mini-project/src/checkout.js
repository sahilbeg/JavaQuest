import { el } from "./dom.js";
import { rupees } from "./money.js";

export const STEPS = ["Address", "Payment", "Done"];
const HEADINGS = { Address: "Delivery address", Payment: "Pay by UPI", Done: "Thank you!" };

/** The first problem with an address, or "" when it is fine. */
export function addressProblem({ name, phone, pin }) {
  // TODO 9: check in this order and return the first problem:
  //   a blank name (after trim)                         -> "Enter your name."
  //   phone, with spaces removed, isn't exactly 10 digits -> "Enter a 10-digit phone number."
  //   pin, trimmed, isn't exactly 6 digits                -> "Enter a 6-digit PIN code."
  //   otherwise "" (no problem)
  return "";
}

/** True for UPI IDs like meera@okaxis: two or more letters, digits, dots, dashes or underscores, an @, then two or more letters. */
export function isValidUpiId(text) {
  // TODO 10: test the trimmed text against a regular expression: [\w.-]{2,} then @ then [A-Za-z]{2,}, and nothing else
  return false;
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
    // TODO 11: mark the current step: aria-current="step" on its <li> in stepItems, and no aria-current on the others.
    //          Then move focus to the heading (it has tabindex="-1"), so a screen reader starts at the new step.
  }

  showStep("Address");
}
