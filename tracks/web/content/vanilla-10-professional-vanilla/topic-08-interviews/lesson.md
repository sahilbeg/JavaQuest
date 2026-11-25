## Hook

In a driving test, the examiner doesn't only check that you reach the end of the road. They watch how you get there: mirrors, signals, how you handle a cow that wanders in front of you.

A frontend interview works the same way. A working answer matters, but the interviewer is mostly watching how you think. How do you make a vague question clear? Do you spot the empty list and the cow on the road? Do you talk while you work? This ticket shows you what they look for, so you can show it.

## Concept

### What a frontend interview loop looks like

```callout jargon
An **interview loop** is the set of interviews a company runs for one job, often on the same day. Each **round** tests something different, with a different interviewer.
```

A typical frontend loop has four or five rounds:

| Round | What happens | What they look for |
|---|---|---|
| Screening | A short call about you and the role | Can you explain what you've built? |
| JavaScript | Write a small function, or predict what code prints | The language itself: closures, the event loop, edge cases |
| DOM / UI | Build a small widget in a browser, often with no framework | The DOM, events, state, accessibility |
| Frontend system design | Talk through how you'd build a bigger feature | Structure, data, performance, trade-offs |
| Behavioural | Stories from your past work | Teamwork, ownership, learning from mistakes |

Some companies use plain JavaScript on purpose, even if they use a framework at work. It shows whether you understand what the framework does for you.

### A method for every coding question

Strong candidates follow the same steps, out loud, every time:

1. **Clarify.** Repeat the question in your own words. Ask about inputs, outputs and limits: "Can the list be empty? Should I change the array I'm given?"
2. **Examples.** Write two or three small examples, including edge cases: empty, one item, duplicates, the boundary.
3. **Plan.** Say your approach before you type: "I'll keep a flag in a closure."
4. **Code.** Write clear code with good names. Keep talking about what you're doing.
5. **Test.** Run your examples through the code by hand, line by line. Fix what breaks.
6. **Improve.** Mention what you'd change with more time: speed, memory, readability.

```callout tip
Silence is the most common way to fail a round you could have passed. If you're stuck, say what you're thinking. Interviewers often give a hint when they can see where you are.
```

### Questions about JavaScript itself

Many rounds check that you know how the language really behaves. Common topics:

- **The event loop**: which code runs first, synchronous code, promise callbacks or timers. You met it in [Waiting without freezing](/t/web-06-async-javascript/topic-01-sync-and-async).
- **Closures**: functions that remember variables, as in [Scope and closures](/t/web-04-javascript-in-depth/topic-01-scope-and-closures).
- **`this`**, copying objects, `==` versus `===`, and how `async`/`await` works underneath.
- **Write a small helper**: `debounce`, `once`, a deep copy, grouping or flattening arrays.

The favourite prediction question is the event loop:

```animate
title: Who prints first?
kind: code
language: js
code: |
  console.log("A");
  setTimeout(() => console.log("B"), 0);
  Promise.resolve().then(() => console.log("C"));
  console.log("D");
steps:
  - line: 1
    out: "A"
    say: Synchronous code runs first, top to bottom. A prints straight away.
  - line: 2
    say: setTimeout puts B in the timer queue. Even 0 ms means "later, after everything else".
  - line: 3
    say: The promise callback goes in the microtask queue. Microtasks run as soon as the current code finishes.
  - line: 4
    out: "D"
    say: D is still synchronous, so it prints before either callback.
  - line: 3
    out: "C"
    say: The script is done. Microtasks run first, so C prints.
  - line: 2
    out: "B"
    say: Only then does the timer run. The answer is A, D, C, B.
```

### Questions about the DOM

Without a framework, you're expected to know the browser itself:

- **Events**: bubbling, `event.target` versus `event.currentTarget`, `preventDefault()` versus `stopPropagation()`.
- **Event delegation**: one listener on a list instead of one per button, from [Event delegation](/t/web-05-dom-and-events/topic-06-event-delegation).
- **Performance**: fragments, and reading layout before writing, from [Fast rendering](/t/vanilla-10-professional-vanilla/topic-01-rendering-performance).
- **Safety**: why `textContent` and not `innerHTML` for user text.

### The UI round: build a widget

"Build an accordion", "build a to-do list", "build a photo carousel": the UI round asks for a small widget, in an online editor or a blank HTML file. A good answer has a clear shape:

1. **HTML first**: real buttons, lists and labels.
2. **One state object**: `{ open: 1 }` or `{ items: [], filter: "all" }`.
3. **A `render()` function** that draws everything from the state.
4. **Events** that change the state and call `render()`, with delegation where there are many items.
5. **Accessibility**: keyboard, `aria-expanded` or `aria-selected`, focus that goes somewhere sensible.
6. **Edge cases**: no items, one item, a very long name.

That's the same state-and-render pattern from [State and render](/t/web-05-dom-and-events/topic-10-state-and-render). Interviewers love seeing it, because it scales.

### Frontend system design

