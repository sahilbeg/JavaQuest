// Wires the checkout form for the preview. The checks never load this file.
import { checkoutRules, validate, showErrors, focusFirstError } from "./validation.js";

const form = document.querySelector("#checkout");
const done = document.querySelector("#done");

form.addEventListener("submit", (event) => {
  event.preventDefault();
  const values = Object.fromEntries(new FormData(form));
  const errors = validate(values, checkoutRules);
  showErrors(form, errors);
  done.textContent = focusFirstError(form, errors) === null ? "Thank you! Your order is on its way to Meera Aunty." : "";
});
