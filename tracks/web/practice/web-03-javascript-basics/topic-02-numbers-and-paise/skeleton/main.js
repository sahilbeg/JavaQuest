import { lineTotal, wholeRupees, leftoverPaise, shareEach } from "./paise.js";

console.log("Two coaster sets: " + lineTotal(15000, 2) + " paise");
console.log("15050 paise is " + wholeRupees(15050) + " rupees and " + leftoverPaise(15050) + " paise");
console.log("₹1,000 split three ways: " + shareEach(100000, 3) + " paise each");
