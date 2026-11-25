## Hook

Before a big order of 200 coasters leaves the workshop, a second pair of hands checks a few from each box: the colour, the size, the stitching at the edge. The person who made them is skilled. But after six hours of crochet, anyone stops seeing their own loose loop.

A code review is that second pair of eyes. In a Next.js app it matters even more, because one file can quietly decide whether code runs on your server or on every customer's phone. Those choices are easy to miss when you write them, and easy to spot when you know where to look.

## Concept

### What a review is for

```callout jargon
A **pull request** (PR) asks to merge a set of changes into the main branch. A **code review** is a teammate reading those changes before they merge, to find problems and share knowledge.
```

You opened pull requests in [GitHub and pull requests](/t/web-07-tooling/topic-05-github-pull-requests). Reviewing them is the other half. A review isn't a test of the author. It's a shared check that the change is correct, safe, fast and clear, so the whole team can own the code later.

Read in this order: first the PR description (what and why), then the tests (what it promises), then the code. Run the preview deploy if there is one, and click through the change on your phone.

### A Next.js review checklist

For every file in the PR, these questions catch most Next.js bugs:

1. **Where does it run?** Server, browser, or both?
2. **What crosses to the browser?** Props, `NEXT_PUBLIC_` values, imported modules.
3. **How does it get data?** In parallel, once, with `params` awaited?
4. **Who can call it?** Every server action and route handler is a public door.
5. **What is cached, and for whom?** Could one customer ever see another's data?
6. **What happens when it fails?** Not found, slow, offline, bad input.
7. **Is it tested and accessible?** Real checks, labels, headings, focus.

Linters and type checkers catch spelling-level problems. Spend your attention on what they can't see: these seven questions.

### Reviewing the client boundary

```jsx
"use client";                                // at the top of app/orders/[id]/page.jsx

import { useState } from "react";
import { formatOrder } from "../../../lib/orders.js";
```

Look at where `"use client"` lands. Here it marks a whole page, and everything it imports, as browser code. That includes `lib/orders.js`, which may hold database code. Ask for the interactive part to move into a small client component.

Also read the props a server component passes to a client component. Every prop is sent to the phone. A whole order record with the customer's phone number, passed "just in case", is both slow and a privacy leak.

### Reviewing data fetching

```animate
title: One call per line, or one for all
kind: code
language: js
code: |
  let calls = 0;
  const getItem = (id) => { calls += 1; return id; };
  const getItems = (ids) => { calls += 1; return ids; };
  const cart = ["DC-1042", "DC-1101", "DC-1205"];
  cart.forEach(getItem);
  console.log("one per line: " + calls);
  calls = 0;
  getItems(cart);
  console.log("one for all: " + calls);
steps:
  - line: 1
    vars: { calls: 0 }
    say: calls counts how many times we ask the data layer for something.
  - line: 4
    vars: { cart: '["DC-1042", "DC-1101", "DC-1205"]' }
    say: A cart with three lines.
  - line: 5
    vars: { calls: 3 }
    say: Asking for each item inside a loop makes one call per line. A 30-line Diwali cart makes 30 calls.
  - line: 6
    out: "one per line: 3"
    say: Reviewers call this the N+1 problem. The cost grows with the size of the cart.
  - line: 8
    vars: { calls: 1 }
    say: One call that takes all the ids at once stays a single call, however big the cart gets.
  - line: 9
    out: "one for all: 1"
    say: When you see a data call inside a loop, ask whether one call could fetch them all.
```

In data code, look for four patterns:

- **Waterfalls**: independent `await`s one after another. Suggest `Promise.all`.
- **Calls inside loops**: one request per item, the N+1 problem above.
- **`params` without `await`**: in Next.js 16, `params.id` is `undefined`.
- **A server component fetching its own API**: `fetch("/api/items")` from a page adds a needless trip. Call the data layer directly.

### Reviewing actions and routes

