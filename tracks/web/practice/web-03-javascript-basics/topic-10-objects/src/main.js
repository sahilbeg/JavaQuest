import { CATALOGUE, priceOf, makeLine, cartTotal } from "./catalogue.js";

console.log("Codes: " + Object.keys(CATALOGUE).join(", "));
console.log("Baby booties: " + priceOf("BTY") + " paise");
console.log(makeLine("AMI", 2));

const lines = [
  { code: "AMI", name: "Amigurumi", paise: 45000, qty: 2 },
  { code: "BTY", name: "Baby booties", paise: 35000, qty: 1 },
];
console.log("Cart total: " + cartTotal(lines) + " paise");
