## Hook

A bakery has a rule: nothing leaves the kitchen until someone tastes it. One day the taster is on leave, and the cakes go out anyway. They look perfect. Half of them have salt instead of sugar.

Your editor tastes your code: it underlines type errors in red. But the editor is only on your laptop. If the pipeline that ships the store never runs the type checker, a type error can leave the kitchen.

## Concept

### The build doesn't check types

You met Vite in [Vite: a dev server and a build](/t/web-07-tooling/topic-02-vite). When Vite builds a TypeScript app, it hands each file to a fast tool, esbuild, which **strips** the types and keeps the JavaScript. It never asks whether the types are right.

```callout jargon
**Transpiling** means turning code in one language into another, here TypeScript into JavaScript. **Type checking** means proving the types fit together. Vite transpiles. Only `tsc` type-checks.
```

That's on purpose. Stripping one file at a time is very fast, so the dev server reloads in milliseconds. Checking needs the whole project at once, which is slower. So the two jobs are split, and you must run both:

```json
{
  "scripts": {
    "dev": "vite",
    "typecheck": "tsc --noEmit",
    "build": "tsc --noEmit && vite build",
    "test": "vitest run"
  }
}
```

`tsc --noEmit` checks every file and writes nothing. In `build`, the `&&` means "only build if the check passed". Many project templates already write it this way. Teams that remove it to "speed up the build" are the ones whose CI goes green with a type error in it.

### Syntax: isolatedModules and import type

```ts
import type { CartLine } from "./types.ts";
```

Because esbuild looks at one file at a time, it can't tell whether an imported name is a type, which disappears, or a value, which must stay. `import type` tells it: this is only a type, remove the whole line.

The tsconfig option `isolatedModules` (or the newer, stricter `verbatimModuleSyntax`) makes `tsc` report an error for any code that a one-file-at-a-time tool would get wrong. Turn it on in every project built with Vite or esbuild.

### The CI pipeline

You met CI in [GitHub and pull requests](/t/web-07-tooling/topic-05-github-pull-requests). For a typed frontend, a good pipeline runs these steps on every pull request:

1. `npm ci`: install exactly what the lockfile says.
2. Lint: ESLint, including TypeScript rules such as "no explicit any".
3. Type check: `npm run typecheck`.
4. Test: `npm test`.
5. Build: `npm run build`.
6. Deploy a **preview** of this pull request, with its own link.

```animate
title: A type error meets the pipeline
kind: scene
nodes:
  - { id: pr, label: Pull request, emoji: "📝", row: 1, col: 0 }
  - { id: lint, label: Lint, emoji: "🧹", value: waiting, row: 0, col: 2 }
  - { id: tsc, label: tsc --noEmit, emoji: "🔍", value: waiting, row: 1, col: 2 }
  - { id: test, label: Tests, emoji: "🧪", value: waiting, row: 2, col: 2 }
  - { id: live, label: dhaaga.in, emoji: "🌐", value: safe, row: 1, col: 4 }
steps:
  - say: A pull request passes withGiftWrap("45000"), text where a number of paise belongs.
    focus: [pr]
  - say: Lint runs first, because it's quick. The line has no lint problem, so it passes.
    send: { from: pr, to: lint, label: "npm run lint" }
    set: { lint: pass }
  - say: The type check reads the whole project and finds the error, TS2345, a string is not a number.
    send: { from: pr, to: tsc, label: "npm run typecheck" }
    set: { tsc: FAIL }
    focus: [tsc]
  - say: The pipeline stops at the first failure. The tests and the build never run, and the PR shows a red cross.
    set: { test: skipped }
  - say: Nothing reaches the live store. Without the tsc step, vite build would have shipped it.
    focus: [live]
```

Cheap and fast checks go first, so a mistake fails in seconds, not after a five-minute build.

### npm ci and the lockfile

```callout jargon
The **lockfile**, `package-lock.json`, records the exact version of every package that was installed. `npm ci` installs exactly those versions, and fails if the lockfile and `package.json` disagree.
```

TypeScript itself is a package. If CI installed a newer TypeScript than your laptop, it might find errors you've never seen, or miss ones you have. With `npm ci` and a committed lockfile, CI checks with the same TypeScript, the same `@types/react` and the same everything as you.

### Typing environment variables

Settings that differ per deploy, like the API's address, reach a Vite app through `import.meta.env`. You met them in [Configuration and environment variables](/t/web-07-tooling/topic-09-environment-config). Give them types in `src/vite-env.d.ts`:

```ts
interface ImportMetaEnv {
  readonly VITE_API_URL: string;
  readonly VITE_UPI_PAYEE: string;
}
```

