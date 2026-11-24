## Hook

Two proofreaders check a printed menu. The first only hunts for mistakes that matter: "₹150" printed as "₹15", a dish listed twice. The second only fixes the look: the same font everywhere, prices lined up on the right.

If the people who wrote the menu argue about fonts, nobody spots the wrong price. Give the look to a machine, and the humans can hunt for real mistakes.

JavaScript teams use two such proofreaders. **ESLint** hunts for likely bugs. **Prettier** fixes the look.

## Concept

```callout jargon
A **linter** is a tool that reads your code without running it and points at likely bugs. **ESLint** is the linter almost every JavaScript team uses. A **formatter** rewrites code so it looks the same everywhere: spaces, quotes, line breaks. **Prettier** is the most common one.
```

They do different jobs:

| | ESLint | Prettier |
|---|---|---|
| Looks for | likely bugs and risky habits | messy layout |
| Example | a variable you never use, `==` instead of `===` | tabs vs spaces, `'` vs `"`, long lines |
| Changes the meaning? | can point at code that behaves wrongly | never: the code does exactly the same thing |

Both are devDependencies, because only developers use them:

```text
npm install --save-dev eslint @eslint/js globals prettier eslint-config-prettier
```

### Why == is worth a red line

`==` converts values before it compares them. That surprises people:

```js run
console.log("" == 0);
console.log("" === 0);
console.log("0" == false);
```

An empty quantity box gives `""`, and `""` equals `0` under `==`. `===` never converts, so it only says `true` when the type and the value match. ESLint's `eqeqeq` rule flags every `==`, so no one has to spot it in a review.

### Syntax: eslint.config.js

```js
export default [
  // each object adds settings or rules
];
```

ESLint reads its settings from `eslint.config.js` in the top folder. The file exports a list. Later entries can change what earlier ones set. Here is the one for dhaaga.in:

```js
import js from "@eslint/js";
import globals from "globals";

export default [
  { ignores: ["dist/"] },
  js.configs.recommended,
  {
    languageOptions: { globals: globals.browser },
    rules: {
      eqeqeq: "error",
      "no-console": "warn",
    },
  },
];
```

- `ignores` skips `dist/`, the build's output. `node_modules` is skipped already.
- `js.configs.recommended` turns on ESLint's recommended rules, like `no-unused-vars` and `no-undef`.
- `globals.browser` tells ESLint that names like `document` and `window` exist, so it does not call them undefined.
- `rules` adds your team's own choices.

```callout jargon
A **rule** is one check, with a name like `eqeqeq`. Its **severity** is `"off"`, `"warn"` or `"error"`. An error makes the lint command fail. A warning is shown, but the command still passes.
```

### Syntax: running ESLint

```text
npx eslint .
```

The `.` means "this folder and everything inside". Here is a cart function with three problems:

```js
export function addToCart(cart, code, qty) {
  const discount = 0;
  if (qty == 0) {
    return cart;
  }
  cart.push({ code, qty: qtty });
  return cart;
}
```

And ESLint's report:

```text
/Users/you/dhaaga-web/src/cart.js
  2:9   error  'discount' is assigned a value but never used  no-unused-vars
  3:11  error  Expected '===' and instead saw '=='            eqeqeq
  6:26  error  'qtty' is not defined                          no-undef

✖ 3 problems (3 errors, 0 warnings)
```

Each line is `line:column`, the severity, the message, and the rule's name. Line 6 is a real bug: a typo that would crash the cart with a `ReferenceError` the first time a customer adds an item.

`npx eslint . --fix` fixes the problems a rule knows how to fix safely. The rest you fix by hand.

```callout tip
If you truly need to break a rule on one line, write `// eslint-disable-next-line no-console` above it, and say why. Turning a rule off for one line is better than turning it off for everyone.
```

### Syntax: running Prettier

```text
npx prettier --write .
npx prettier --check .
```

`--write` rewrites every file it can format. `--check` changes nothing; it only lists the files that are not formatted.

Before:

```js
export function lineTotal( line ){return line.paise*line.qty}
```

After `npx prettier --write src/cart.js`:

```js
export function lineTotal(line) {
  return line.paise * line.qty;
}
```

Same behaviour, easier to read. When something is not formatted, `--check` prints:

```text
Checking formatting...
[warn] src/cart.js
[warn] Code style issues found in the above file. Run Prettier with --write to fix.
```

### Prettier's settings

Prettier has very few options, on purpose. Fewer choices means fewer arguments. You set them in `.prettierrc`, a JSON file in the top folder:

```json
{
  "printWidth": 100,
  "singleQuote": false
}
```

Prettier skips `node_modules`, and the files your `.gitignore` lists. Add a `.prettierignore` file only for anything else.

### Using them together

Some of ESLint's older rules also care about layout. If both tools have an opinion, they fight: one adds a space, the other removes it.

`eslint-config-prettier` switches off every ESLint rule that Prettier already handles. Put it last in the list, so it wins:

```js
import js from "@eslint/js";
import globals from "globals";
import prettier from "eslint-config-prettier";

