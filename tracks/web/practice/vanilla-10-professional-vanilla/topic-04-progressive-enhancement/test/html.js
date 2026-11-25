import { readFileSync } from "node:fs";
import { join } from "node:path";

// Tests run with the ticket's folder (holding src/ and test/) as the working directory. Read files from there:
// inside helper modules, import.meta.url is not the file's real location.

/** The learner's page, parsed like a browser would: query it with querySelector. */
export function page(file = "index.html") {
  return new DOMParser().parseFromString(source(file), "text/html");
}

/** The page's text exactly as written, for checks the parser would hide (like the charset line). */
export function source(file = "index.html") {
  return readFileSync(join(process.cwd(), "src", file), "utf8");
}