```callout jargon
A **system design** round is a conversation, not code. You're asked to design something big, like "the product page of an online store", and you talk through the parts, the data, and the trade-offs, often drawing boxes.
```

A useful order for your answer:

1. **Requirements**: what must it do, for whom, on which devices? How many products, how fast a network?
2. **Components**: the page's parts, like gallery, price box, add-to-cart, reviews, and which owns what state.
3. **Data and API**: what each part needs from the server, and in what shape.
4. **Performance**: what loads first, images, lazy loading, caching, slow 3G.
5. **Accessibility, SEO and security**: keyboard paths, prerendered HTML, safe rendering of reviews.
6. **Trade-offs**: say what you'd give up, and why. There is rarely one right answer.

### Behavioural questions: tell a real story

"Tell me about a time you made a mistake." "Tell me about a disagreement with a teammate." These questions check how you work with people.

```callout jargon
**STAR** is a way to answer them: the **Situation** (where and when), your **Task** (what you had to do), the **Actions** you took yourself, and the **Result**, with what you learned.
```

Use real stories from your own work, including this course: the gift note that ran code, the price that stayed cached for a day. Say "I", not only "we", so the interviewer hears what **you** did. A mistake you learned from is a great story.

## Worked example

Here is the method on one real question: "Write `once(fn)`. It returns a function that calls `fn` the first time only, and returns that first result on every later call."

**Clarify.** "Should later calls get the first result, or `undefined`?" The first result. "Are arguments passed through?" Yes, on the first call.

**Examples.**

- `placeOrder = once(...)`. Call it with `order-17`, then with `order-18`: both calls return the result for `order-17`, and the order is placed once.
- Edge case: what if `fn` returns `undefined`? Then later calls must still not run it again. So a flag is safer than checking the result.

**Plan.** "A closure keeps a `called` flag and the first `result`."

**Code.**

```js
function once(fn) {
  let called = false;
  let result;
  return (...args) => {
    if (!called) {
      called = true;
      result = fn(...args);
    }
    return result;
  };
}
```

**Test.** Run both examples by hand. On the first call, `called` is false, so `fn` runs and `result` is kept. On the second, `called` is true, so the kept result comes back.

**Improve.** "If `fn` throws, `called` is already true, so it never retries. I'd ask whether a failed first call should be allowed to run again."

## Try it

This runs the worked example's tests. Run it.

```js run
function once(fn) {
  let called = false;
  let result;
  return (...args) => {
    if (!called) {
      called = true;
      result = fn(...args);
    }
    return result;
  };
}

let ordersPlaced = 0;
const placeOrder = once((id) => {
  ordersPlaced += 1;
  return `placed ${id}`;
});

console.log(placeOrder("order-17"));
console.log(placeOrder("order-18"));
console.log("orders placed:", ordersPlaced);
```

It prints:

```text
placed order-17
placed order-17
orders placed: 1
```

1. Make the inner function return `undefined` instead of the text. Is it still called only once?
2. Change `once` so that if `fn` throws, the next call tries again. Test it with a function that throws on its first call only.

## Recap

- A frontend loop usually has JavaScript, DOM or UI, system design and behavioural rounds. Interviewers watch how you think, not just whether it works.
- For every coding question: clarify, write examples with edge cases, plan out loud, code, test by hand, then suggest improvements.
- Know the language: the event loop order (synchronous code, then microtasks, then timers), closures, `this`, and small helpers.
- In a UI round without a framework, use HTML first, one state object, a `render()` function, delegated events, and accessibility.
- In design rounds, go from requirements to components, data, performance and trade-offs. In behavioural rounds, tell real STAR stories about what you did.

## Common mistakes

**1. Coding before clarifying.**

```js
function groupByColour(items) {
  // started typing 10 seconds after hearing the question
}
```

Symptom: twenty minutes later you find the interviewer wanted counts, not lists, and there's no time left. Fix: spend the first few minutes on questions and examples. It's never wasted.

**2. Working in silence.**

```text
Interviewer: "How's it going?"   Candidate: "...one minute."  (eight minutes pass)
```

Symptom: the interviewer can't tell whether you're close or lost, so they can't give credit or a hint. Fix: say your plan and what you're trying, even when unsure.

**3. Testing only the happy path.**

```js
console.log([45000, 15000].reduce((sum, paise) => sum + paise));
```

Symptom: it works for two prices, but `[].reduce(...)` with no starting value throws on an empty cart. Fix: always try empty, one item and the boundaries, and give `reduce` a starting value of `0`.

**4. A widget that only works with a mouse.**

```js
const header = document.createElement("div");
header.addEventListener("click", () => console.log("toggle"));
```

Symptom: the interviewer presses Tab and nothing is reachable. In a no-framework round, that's what they were checking. Fix: real `<button>` elements, `aria-expanded`, and focus that moves sensibly.

**5. "We" stories with no "I".**

```text
"We had a bug, and we fixed it, and then we shipped."
```

Symptom: the interviewer still has no idea what you did. Fix: STAR, in the first person: "I found that the slip used innerHTML, I wrote a test that failed, and I changed it to textContent."
