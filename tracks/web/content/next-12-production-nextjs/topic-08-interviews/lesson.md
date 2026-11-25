## Hook

A music exam isn't one song. The examiner hears your scales, a piece you prepared, a piece you've never seen, and asks you to clap back a rhythm. Each part shows a different skill, and you can practise every one.

A Next.js interview loop works the same way. Over a few rounds, interviewers check your JavaScript and React, how well you understand where Next.js code runs, whether you can build something under time pressure, and how you'd design a bigger feature. You've built every piece of this in the store. This ticket turns that into answers you can say out loud.

## Concept

### The Next.js loop

```callout jargon
An **interview loop** is the full set of interviews for one job, often on one day. Each **round** is one interview, usually 45 to 60 minutes.
```

A typical loop for a frontend engineer on a Next.js team:

| Round | What happens | What it checks |
|---|---|---|
| JavaScript and React | questions and small snippets | closures, promises, the event loop, state, effects |
| Next.js concepts | a conversation with small code samples | rendering, server and client components, data, caching |
| Coding | build a page, a route handler or an action in 45 to 90 minutes | working code, edge cases, clear talking |
| System design | design a feature like the product page or search | trade-offs across rendering, data, caching and performance |
| Behavioural | stories from your past work | ownership, teamwork, learning from mistakes |

Companies mix these differently. Ask the recruiter what your loop contains, and prepare for those rounds.

### Rendering questions

These come up in almost every Next.js round. Be able to explain each in two or three sentences, with a Dhaaga example.

```callout jargon
**SSR** (server-side rendering) builds a page's HTML on the server for each request. **SSG** (static site generation) builds it once, ahead of time. **CSR** (client-side rendering) sends little HTML and builds the page in the browser with JavaScript.
```

- **Static or dynamic?** Golu's page is the same for everyone, so it's built ahead of time. The cart reads a cookie, so it's rendered per request.
- **Server or client component?** Server by default. Add `"use client"` only for state, effects, event handlers or browser APIs, as low in the tree as possible.
- **What is hydration?** The browser attaches handlers and state to HTML that's already on screen.
- **What can a server component do that a client one can't?** Be `async`, read the data layer and secrets directly, and send no JavaScript.

### Data and mutation questions

- **Where do you fetch data?** In server components, straight from the data layer, with `Promise.all` for independent calls.
- **Route handler or server action?** A route handler is a URL for anyone, like a mobile app or a partner calling `GET /api/stock`. A server action is for your own forms and buttons to change data.
- **How does the page update after an order?** The action calls `revalidatePath` or `revalidateTag`, so cached pages show the new stock.
- **How do you keep it safe?** Check the session and validate input inside every action and route handler, and never trust prices from the browser.

### Debugging questions

Interviewers love "this works on my laptop but breaks in production" questions. A classic is the hydration mismatch.

```callout jargon
A **hydration mismatch** happens when the HTML the server rendered differs from what React renders in the browser for the same component. React warns, and may throw away the server's HTML and redraw.
```

```animate
title: The same component, two different answers
kind: code
language: js
code: |
  const greeting = (hour) => (hour < 18 ? "Order before 6 pm for same-day dispatch" : "Order now for tomorrow's dispatch");
  const serverHtml = greeting(17);
  const browserHtml = greeting(18);
  console.log(serverHtml === browserHtml ? "match" : "hydration mismatch");
steps:
  - line: 1
    say: The component picks its words from the current hour. That looks harmless.
  - line: 2
    vars: { serverHtml: '"Order before 6 pm..."' }
    say: The server rendered it at 17:59, so the HTML says order before 6 pm.
  - line: 3
    vars: { browserHtml: '"Order now for tomorrow..."' }
    say: The customer's phone hydrated it a moment later, at 18:00. Different words.
  - line: 4
    out: "hydration mismatch"
    say: Server and browser disagree. Fix it by deciding the time in one place, or showing time-based text only after hydration in an effect.
```

Other common causes: `Math.random()` or `new Date()` in render, reading `window` or `localStorage` while rendering, and invalid HTML like a `<div>` inside a `<p>`.

Other favourites:

- **"`params.id` is undefined."** In Next.js 16, `params` is a Promise. `await` it.
- **"It works in dev but the price is old in production."** A cached page that was never revalidated.
- **"The page got slow after a small change."** Someone added `cookies()` and made it dynamic, or `"use client"` to a big component.

### The coding round

The method is the same for any coding round:

1. **Clarify.** Inputs, edge cases, what "done" means. "Can the id be unknown? Should it work without JavaScript?"
2. **Examples.** Two or three inputs and their answers, including an awkward one.
3. **Plan out loud.** "A server component for the page, one client island for the button."
4. **Code.** Clear names, small pieces. Keep talking.
5. **Test by hand.** Walk the examples through, then the edges: unknown id, empty list, quantity 0.
6. **Improve.** Remove repeated work, add a check, handle failure.

A Next.js coding task is often "build the item page", "add a route handler", or "write the action for this form". Each one has the edges you've met in this module: `params` as a Promise, `notFound()`, validating every field, and server-side prices.

### Next.js system design

```callout jargon
**System design** is a discussion round about building a larger feature. There's no single right answer: the interviewer watches how you reason about trade-offs.
```

A shape that works for "design the product page for dhaaga.in":

