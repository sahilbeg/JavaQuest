# Writing a web module: the shared brief

Everyone writing the Frontend (JavaScript + React) course follows this, so the 11 modules read as one course. Read it with `docs/CONTENT_SPEC.md` (formats; "Web courses" at the end), `docs/STORY.md` (the Dhaaga world and its binding facts), `docs/CURRICULUM.md` (the web section: every topic id, title, practice kind and prerequisite) and `CLAUDE.md` ("Rules learned while building").

The style reference is a finished Java module, for example `content/module-01-basics/` with `practice/module-01-basics/`. Match its depth, voice, card-sized `###` subsections, animations, quizzes and the size of its exercises.

## Where files go

```
tracks/web/content/<module>/module.yaml
tracks/web/content/<module>/<topic>/meta.yaml, lesson.md, quiz.yaml   (no quiz.yaml for boss and mini-project)
tracks/web/practice/<module>/<topic>/README.md, src/, skeleton/, solution/, test/   (code topics only)
```

- **Ids:** module and topic ids are exactly those in `docs/CURRICULUM.md`.
- **`module.yaml`:** `id`, `title`, `project` (the business stage from `docs/STORY.md`), `summary`. Add `unlockAfter: <previous module>/boss`, except for web-00.
- **`src/`:** byte-for-byte the same as `skeleton/` when written.
- **Quiz topics:** `practice.kind: quiz` and no practice folder.

## meta.yaml

```yaml
id: web-03-javascript-basics/topic-02-numbers-and-paise
title: "Numbers, maths and why prices are in paise"
order: 2                       # topic number; the boss is 99, the mini-project 100
minutes: 8                     # 5 to 10 (incidents and features up to 15)
prerequisites: [web-03-javascript-basics/topic-01-variables]
concepts: [number-type, arithmetic, paise]
practice:
  kind: code
  runner: vitest
  mainClass: main.js           # only when the ticket has a program that prints (Run button starts it with Node)
  preview: true                # only when the ticket builds a page (HTML/CSS/DOM/React)
xp: { quiz: 50, code: 100 }
ticket:
  key: WEB-0302                # WEB-MMTT; boss INC-MM; mini-project FEAT-MM (react-10's is CAPSTONE)
  from: maya
  title: "The calculator says ₹1.5 instead of ₹150"
  body: >
    Two to four sentences in the teammate's voice, naming a real customer, item or day.
hints:                         # exactly 3, gentle to explicit
  - "..."
  - "..."
  - "..."
review: true                   # false for boss and mini-project
```

