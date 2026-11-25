## Hook

A building inspector doesn't just check that the walls stand up today. She looks for the hidden things: a wire with no cover, a beam held up by a wedge of paper. Each one is fine until the day it isn't.

Reviewing TypeScript is like that. The compiler already checked that the types fit. Your job is to find the places where someone told the compiler to look away, and the types that tell a story the code doesn't keep.

## Concept

### What the compiler already did

You met pull requests in [GitHub and pull requests](/t/web-07-tooling/topic-05-github-pull-requests). Reviewing one well is a craft: open the preview, follow a checklist, and write kind, specific comments with a label on each. All of that applies to TypeScript too.

TypeScript changes where you spend your time. When CI runs `tsc`, every wrong property name and every missing argument is already caught. So don't spend review time re-checking what the compiler checks. Spend it on what the compiler **can't** see:

1. **Does it work?** Behaviour, edge cases and the preview, as always.
2. **Escape hatches.** Every `any`, `as`, `!` and `@ts-ignore` in the diff.
3. **Honest types.** Do the types say what really happens, including errors and missing values?
4. **Unions.** If a union grew, does every place that branches on it handle the new member?
5. **Simple types.** Can a teammate read the types in a minute?
6. Then tests, accessibility, performance and naming, as before.

### Escape hatches: where the checks stop

```callout jargon
An **escape hatch** is a way to switch the type checker off for one spot: `any`, an `as` cast, a `!` assertion, or a `@ts-ignore` comment.
```

Search the diff for them first. They are where bugs hide, because no tool looks there:

| In the diff | Ask the author |
|---|---|
| `as Item[]` on fetched data | Where is this checked? Could it be a parser? |
| `!` after `find(...)` | What if nothing is found? |
| `: any` | What is the real shape? |
| `// @ts-ignore` | What's the error, and why can't it be fixed? Could it be `@ts-expect-error` with a ticket? |

An escape hatch isn't always wrong. A test helper or a badly typed old library can need one. But each one needs a reason in a comment, so the next reader knows it was a choice.

### Honest types

A type is a promise to every caller. Review whether the promise is true:

- **Missing values.** `function findItem(id: string): Item` can't be right if the item might not exist. It should say `Item | undefined`.
- **Errors.** A function that can fail should say so, with a `Result` type, as in [Typed APIs end to end](/t/tsreact-12-professional-typescript/topic-03-typed-apis-end-to-end), or with a clear thrown error the caller expects.
- **Too wide.** `status: string` accepts `"shiped"`. A union of the real values catches typos.
- **Too loose for money.** `paise: number` is fine, but a function that returns rupees as a decimal should be questioned: money is whole paise.

### Unions that grew

When a pull request adds a member to a union, like a new payment mode, the most important review question is: which code branches on this union, and does all of it handle the new member?

```animate
title: A default branch swallows the new payment mode
kind: code
language: ts
code: |
  type Payment = "CASH" | "UPI" | "CARD";
  function label(mode: Payment): string {
    switch (mode) {
      case "CASH": return "Cash at the counter";
      case "UPI": return "UPI";
      default: return "Card";
    }
  }
  console.log(label("CARD"));
  console.log(label("WALLET" as Payment));
steps:
  - line: 1
    say: Three payment modes. The pull request adds WALLET to this union.
  - line: 6
    say: The switch handles CASH and UPI, and lets default catch everything else.
  - line: 9
    out: "Card"
    say: For CARD, default gives the right answer, so the code looks fine.
  - line: 10
    out: "Card"
    say: For WALLET, default also says Card. The receipt is wrong, and the compiler never said a word.
```

The fix is an **exhaustive** switch: handle every member by name, and make the default prove nothing is left.

### Syntax: an exhaustive check

```ts
function receiptLabel(mode: Payment): string {
  switch (mode) {
    case "CASH": return "Cash at the counter";
    case "UPI": return "UPI";
    case "CARD": return "Card";
    default: {
      const unhandled: never = mode;
      throw new Error("Unknown payment mode: " + unhandled);
    }
  }
}
```

You met `never` in [Discriminated unions](/t/ts-09-typescript-in-depth/topic-04-discriminated-unions). When every case is handled, `mode` has the type `never` in the default, and the line compiles. When someone adds `"WALLET"`, `mode` is `"WALLET"` there, and the assignment fails:

```text
error TS2322: Type '"WALLET"' is not assignable to type 'never'.
```

Now the compiler finds every switch to update. In review, ask for this whenever code branches on a union that might grow.

### Simple types beat clever types

TypeScript's type system can do almost anything: conditional types, mapped types, template literal types, recursion. That doesn't mean a feature needs it.

```ts
type DeepPartialExceptIds<T> = {
  [K in keyof T]: K extends `${string}Id` ? T[K] : T[K] extends object ? DeepPartialExceptIds<T[K]> : T[K] | undefined;
};
```

If a type takes ten minutes to read, ask whether a plain interface would do. Clever types are hard to change, and their error messages are hard to read. A good rule: the types in app code should read like a description of the data. Save the clever ones for shared libraries, with tests and comments.

### Writing TypeScript review comments

