import { itemLabel, readyMessage, cleanCode, initials } from "./labels.js";

console.log(itemLabel("AMI", "Amigurumi"));
console.log(readyMessage("Asha", "order-17"));
console.log("Typed \"  ami \", cleaned: " + cleanCode("  ami "));
console.log("Stitch on the booties: " + initials("riya", "fernandes"));
