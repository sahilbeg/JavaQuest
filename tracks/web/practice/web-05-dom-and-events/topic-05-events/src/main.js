// Wires the item page. The checks never load this file.
import { setupStepper, setupAddButton, setupEscapeClears } from "./stepper.js";

const readQty = setupStepper(document.querySelector(".stepper"), 5);
setupAddButton(document.querySelector("#add"), document.querySelector("#status"), readQty);
setupEscapeClears(document.querySelector("#note"));