export default [
  { ignores: ["dist/"] },
  js.configs.recommended,
  { languageOptions: { globals: globals.browser }, rules: { eqeqeq: "error" } },
  prettier,
];
```

Now each tool has one job. ESLint for bugs, Prettier for looks.

```animate
title: A change on its way to review
kind: scene
nodes:
  - { id: you, label: You save cart.js, emoji: "📝", value: messy }
  - { id: prettier, label: Prettier, emoji: "🧹" }
  - { id: eslint, label: ESLint, emoji: "🔍" }
  - { id: review, label: Kavya's review, emoji: "👀" }
steps:
  - say: You save cart.js. The spaces are messy, and there is a == on line 3.
    focus: [you]
  - say: Your editor runs Prettier on save. The layout is fixed, and the code does exactly the same thing.
    send: { from: you, to: prettier, label: format on save }
    set: { you: tidy, prettier: formatted }
  - say: You run npm run lint. ESLint reads the code and finds the == on line 3.
    send: { from: you, to: eslint, label: npm run lint }
    set: { eslint: "1 error: eqeqeq" }
  - say: You change it to ===. Now the lint passes.
    send: { from: eslint, to: you, label: "3:11 eqeqeq", back: true }
    set: { eslint: 0 problems }
  - say: Kavya opens the review. Nothing about spaces or quotes. Every comment is about how the cart works.
    send: { from: you, to: review, label: pull request }
    set: { review: comments about logic }
```

### Scripts and your editor

Put both tools in `scripts`, so everyone runs them the same way:

```json
"scripts": {
  "lint": "eslint .",
  "format": "prettier --write .",
  "format:check": "prettier --check ."
}
```

In VS Code, install the ESLint and Prettier extensions. ESLint then draws red squiggles while you type. Turn on "Format On Save" in the settings, and Prettier tidies every file when you save it.

A robot can also run `npm run lint` and `npm run format:check` on every pull request. You will meet it in ticket 5.

## Worked example

Kavya's ticket: the tools should argue about style, so humans can talk about bugs. Here is the setup.

**Step 1: install.**

```text
npm install --save-dev eslint @eslint/js globals prettier eslint-config-prettier
```

**Step 2: write `eslint.config.js`.** You use the version with `prettier` last, from "Using them together".

**Step 3: add the scripts** `lint`, `format` and `format:check`.

**Step 4: format everything once.**

```text
npm run format
git add .
git commit -m "Format all files with Prettier"
```

One big formatting commit, with no other changes in it. Reviewers can skip it, and later changes stay small.

**Step 5: lint, and fix the real bugs.**

```text
npm run lint
```

ESLint finds the `qtty` typo and the `==` from earlier. You fix both by hand, then run `npm run lint` again:

```text
> dhaaga-web@1.0.0 lint
> eslint .

```

No problems listed means the lint passed. npm prints the script's name and command first, then ESLint prints nothing.

## Try it

In the Vite project from the last ticket:

1. Install the five packages from step 1 and write the `eslint.config.js` above.
2. In `src/main.js`, add `const unused = 5;` and `if (1 == "1") {}`. Run `npx eslint .` and read the report.
3. Mess up the spacing of a function on purpose. Run `npx prettier --check .`, then `npx prettier --write .`, and look at the file again.
4. Change `eqeqeq` to `"warn"` and run ESLint again. Does the command still fail?

## Recap

- ESLint is a linter: it reads code without running it and points at likely bugs, like unused variables, undefined names and `==`.
- Prettier is a formatter: it fixes spaces, quotes and line breaks, and never changes what the code does.
- ESLint's rules live in `eslint.config.js`. Each rule is `"off"`, `"warn"` or `"error"`, and errors fail the command.
- `eslint-config-prettier`, placed last, stops the two tools fighting over layout.
- Put `lint` and `format` in `scripts`, and turn on format on save, so style never reaches a review.

## Common mistakes

**1. Mixing up the two tools.**

You run `npx prettier --write .` and expect it to catch the `qtty` typo.

What you see: the file is beautifully formatted, and the typo is still there. The cart crashes with `ReferenceError: qtty is not defined`.

Fix: Prettier only changes the look. Run `npm run lint` too: ESLint is the one that finds bugs.

**2. Forgetting the browser globals.**

```js
import js from "@eslint/js";

export default [js.configs.recommended];
```

What you see: ESLint reports `'document' is not defined  no-undef` on every page script.

Fix: add `{ languageOptions: { globals: globals.browser } }`, with `import globals from "globals";` at the top.

**3. Linting the build output.**

What you see: hundreds of errors in `dist/assets/index-C3r9kQe1.js`, a file you never wrote.

Fix: add `{ ignores: ["dist/"] }` as the first entry in `eslint.config.js`.

**4. Formatting and changing code in one commit.**

You fix the delivery fee and run Prettier on every file in the same commit.

What you see: the pull request shows 40 changed files. The real fix, one line, is lost among them.

Fix: format everything once, in its own commit, before you start. After that, format on save keeps every change small.