For every server action and route handler in the diff, ask the questions from [Security: secrets, headers and input](/t/next-12-production-nextjs/topic-04-security):

- **Authentication**: is the session checked inside this function, not only on the page?
- **Authorisation**: may this customer touch this order?
- **Validation**: is every field checked for type and range?
- **Trust**: do prices, stock and totals come from the server, never from the form?
- **Output**: does it return only what the page needs?

### Reviewing caching and rendering

Caching bugs don't show up on the author's laptop. They show up when two customers use the site at once.

- A new `cookies()` or `headers()` call turns a static page dynamic. Is that on purpose, or did a product page just get slower?
- Is anything personal, like a cart or an order, being cached in a way that could be shown to someone else?
- After an action changes data, does it call `revalidatePath` or `revalidateTag`, as in [Caching and revalidation](/t/next-11-data-and-server/topic-02-caching-and-revalidation)? Otherwise the catalogue keeps showing old stock.

### Writing comments people are glad to get

A good comment is **specific**, says **why**, and **suggests** a fix. Label how much it matters, so the author knows what must change:

| Label | Means |
|---|---|
| **blocking** | must change before merge: a bug, a leak, a security hole |
| **suggestion** | would make it better; the author decides |
| **question** | you don't understand something yet |
| **nit** | tiny and optional, like a name or a typo |

```text
blocking: placeOrder multiplies by formData.get("paise"), so anyone can edit the hidden field
and pay 1 paisa. Could we look the price up with getItem(id) instead? Happy to pair on it.
```

Comment on the code, not the person: "this action trusts the form", not "you forgot security". Say what's good too. "Nice, this Promise.all cut the page's wait by two thirds" teaches as much as a correction.

### Receiving a review

When you're the author, keep the PR small and its description clear: what changed, why, and how to test it. Reply to every comment, even with "done". If you disagree, explain your reasons calmly, or talk it through. The goal is good code, not winning.

```animate
title: A pull request through review
kind: scene
nodes:
  - { id: author, label: Author, emoji: "🧑‍💻", row: 1, col: 0 }
  - { id: pr, label: "PR #87: gift wrap", emoji: "📝", value: open, row: 1, col: 2 }
  - { id: reviewer, label: Reviewer, emoji: "🔍", row: 1, col: 4 }
  - { id: main, label: main, emoji: "🌳", row: 0, col: 2 }
steps:
  - say: The author opens a small PR with a clear description and a preview link.
    send: { from: author, to: pr, label: "open" }
  - say: The reviewer reads the description, the tests, then the code, and tries the preview on a phone.
    send: { from: reviewer, to: pr, label: "read" }
  - say: One blocking comment, the gift wrap price comes from the form, plus a suggestion and a word of praise.
    send: { from: reviewer, to: pr, label: "3 comments" }
    set: { pr: "changes requested" }
  - say: The author fixes the price, replies to each comment, and pushes.
    send: { from: author, to: pr, label: "fix + replies" }
    set: { pr: "updated" }
  - say: The reviewer approves, the checks are green, and the change merges.
    send: { from: pr, to: main, label: "merge" }
    set: { pr: merged }
    focus: [main]
```

## Worked example

Here's a real-sized PR: "Add gift wrap (₹30) to Buy now". The diff has two files.

```jsx
"use client";

export default function BuyBox({ item, customer }) {
  return (
    <form action={placeOrder}>
      <input type="hidden" name="id" value={item.id} />
      <input type="hidden" name="giftWrapPaise" value="3000" />
      <label>
        <input type="checkbox" name="giftWrap" /> Gift wrap (₹30)
      </label>
      <p>Delivering to {customer.address}</p>
      <button type="submit">Buy now</button>
    </form>
  );
}
```

```js
"use server";

export async function placeOrder(formData) {
  const item = await getItem(String(formData.get("id")));
  const wrap = formData.get("giftWrap") ? Number(formData.get("giftWrapPaise")) : 0;
  return saveOrder({ itemId: item.id, totalPaise: item.paise + wrap });
}
```

