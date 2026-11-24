import { receipt } from "./calculator.js";

const diwaliOrder = [
  { code: "BAG", qty: 2 },
  { code: "BTY", qty: 1, giftWrap: true },
];

const smallOrder = [
  { code: "CST", qty: 1 },
  { code: "KEY", qty: 2, name: "Riya" },
];

console.log(receipt(diwaliOrder));
console.log("");
console.log(receipt(smallOrder));
