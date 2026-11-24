// Wires the order form. The checks never load this file.
import { setupStitchCounter, setupOrderForm } from "./order-form.js";

setupStitchCounter(document.querySelector("#stitch"), document.querySelector("#stitch-count"), 12);

setupOrderForm(document.querySelector("#order"), (details) => {
  document.querySelector("#thanks").textContent =
    `Thank you, ${details.name}! We'll call ${details.phone} about delivery to ${details.pin}.`;
});