```callout jargon
A **declaration file** ends in `.d.ts`. It holds only types, no code, and describes things that exist elsewhere: a library, a global, or here, the environment.
```

Now `import.meta.env.VITE_API_URL` is a `string`, and a typo like `VITE_API_ULR` is a type error. Two warnings stay true: every `VITE_` value is baked into the JavaScript that customers download, so it's public, never a secret. And a type can't prove the value was really set, so check it once at start-up.

### Deploying, and rolling back

The build writes plain files to `dist/`: HTML, JavaScript and CSS. A static host serves them. Types are long gone by then. They did their job in CI.

- **Preview deploys** give every pull request its own link, so reviewers can click through it.
- **Source maps** let error reports in production point at your `.tsx` lines, not the minified JavaScript.
- **Rollback**: each deploy is a folder of files, so going back means pointing the site at the previous build. That takes seconds. Fix forward afterwards, calmly, with a new pull request.

## Worked example

Kavya's old pipeline was two steps: `npm install`, then `vite build`. Here's how the Tuesday bug got through it, and the new pipeline.

The bad line in the pull request:

```ts
const total = withGiftWrap(form.get("paise"));
```

`form.get` returns `FormDataEntryValue | null`, not a number. The editor showed:

```text
error TS2345: Argument of type 'FormDataEntryValue | null' is not assignable to parameter of type 'number'.
```

`vite build` stripped the types and built it. The code added text and numbers, and Golu cost ₹45,00,030.00.

The new workflow, `.github/workflows/ci.yml`:

```yaml
name: CI
on: pull_request
jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 22
          cache: npm
      - run: npm ci
      - run: npm run lint
      - run: npm run typecheck
      - run: npm test
      - run: npm run build
```

Line by line:

- `npm ci` installs the locked versions, including the same TypeScript as your laptop.
- `npm run typecheck` is the step that was missing. It fails the pull request on the Tuesday line.
- The build runs last, and only if everything before it passed.
- A branch rule on `main` says "CI must be green to merge", so nobody can skip it.

## Try it

Node runs TypeScript the way Vite builds it: it strips the types and never checks them. This snippet passes text where a number belongs on its last line, on purpose. Run it.

```ts run
function withGiftWrap(paise: number): number {
  return paise + 3000;
}

console.log(withGiftWrap(45000));
console.log(withGiftWrap("45000" as unknown as number));
```

It prints:

```text
48000
450003000
```

The cast on the last line hides the error from `tsc` too. Without it, `tsc` would say "Argument of type 'string' is not assignable to parameter of type 'number'".

1. Which of the two answers would a customer see as a wrong price?
2. Which pipeline step would catch the last line if the `as unknown as number` were removed?

## Recap

- Vite and esbuild strip types without checking them. Only `tsc` type-checks, so run `tsc --noEmit` in CI and before the build.
- Use `import type` for types, and turn on `isolatedModules` (or `verbatimModuleSyntax`) for one-file-at-a-time tools.
- A good pipeline runs `npm ci`, lint, type check, tests, build and a preview deploy, cheapest first, and stops at the first failure.
- Type `import.meta.env` in `vite-env.d.ts`. Every `VITE_` value is public, and a type doesn't prove it was set.
- A deploy is a folder of built files: rolling back means serving the previous build, in seconds.

## Common mistakes

**1. Trusting a green build.**

```json
{ "scripts": { "build": "vite build" } }
```

Symptom: CI is green, and a type error ships to customers. Fix: `"build": "tsc --noEmit && vite build"`, and a separate `typecheck` step in CI.

**2. Installing with npm install in CI.**

```yaml
- run: npm install
```

Symptom: CI quietly gets a newer `@types/react` and fails on errors nobody can see locally, or passes on ones they can. Fix: `npm ci`, with the lockfile committed.

**3. Importing a type as if it were a value.**

```ts
import { CartLine } from "./types.ts";
```

Symptom: with `verbatimModuleSyntax`, `tsc` refuses it, and some one-file tools leave an import that fails in the browser. Fix: `import type { CartLine } from "./types.ts";`.

**4. Putting a secret in a VITE_ variable.**

```ts
const key = import.meta.env.VITE_UPI_SECRET_KEY;
```

Symptom: anyone can read the key in the downloaded JavaScript. Typing it doesn't hide it. Fix: secrets live only on a server.

**5. Fixing a bad deploy under pressure instead of rolling back.**

```text
hotfix: try again (3rd attempt)
```

Symptom: customers see the broken price for an hour while fixes are rushed. Fix: roll back to the last good build in seconds, then fix forward with a normal, reviewed pull request.