1. **Requirements.** Thousands of items, mostly mobile visitors, stock that changes often, shared on WhatsApp.
2. **Rendering.** Product pages static with `generateStaticParams`, revalidated when stock or price changes. The cart dynamic.
3. **Data.** The page reads the data layer in parallel. Live stock comes from a small route handler, or streams in with `Suspense`.
4. **Client islands.** Only Add to cart and the photo gallery run in the browser.
5. **Caching.** Hashed files cached for a year, pages revalidated on change, nothing personal cached.
6. **Failure.** `notFound()` for unknown ids, `error.js` with retry, logs with digests.
7. **Accessibility and SEO.** Landmarks, one `<h1>`, alt text, `generateMetadata` with Open Graph, a sitemap.

Draw it, and talk through each step.

### Talking trade-offs

```callout jargon
A **trade-off** is a choice where you gain one thing by giving up another. Strong candidates name both sides.
```

Every design choice has a cost. Say it before the interviewer asks:

- "Static product pages are fast and cheap to serve, but the stock can be a little old, so I'd revalidate on every order and show live stock in a small island."
- "A server action is simplest for our own form, but a partner app can't call it, so the stock check also gets a route handler."
- "A nonce-based CSP is stricter, but it makes every page dynamic. For launch I'd start with a CSP in `next.config` and revisit."

### Behavioural questions

Behavioural rounds ask about real work: a bug you're proud of, a disagreement, something that went wrong. Use **STAR**: the **S**ituation, your **T**ask, the **A**ction you took and the **R**esult, with a number if you can.

```text
Situation: Golu's page took almost 2 seconds to show on mobile data.
Task: make it fast without redesigning the page.
Action: I found three data calls awaited one by one and started them together
with Promise.all, kept only the Add to cart button as a client component,
and built every item page ahead of time with generateStaticParams.
Result: the server's wait dropped from about 1.8 s to 0.6 s, and the page shipped far less JavaScript.
```

Say "I" for your part, and be honest about mistakes and what you learned.

## Worked example

Here's a Next.js concepts question, answered the way an interviewer hopes to hear.

**Question:** "When would you use a route handler instead of a server action?"

**Answer, out loud:** "Both run on the server. A server action is a function my own forms and buttons call to change data, like placing an order. Next.js gives it a hidden endpoint, so I still check the session and validate every field inside it. A route handler is a real URL, like `GET /api/stock?id=DC-1042`. I'd use one when something outside my React code needs to call it: a mobile app, a partner, a webhook from the payment gateway. For Dhaaga, Buy now is an action, and the stock check for the delivery partner is a route handler."

**Why it's strong:**

- It starts with what they share, then the real difference: who calls it.
- It mentions security without being asked.
- It ends with a concrete example from real work.

## Try it

Interviewers sometimes ask you to build a tiny version of a framework feature. This is a simple "revalidate after N seconds" cache, with the time passed in so it's easy to test. Run it.

```js run
function cached(fetcher, seconds) {
  let value;
  let savedAt = -Infinity;
  return (now) => {
    if (now - savedAt >= seconds) {
      value = fetcher(now);
      savedAt = now;
    }
    return value;
  };
}

let calls = 0;
const stock = cached((now) => {
  calls += 1;
  return "stock read at " + now + " s";
}, 60);

console.log(stock(0));
console.log(stock(30));
console.log(stock(61));
console.log("calls: " + calls);
```

It prints:

```text
stock read at 0 s
stock read at 0 s
stock read at 61 s
calls: 2
```

1. Say out loud what happens at 30 seconds, and why the customer sees data from 0 seconds.
2. Next.js serves the old value while it fetches a new one in the background. How would you change `cached` to do that? Talk through it before writing code.

## Recap

- A Next.js loop usually has JavaScript and React, Next.js concepts, coding, system design and behavioural rounds. Ask which ones yours has.
- Explain static and dynamic rendering, server and client components, and hydration with a real example.
- Know where data is fetched, when to use a route handler or a server action, how revalidation works, and how to keep both safe.
- Debug out loud: hydration mismatches, `params` as a Promise, stale caches, and pages turned dynamic by accident.
- In system design, go from requirements to rendering, data, client islands, caching, failure, accessibility and SEO, naming the trade-off of each choice. Tell STAR stories with results.

## Common mistakes

**1. Saying "Next.js is faster" with no reason.**

```text
"I'd use Next.js because it's faster."
```

Symptom: the interviewer asks "faster how?" and the answer stalls. Fix: name the mechanism: "Pages built ahead of time and served from a CDN, and server components that send no JavaScript."

**2. Making everything a client component.**

```text
"I'd put 'use client' on the page and fetch in a useEffect."
```

Symptom: the interviewer asks about bundle size and first load, and the design has no good answer. Fix: start from server components and add small client islands only where you need interaction.

**3. Forgetting that actions are public.**

```text
"The form only shows to logged-in customers, so the action is safe."
```

Symptom: the interviewer asks "what if someone POSTs to it directly?". Fix: say up front that every action checks the session and validates its input.

**4. Designing without trade-offs.**

```text
"Every page is static." (and nothing about stock that changes every minute)
```

Symptom: the interviewer points out stale stock, and it sounds like you hadn't thought of it. Fix: say the cost of each choice and how you'd soften it, like revalidating on each order.

**5. Coding in silence.**

```text
(ten minutes of typing, then "done")
```

Symptom: the interviewer can't follow your plan, and can't help when you get stuck. Fix: say what you're doing and why, test out loud, and ask when you're unsure.
