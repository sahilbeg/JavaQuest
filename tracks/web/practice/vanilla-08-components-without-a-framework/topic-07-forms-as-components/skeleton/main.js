// Puts the form on the page and shows each request it sends. The checks never load this file.
import "./order-form.js";

document.querySelector("#form-spot").append(document.createElement("custom-order-form"));

document.addEventListener("order-request", (event) => {
  const { name, phone, qty } = event.detail;
  document.querySelector("#sent").textContent = `Request sent: ${qty} piece(s) for ${name}, we'll call ${phone}.`;
});
