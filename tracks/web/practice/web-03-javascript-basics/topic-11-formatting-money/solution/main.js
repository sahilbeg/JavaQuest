import { plainRupees, formatINR, priceTag, receiptRow } from "./money.js";

console.log(plainRupees(15050));
console.log(formatINR(129900));
console.log(priceTag("Tote bag", 89900));
console.log(receiptRow("Keychain", 12000));
console.log(receiptRow("Delivery", 6000));
console.log(receiptRow("Total", 18000));