The review comments:

- **blocking**: the gift wrap price comes from a hidden field, so it can be set to 0. Please use a server constant, `GIFT_WRAP_PAISE = 3000`.
- **blocking**: `item` can be `null` for an unknown id, and then `item.id` throws. Return a clear error instead.
- **suggestion**: `BuyBox` gets the whole `customer`, which sends their address and phone to the browser. Pass only what it shows.
- **question**: should the action check that someone is logged in? `saveOrder` doesn't seem to know who the customer is.
- And some praise: wrapping the checkbox in its label gives it a name without needing an `id`. Nice.

Each comment says what, why and what to do. Two must change; the author decides on the rest.

## Try it

This tiny "review bot" scans a diff for three Next.js red flags. Real teams add checks like this to CI, so humans can focus on the rest. Run it.

```js run
const rules = [
  { find: "params.id", say: "params is a Promise in Next.js 16: await it first" },
  { find: 'formData.get("paise")', say: "never take a price from the form" },
  { find: "NEXT_PUBLIC_UPI", say: "NEXT_PUBLIC_ values are sent to every browser" },
];

const diff = [
  "const item = await getItem(params.id);",
  "const qty = Number(formData.get(\"qty\"));",
  "const total = Number(formData.get(\"paise\")) * qty;",
  "const key = process.env.NEXT_PUBLIC_UPI_KEY;",
];

diff.forEach((line, index) => {
  for (const rule of rules) {
    if (line.includes(rule.find)) console.log("line " + (index + 1) + ": " + rule.say);
  }
});
```

It prints:

```text
line 1: params is a Promise in Next.js 16: await it first
line 3: never take a price from the form
line 4: NEXT_PUBLIC_ values are sent to every browser
```

1. Add a rule that flags `"use client"` in a file whose name ends in `page.jsx`. What extra information would the bot need?
2. Which of the seven checklist questions could never be checked by a bot like this? Why?

## Recap

- A review is a shared check that a change is correct, safe, fast and clear. Read the description, then the tests, then the code, and try the preview.
- For Next.js code, ask where each file runs, what crosses to the browser, how data is fetched, who can call it, what is cached, how it fails, and whether it's tested and accessible.
- Flag `"use client"` set too high, fat props, waterfalls, calls inside loops, un-awaited `params`, and server components fetching their own API.
- In actions and routes, check login, permission, every field, server-side prices and minimal output. After a change, check revalidation.
- Write comments that are labelled, specific, kind and suggest a fix. As an author, keep PRs small and reply to every comment.

## Common mistakes

**1. Approving without reading the server-side changes.**

```text
LGTM!   (after looking only at the screenshots)
```

Symptom: the page looked fine, but the new action took the price from a hidden field. Fix: read every action and route handler in the diff with the five security questions.

**2. Missing a "use client" that moved up.**

```jsx
"use client";   // newly added to app/items/[id]/page.jsx to make one button work
```

Symptom: the PR works, but the item page now ships all its code to the phone and can't read data on the server. Fix: ask for the button to become its own small client component.

**3. Vague comments.**

```text
This is wrong.
```

Symptom: the author doesn't know what's wrong, how much it matters, or what you'd like instead, and a long back-and-forth follows. Fix: "blocking: X happens because Y. Could we do Z?"

**4. Blocking on taste.**

```text
blocking: I'd call this variable lines, not cartLines.
```

Symptom: a working, safe change waits two days over a name. Fix: label taste as a **nit** or a **suggestion**, and keep **blocking** for bugs, leaks and security.

**5. A giant pull request.**

```text
PR #91: new checkout, cart redesign, caching changes, and a library upgrade (2,400 lines)
```

Symptom: reviewers skim, and the one dangerous line slips through. Fix: split it into small PRs that each do one thing.
