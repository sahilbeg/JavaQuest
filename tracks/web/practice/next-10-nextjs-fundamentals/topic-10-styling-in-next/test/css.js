import { readFileSync } from "node:fs";
import { join } from "node:path";

// Tests run with the ticket's folder (holding src/ and test/) as the working directory.

/** A learner file's text exactly as written, read from src/. */
export function source(file) {
  return readFileSync(join(process.cwd(), "src", file), "utf8");
}

/** The text with its comments taken out, so words in a TODO comment never count as code. */
export function withoutComments(text) {
  return text.replace(/\/\*[\s\S]*?\*\//g, "").replace(/(^|[^:])\/\/[^\n]*/g, "$1");
}

/** Every rule: { selector, media, decls } with decls a Map of property -> value. Comments are ignored. */
export function rules(text) {
  const out = [];
  const walk = (body, media) => {
    let i = 0;
    while (i < body.length) {
      const open = body.indexOf("{", i);
      if (open < 0) break;
      const head = body.slice(i, open).trim();
      let depth = 1;
      let j = open + 1;
      while (j < body.length && depth > 0) {
        if (body[j] === "{") depth++;
        else if (body[j] === "}") depth--;
        j++;
      }
      const inner = body.slice(open + 1, j - 1);
      if (head.startsWith("@media")) {
        walk(inner, head.slice("@media".length).trim());
      } else if (!head.startsWith("@")) {
        const decls = new Map();
        for (const part of inner.split(";")) {
          const colon = part.indexOf(":");
          if (colon > 0) decls.set(part.slice(0, colon).trim().toLowerCase(), part.slice(colon + 1).trim().toLowerCase());
        }
        for (const selector of head.split(",")) out.push({ selector: selector.trim().replace(/\s+/g, " "), media, decls });
      }
      i = j;
    }
  };
  walk(text.replace(/\/\*[\s\S]*?\*\//g, ""), null);
  return out;
}

/** The value a selector sets for a property in the given CSS file (the last rule wins), outside media queries. */
export function value(file, selector, property) {
  let found;
  for (const rule of rules(source(file))) {
    if (rule.selector === selector && rule.media === null && rule.decls.has(property)) found = rule.decls.get(property);
  }
  return found;
}
