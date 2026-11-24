import { lineTotal, extrasFor, itemCost, deliveryFor } from "./extras.js";

console.log("One amigurumi: " + lineTotal(45000) + " paise");
console.log("Extras for one wrapped toy named Golu: " + extrasFor(true, "Golu") + " paise");
const cost = itemCost(45000, 2, true, "Golu");
console.log("Two wrapped amigurumi named Golu: " + cost + " paise");
console.log("Delivery for that order: " + deliveryFor(cost) + " paise");
