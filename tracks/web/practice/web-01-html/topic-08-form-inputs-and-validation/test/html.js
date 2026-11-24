import { readFileSync } from "node:fs";
import { join } from "node:path";

/** The learner's page, parsed like a browser would: query it with querySelector. */
export function page(file = "index.html") {
  return new DOMParser().parseFromString(source(file), "text/html");
}

/** The page's text exactly as written, for checks the parser would hide (like the charset line). */
export function source(file = "index.html") {
  return readFileSync(join(process.cwd(), "src", file), "utf8");
}
