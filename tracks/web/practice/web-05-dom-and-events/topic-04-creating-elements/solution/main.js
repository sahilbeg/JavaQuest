// Builds a sample slip. The checks never load this file.
import { addLine, removeLine } from "./slip.js";

const slip = document.querySelector("#slip");

addLine(slip, { code: "KEY", name: "Keychain", qty: 2 });
addLine(slip, { code: "AMI", name: "Amigurumi", qty: 1 });
addLine(slip, { code: "BTY", name: "Baby booties", qty: 1 });

// The customer changed their mind about the booties.
removeLine(slip, "BTY");
