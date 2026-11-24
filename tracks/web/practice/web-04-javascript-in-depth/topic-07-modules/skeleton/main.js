// Press Run to print a sample receipt.
import { receiptLine, deliveryLine } from "./receipt.js";

console.log(receiptLine("AMI", 2));
console.log(receiptLine("KEY", 1));
console.log(deliveryLine(102000));
console.log(deliveryLine(45000));
