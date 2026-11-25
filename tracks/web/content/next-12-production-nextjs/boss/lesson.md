## What happened

Arjun runs your mock Next.js loop today, and he plays the interviewer. He hands you the answers a previous candidate gave in three rounds. Each answer worked on the candidate's one quick try. Each one still failed the round.

1. **The API round.** "Write `GET /api/search?q=...&max=...` for the store's search box." The answer has five bugs that only show on edge cases.
2. **The server action round.** "Write `addToCart` for a form that uses `useActionState`." The answer works for the first click, and goes wrong from the second.
3. **The Next.js round.** "Show the catalogue two items per page, at `/?page=2` and so on." The answer was written for an older Next.js, and gets its edges wrong.

Your job: fix each answer the way an interviewer expects to see it fixed.

```callout jargon
A **mock loop** is a practice interview loop, run by a colleague who plays the interviewers. It feels real, but nothing rides on it except what you learn.
```

### Why the first answer finds nothing

The candidate tried `?q=Golu&max=50000`. It worked. Then the interviewer opened the search page with no filters at all, and got an empty list.

```animate
title: A missing max becomes zero
kind: code
language: js
code: |
  const params = new URLSearchParams("q=golu");
  const max = Number(params.get("max"));
  console.log(params.get("max"));
  console.log(max);
  console.log(45000 <= max);
steps:
  - line: 1
    say: The customer typed golu and set no price limit, so the URL has no max at all.
  - line: 2
    vars: { max: 0 }
    say: get returns null for a missing parameter. Number(null) is 0, not "no limit".
  - line: 3
    out: "null"
    say: Here is the null that get returned.
  - line: 4
    out: "0"
    say: And here it is as a number. The candidate now has a price limit of zero paise.
  - line: 5
    out: "false"
    say: Golu costs 45000, which is not at most 0. Every item is filtered out, so the search finds nothing.
```

The other four bugs hide the same way. "golu" doesn't match "Golu" without ignoring case. "abc" becomes `NaN`, which quietly filters everything out instead of answering 400. The results come back in catalogue order, and they carry every field, including what each item costs Dhaaga to make.

### Why the second answer breaks on the second click

Every `FormData` value is text. The first click adds a line with `qty: "2"`, a string. The second click does `existing.qty += qty`:

```js run
const line = { id: "DC-1042", qty: 2 };
const qty = "3";
console.log(line.qty + qty);
console.log(line.qty + Number(qty));
```

It prints `23`, then `5`. Adding a number and text joins them as text. And `+=` changes the old state object in place, which `useActionState` and React never expect. The answer also never checks the item, the quantity, or the stock.

## What you need

You learned each idea in an earlier ticket. Here they meet in one incident.

- Testing edges and the interview method: [Next.js interviews](/t/next-12-production-nextjs/topic-08-interviews).
- Route handlers with real `Request` and `Response`: [Route handlers](/t/next-11-data-and-server/topic-03-route-handlers) and [Testing a Next.js app](/t/next-12-production-nextjs/topic-01-testing-next-apps).
- Server actions, form state and checking input: [Forms with server actions](/t/next-11-data-and-server/topic-05-forms-with-actions) and [Security](/t/next-12-production-nextjs/topic-04-security).
- New arrays instead of changing old ones: [useReducer for the cart](/t/react-09-react-in-depth/topic-05-reducers).
- `searchParams` as a Promise, `redirect` and `notFound`: [Proxy, redirects and guards](/t/next-11-data-and-server/topic-08-proxy-and-redirects), and canonical addresses in [Accessibility and SEO](/t/next-12-production-nextjs/topic-03-accessibility-and-seo).

### One address per page

```callout jargon
**Pagination** splits a long list into numbered pages. With `?page=` in the URL, every page has its own address that customers can share and search engines can list.
```

Page 1 can be reached at both `/` and `/?page=1`. Two addresses for one page split its search ranking. So `/?page=1` redirects to `/`, page 2's Previous link points at `/`, and the metadata names `/` as the canonical address. A page number past the end, or one that isn't a number, is a real 404, not an empty list.

### Reading searchParams safely

`searchParams` arrives as a Promise of an object. A value is usually a string, but it's `undefined` when missing, and an **array** when the URL repeats it, like `?page=2&page=3`. So check that it's a string of digits before turning it into a number. `Number("2.5")` and `Number(" 2 ")` both give numbers, but neither is a page.

## The tiers

1. **Bronze** resolves the incident. The search ignores case and spaces, treats a missing `max` as no limit, answers 400 for a `max` that isn't a whole number, sorts cheapest first, and sends only `{ id, name, paise }`.
2. **Silver**: the action round. Number quantities, one line per item, checks for unknown items, bad quantities and stock, and a brand-new state every time.
3. **Gold**: the Next.js round. Await `searchParams`, show the right two items, Previous and Next only where they lead somewhere, redirect `?page=1` to `/`, 404 for bad pages, and metadata with a canonical address per page.

The exact rules are in the README.

## Recap

- Test the edges before you say "done": no filters, odd case and spaces, exactly the limit, bad input, and the order of the results.
- `searchParams.get(...)` returns `null` for a missing value, and `Number(null)` is 0. Treat "missing" and "bad" separately, and answer bad input with a 400.
- `FormData` values are text: turn them into numbers before adding. Validate the item, the quantity and the stock, and return brand-new state.
- In Next.js 16, `searchParams` is a Promise, and a value can be missing, a string or an array.
- Give each page one address: redirect duplicates, 404 what doesn't exist, and name the canonical in the metadata.

## Common mistakes

**1. Turning a missing max into zero.**

```js
const max = Number(params.get("max"));
```

Symptom: the bronze check says a search with no filters should list all five items, but got none. Fix: keep the text, and use `Infinity` when it's `null`: `const max = maxText === null ? Infinity : Number(maxText);`.

**2. Checking a number with isNaN only.**

```js
const bad = Number.isNaN(Number(maxText));
```

Symptom: `max=12.5` and `max=-1` still get through, and so does `max=`, because `Number("")` is 0. Fix: test the text itself: `/^\d+$/.test(maxText)`.

**3. Changing prevState in place.**

```js
existing.qty += qty;
prevState.lines.push({ id, qty });
```

Symptom: the silver check says prevState must stay exactly as it was. In the app, React may not notice the change, and the cart shows old numbers. Fix: `prevState.lines.map(...)` with a new object for the changed line, or `[...prevState.lines, newLine]`.

**4. Reading searchParams without await.**

```jsx
const page = Number(searchParams.page ?? 1);
```

Symptom: every URL shows page 1, because `searchParams.page` on a Promise is `undefined`. The gold check says `?page=2` should show the keychain and the tote bag. Fix: `const raw = (await searchParams).page;`.

**5. Off by one in the slice.**

```jsx
const shown = items.slice(page * 2, page * 2 + 2);
```

Symptom: page 1 shows the keychain and the tote bag, and page 3 is empty. Page numbers start at 1, but array positions start at 0. Fix: `items.slice((page - 1) * 2, page * 2)`.