```callout jargon
**Conventional Comments** is a simple habit: start each review comment with a label. `issue` is a problem, `suggestion` an idea, `question` a question, `nitpick` a tiny matter of taste, and `praise` something done well. Add `(blocking)` when the pull request can't merge until it's fixed.
```

Some examples for TypeScript:

```text
issue (blocking): this casts the API answer with "as Item[]". When the server sends a price as text,
the cart adds text. Could we use parseItems from api.ts here?

issue (blocking): the switch in receiptLabel has a default branch, so WALLET prints "Card".
Could we list every mode and add the never check, so the next new mode is a compile error?

suggestion: findCustomer can return undefined, but its type says Customer. Could the type say
Customer | undefined, so callers handle it?

question: the ! on line 40 says the ref is always set. Is that true on the first render?

praise: the Result type here makes every caller handle a failed payment. Lovely.
```

Block for behaviour and for types that lie. Don't block for taste, such as `type` versus `interface` for a plain object. Pick the team's rule once, and let a lint rule enforce it.

## Worked example

Here's the WALLET pull request. CI is green.

```diff
-type Payment = "CASH" | "UPI" | "CARD";
+type Payment = "CASH" | "UPI" | "CARD" | "WALLET";

 function receiptLabel(mode: Payment): string {
   switch (mode) {
     case "CASH": return "Cash at the counter";
     case "UPI": return "UPI";
     default: return "Card";
   }
 }

+async function walletBalance(phone: string): Promise<number> {
+  const response = await fetch(`/api/wallet/${phone}`);
+  const data = (await response.json()) as any;
+  return data.balance_paise!;
+}
```

The review, in checklist order:

```text
issue (blocking), receiptLabel: the default branch prints "Card" for WALLET. Could we handle all four
modes by name and add const unhandled: never = mode in the default?

issue (blocking), walletBalance: the answer is cast with "as any", and "!" promises balance_paise exists.
A 404 or a text balance becomes NaN on the checkout. Could it return Result<number>, check
response.ok, and check that balance_paise is a whole number?

question, walletBalance: the phone number goes into the URL as typed. Should spaces be removed first?

praise: adding WALLET to the union, instead of passing a string around, is exactly right.
```

The verdict is **Request changes**. Both blocking comments are about types that hide real behaviour, and neither is about style.

## Try it

This is a tiny review robot. It looks at the added lines of a diff for escape hatches. Run it.

```ts run
const diff: string[] = [
  "+const items = (await res.json()) as Item[];",
  "+function total(lines: any) {",
  "+  return lines.reduce((sum: number, line: CartLine) => sum + line.paise * line.qty, 0);",
  "+const golu = items.find((item) => item.id === id)!;",
];

const checks: [string, RegExp][] = [
  ["as cast", /\bas\s+[A-Z]/],
  ["any", /\bany\b/],
  ["non-null !", /\)!/],
];

diff.forEach((line, index) => {
  for (const [name, pattern] of checks) {
    if (pattern.test(line)) console.log("line " + (index + 1) + ": " + name);
  }
});
```

It prints:

```text
line 1: as cast
line 2: any
line 4: non-null !
```

1. Add a line `"+// @ts-ignore"` and a check that finds it.
2. Add the line `"+const x = value as const;"`. Does the robot flag it? Should it?

## Recap

- With `tsc` in CI, don't re-check what the compiler checks. Review behaviour first, then what the compiler can't see.
- Find every escape hatch in the diff (`any`, `as`, `!`, `@ts-ignore`) and ask for a reason or a real check.
- Check that types are honest: missing values, errors, and unions instead of wide strings.
- When a union grows, find every branch on it, and ask for exhaustive switches with a `never` check.
- Prefer plain, readable types in app code. Label comments, block for behaviour and lying types, never for taste.

## Common mistakes

**1. Approving because "the types pass".**

```text
LGTM, tsc is green.
```

Symptom: the PR merges with `as any` on the wallet answer, and the checkout shows NaN. Fix: the compiler can't check what was switched off. Search the diff for escape hatches.

**2. Missing the default branch.**

```ts
function receiptLabel(mode: Payment): string {
  switch (mode) {
    case "CASH": return "Cash at the counter";
    default: return "Card";
  }
}
```

Symptom: a new member falls into `default` and prints the wrong thing, with no error. Fix: name every case, and add `const unhandled: never = mode` in the default.

**3. Blocking for taste.**

```text
issue (blocking): please use interface instead of type here.
```

Symptom: the author spends a day on a change that helps no customer, and starts dreading your reviews. Fix: make it a team rule with a lint check, or leave a non-blocking nitpick.

**4. Accepting a type that hides missing values.**

```ts
function findCustomer(phone: string): Customer {
  return CUSTOMERS.find((c) => c.phone === phone) as Customer;
}
```

Symptom: callers never check for a missing customer, and the order page crashes for new ones. Fix: ask for `Customer | undefined`, without the cast.

**5. Asking for clever types in app code.**

```text
suggestion: could this be a recursive conditional type so it works for any depth?
```

Symptom: a simple form type becomes a puzzle nobody can change. Fix: suggest the simplest type that describes the real data.
