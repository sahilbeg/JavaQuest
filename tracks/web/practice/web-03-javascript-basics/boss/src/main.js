import { cartTotal, totalLine } from "./cart.js";

// Mrs. Kapoor's Diwali order: two tote bags, baby booties and a coaster set.
const order = [
  { code: "BAG", qty: 2 },
  { code: "BTY", qty: 1 },
  { code: "CST", qty: 1 },
];

console.log("Paise to ask UPI for: " + cartTotal(order));
console.log(totalLine(order));
