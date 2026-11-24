import { sumPaise, countAbove, toysFromYarn, firstIndexOver } from "./cart.js";

const order = [15000, 12000, 12000, 45000, 89900];
console.log("Order total: " + sumPaise(order) + " paise");
console.log("Items above ₹200: " + countAbove(order, 20000));
console.log("Toys from 130 g of yarn: " + toysFromYarn(130));
console.log("First item above ₹400 is at position " + firstIndexOver(order, 40000));
