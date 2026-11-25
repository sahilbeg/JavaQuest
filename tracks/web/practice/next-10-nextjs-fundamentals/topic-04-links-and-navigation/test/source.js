import { readFileSync } from "node:fs";
import { join } from "node:path";

// Tests run with the ticket's folder (holding src/ and test/) as the working directory.

/** A learner file's text exactly as written, for checks that rendering can't see. */
export function source(file) {
  return readFileSync(join(process.cwd(), "src", file), "utf8");
}

/** The text with its comments taken out, so words in a TODO comment never count as code. */
export function withoutComments(text) {
  return text.replace(/\/\*[\s\S]*?\*\//g, "").replace(/(^|[^:])\/\/[^\n]*/g, "$1");
}

/** Whether the file's first statement is the "use client" directive (comments before it are allowed). */
export function startsWithUseClient(text) {
  const code = text.replace(/^(?:\s|\/\/[^\n]*|\/\*[\s\S]*?\*\/)*/, "");
  return /^["']use client["']/.test(code);
}