**Who sends what (`ticket.from`)** (the names shown come from the course's cast):
- `maya` = Kavya Iyer, lead engineer: most tickets.
- `sana` = Sana Shaikh, designer: page, CSS and UI tickets.
- `sam` = Raju Pawar, the counter and customer support: bugs customers hit.
- `theo` = Arjun Mehta, operations: every incident (boss).
- `priya` = Meera Kulkarni, founder: every feature (mini-project).

## lesson.md

Use the same sections as the Java course (`docs/CONTENT_SPEC.md`):
- **Topics:** Hook, Concept, Worked example, Try it, Recap, Common mistakes.
- **Incidents:** What happened, What you need, The tiers, Recap, Common mistakes.
- **Features:** The design, What you build, Recap, Common mistakes.

**Writing rules**
- Explain like the reader is 15. Keep sentences under 25 words. Define every jargon word the first time, with ```` ```callout jargon ````.
- Teammates never teach: they appear only in the ticket.
- Don't name the helper in lesson text. Write a tip plainly in a ```` ```callout tip ````.
- Use short `###` subsections: each becomes one card. Put a `### Syntax: <name>` box for each new piece of syntax, with the short general form in its first code block.
- Under `## Common mistakes`, start each mistake with a bold title paragraph (`**1. Comparing with == instead of ===.**`). Follow it with the wrong code, what the learner sees, and the fix.

**Code blocks**
- ```` ```js run ````: plain JavaScript that Node runs and that must finish. No `document`, `window`, `fetch` or `localStorage`; no timers longer than a few milliseconds. Output must not depend on the clock or the machine. Never print `new Date()` without fixing the date.
- ```` ```js run crash ````: must end in an error, on purpose.
- ```` ```js ````: must parse, and isn't run. Use it for DOM code, browser-only code, and fragments.
- ```` ```jsx ````, ```` ```html ```` and ```` ```css ````: shown, not run. Use ```` ```jsx ```` for every React snippet (JSX never goes in a `js` block).
- Use `js nocompile` only for something that deliberately doesn't parse.

**Animations**
- Every topic has at least one ```` ```animate ```` block.
- A `kind: code` animation in JavaScript adds `language: js`. Its `code` runs with Node, and its `out` lines must equal what it prints. Keep it to plain JavaScript, no DOM.
- Use `kind: scene` for browsers, servers, the DOM tree, events, requests, React re-renders and git.

## quiz.yaml

- 10–12 questions, `drawCount: 6`, `passPercent: 80`. Every concept in `meta.yaml` is covered. Every `reteach` names a real heading in the lesson (`"## Concept"` or `"### Syntax: ..."`).
- Exactly one defensible answer per question. If a different answer would also work, use multiple choice.
- **Fill-blank** needs a `goal`: one sentence, 25 words or fewer, saying what the finished line does. It must not contain any accepted answer (a one-word answer is matched as a whole word, ignoring case) and has no `___`. The prompt is the code line only.
- **Predict-output** code is plain JavaScript that Node runs. The `answer` is exactly what it prints.
- **Spot-the-bug** `bugLine` is 1-based.

## The story facts (binding, from docs/STORY.md)

- **Money:** in code, money is whole paise (integers): ₹1 = 100 paise. In text, show it as `₹1,299.00`. Never store rupees as decimals.
- **Price list:**
  - `CST` coaster set (4): ₹150 = 15000 paise
  - `KEY` keychain: ₹120 = 12000
  - `AMI` amigurumi (small toy): ₹450 = 45000
  - `BAG` tote bag: ₹899 = 89900
  - `BTY` baby booties: ₹350 = 35000
- **Item ids:** `DC-1042` (DC-1042 is the amigurumi elephant modelled on Golu).
- **Order ids:** `order-17`.
- **Fake phone numbers:** `98765 43210`.
- **Where Dhaaga sells:** the FC Road stall, the Aundh shop, the Koregaon Park pop-up (Diwali), WhatsApp, the website dhaaga.in.
- **Payment modes:** `CASH`, `UPI`, `CARD`.
- **Extras:**
  - Gift wrap: ₹30.
  - A name stitched on: ₹50.
  - Delivery inside Pune: ₹60, free above ₹999 (the order total before delivery).
  - The Diwali offer: 10% off orders above ₹1,500, rounded down to whole paise.
- **Hours:** the stall opens Sat–Sun 10:00–20:00; the shop opens Tue–Sun 11:00–21:00.
- **Yarn:** grams per colour, in Marigold, Indigo, Rani pink, Mint and Off-white. A small amigurumi uses about 40 g.
- **Brand colours (Sana's):** Marigold `#F2A81D` and Indigo `#2E3A8C`, with Off-white `#FAF7F0` for backgrounds.

## Practice code

**Files**
- Use `.html`, `.css`, `.js` and `.jsx`. JSX only ever goes in `.jsx` files.
- ES modules everywhere (`import`/`export`). React 19 with the automatic JSX runtime, so you never `import React`.
- **The module under test has no side effects on import.** Put the logic in an exported function or component (for example `src/cart.js` or `src/Cart.jsx`). A page's wiring lives in `src/main.js` / `src/main.jsx`, which imports it, and tests never import `main.*`.
- **A page ticket** (`preview: true`) has `src/index.html` loading `<script type="module" src="main.js"></script>`, or just a `main.jsx` that renders into `#root` (the preview writes the page). CSS is either imported in `main.*` (`import "./style.css";`) or linked from `index.html`.
- **A program ticket** (`mainClass: main.js`) prints with `console.log`. To read typed input, use `readFileSync(0, "utf8")`. Put the logic in an exported function the tests call.

**Skeleton**
- It parses, and importing it never throws.
- Each unfinished part has a numbered `// TODO 1: ...` comment (`<!-- TODO 1: ... -->` in HTML, `/* TODO 1: ... */` in CSS) with a one-line instruction.
- Unfinished functions return a placeholder (`0`, `""`, `[]`, `null`), so the tests fail with assertion messages, not crashes.
- It includes every import the solution uses, because the in-app editor can't add imports.

**Solution:** the shortest clear code that passes every test.

**`README.md`:** the task in plain words, a few bullet points, and which file(s) to change.

## Tests (Vitest in jsdom)

**Rules**
- Files are named `test/<Name>.test.js` or `test/<Name>.test.jsx`.
- Import from `"vitest"` explicitly; globals are off.
- Every `it(...)` name reads as a sentence.
- Every `expect` has a beginner-readable message as its second argument: `expect(total, "two coaster sets cost 30000 paise").toBe(30000)`.
- No network, no real timers you wait for, no randomness. They must pass in under a second on the solution.
- Helper files in `test/` (like the two below) are allowed. Tests reach the learner's files with `../src/...`, because the runner copies `src/` and `test/` side by side.
- **Incidents:** tests split into `test/<Name>Bronze.test.js(x)`, `<Name>Silver.test.js(x)` and `<Name>Gold.test.js(x)`. Bronze is the bug itself; silver and gold are harder edges.

**jsdom limits to design around**
- No layout: sizes and positions are 0.
- No real network.
- `getComputedStyle` doesn't apply media queries.
- So check HTML by parsing it, and check CSS by reading the rules (helper below). Don't measure pixels.

### HTML pages: `test/html.js`

```js
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
```

```js
import { describe, it, expect } from "vitest";
import { page } from "./html.js";

describe("The catalogue page", () => {
  it("has one main heading that names the stall", () => {
    const h1s = page().querySelectorAll("h1");
    expect(h1s.length, "the page should have exactly one <h1>").toBe(1);
    expect(h1s[0].textContent.trim(), "the <h1> should say Dhaaga & Co.").toBe("Dhaaga & Co.");
  });
});
```

### CSS: `test/css.js`

```js
import { readFileSync } from "node:fs";
import { join } from "node:path";

/** The learner's stylesheet as text, read from the ticket folder (the tests' working directory). */
export function css(file = "style.css") {
  return readFileSync(join(process.cwd(), "src", file), "utf8");
}

/** Every rule: { selector, media, decls } with decls a Map of property -> value. Comments are ignored. */
export function rules(text = css()) {
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

/** The value a selector sets for a property (the last rule wins), outside media queries unless media is given. */
export function value(selector, property, { media = null, text = css() } = {}) {
  let found;
  for (const rule of rules(text)) {
    if (rule.selector === selector && rule.media === media && rule.decls.has(property)) found = rule.decls.get(property);
  }
  return found;
}
```

Values come back lower-cased and trimmed (`"#f2a81d"`, `"1rem"`). Compare exactly, or check them with a regular expression when several spellings are fine (`/^(0|0px)$/`). Say in the README which spelling the check wants when it matters.

### JavaScript logic

```js
import { describe, it, expect } from "vitest";
import { orderTotal } from "../src/cart.js";

describe("orderTotal", () => {
  it("adds paise times quantity for every line", () => {
    expect(orderTotal([{ code: "CST", paise: 15000, qty: 2 }]), "two coaster sets cost 30000 paise").toBe(30000);
  });
});
```

- To check printing, spy on `console.log`: `const log = vi.spyOn(console, "log").mockImplementation(() => {});`, then check `log.mock.calls`, and call `log.mockRestore()` in `afterEach`.
- **Time:** use `vi.useFakeTimers()` and `vi.advanceTimersByTime(ms)`, with `vi.useRealTimers()` in `afterEach`. Pass dates in as arguments rather than reading the clock.
- **fetch:** `vi.stubGlobal("fetch", vi.fn(async (url) => ({ ok: true, status: 200, json: async () => data })))`, with `vi.unstubAllGlobals()` in `afterEach`.
- **localStorage:** it exists in jsdom. Call `localStorage.clear()` in `beforeEach`.

### The DOM

```js
import { describe, it, expect, beforeEach } from "vitest";
import { renderCatalogue } from "../src/catalogue.js";

beforeEach(() => {
  document.body.innerHTML = `<ul id="list"></ul>`;
});

describe("renderCatalogue", () => {
  it("draws one list item per product", () => {
    renderCatalogue(document.querySelector("#list"), [{ id: "DC-1042", name: "Golu the elephant", paise: 45000 }]);
    expect(document.querySelectorAll("#list li").length, "one product should make one <li>").toBe(1);
  });
});
```

To trigger events, use `element.click()`, or `element.dispatchEvent(new Event("input", { bubbles: true }))` after setting `.value`.

### React

```jsx
import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ProductCard } from "../src/ProductCard.jsx";

afterEach(cleanup);

describe("ProductCard", () => {
  it("shows the price in rupees", () => {
    render(<ProductCard name="Coaster set" paise={15000} />);
    expect(screen.queryByText("₹150.00"), "the card should show ₹150.00 for 15000 paise").not.toBeNull();
  });

  it("adds the item when you press Add to cart", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn();
    render(<ProductCard name="Coaster set" paise={15000} onAdd={onAdd} />);
    await user.click(screen.getByRole("button", { name: "Add to cart" }));
    expect(onAdd.mock.calls.length, "pressing Add to cart should call onAdd once").toBe(1);
  });
});
```

- `import { vi } from "vitest"` when you use `vi`.
- `afterEach(cleanup)` is required, because globals are off.
- Prefer `queryBy...` plus `expect(..., message)` over `getBy...`, so a miss shows a friendly message (a `getBy` miss throws RTL's own error).
- There are no jest-dom matchers. Compare `textContent`, attributes, or `.disabled` with `toBe`.
- `react`, `react-dom`, `@testing-library/react`, `@testing-library/user-event` and `@testing-library/dom` are installed. Nothing else is, so there's no React Router. Routing in react-09 is taught by building a small router with state and `window.location.hash`, with React Router named as what teams use.

## Checking your own work

You can't run commands, so check by reading:
- Every solution passes every test. Trace each `expect` against the solution.
- Every skeleton parses, imports cleanly, and fails at least one test of every test file. For incidents, the skeleton fails bronze.
- Every `js run` block and predict-output question prints exactly what you claim. Trace it line by line.
- Every quiz `reteach` heading exists in the lesson.
- `src/` equals `skeleton/`.
- Ids, keys, prerequisites and `order` match `docs/CURRICULUM.md`.
