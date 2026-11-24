## Hook

A cricket trial isn't one test. The selectors watch you bat in the nets, field, bowl a few overs, and play a match situation. Each part shows something different. A great batter who can't field still has work to do.

A frontend interview loop is the same. Over a few rounds, interviewers check how well you know JavaScript, whether you can build a working, accessible component under time pressure, how you'd design a bigger feature, and how you work with people. None of it is magic. Each round has a shape, and you can practise every one.

## Concept

### The frontend loop

```callout jargon
An **interview loop** is the full set of interviews for one job, often on one day. Each **round** is one interview, usually 45 to 60 minutes, with one or two interviewers.
```

A typical loop for a frontend engineer at a product company:

| Round | What happens | What it checks |
|---|---|---|
| Recruiter or phone screen | a chat, or a short coding question | basics and communication |
| JavaScript fundamentals | questions and small snippets | closures, `this`, the event loop, promises |
| UI coding ("machine coding") | build a component in 45 to 90 minutes | working React, state, accessibility, clean code |
| Problem solving | a data or algorithm question in JavaScript | arrays, maps, edge cases, complexity |
| Frontend system design | design a feature like search or a feed | components, data, performance, trade-offs |
| Behavioural | questions about your past work | teamwork, ownership, handling conflict |

Companies mix these differently. Some send a **take-home** project instead of a UI round. Ask the recruiter what the loop contains: they're happy to tell you, and it lets you prepare for the right things.

### JavaScript fundamentals

These come up again and again, because they explain how the language really behaves:

- **Closures**: a function remembers the variables around it, from [Scope and closures](/t/web-04-javascript-in-depth/topic-01-scope-and-closures).
- **`this`**: what it is in a method, an arrow function, and a callback.
- **`==` and `===`**, and why you almost always want `===`.
- **Hoisting and `let`/`const`**: why using a `let` before its line throws.
- **Promises and `async`/`await`**: what runs when, and how errors travel.
- **The event loop**: why `setTimeout(..., 0)` doesn't run straight away.

A favourite question shows a snippet and asks "what does this print?". Answer out loud, line by line, as if you were the engine.

### The event loop question

```callout jargon
A **microtask** is a small job, like a promise's `.then`, that runs as soon as the current code finishes. A **task** (or macrotask), like a `setTimeout` callback, waits until all microtasks are done.
```

```animate
title: What runs when?
kind: code
language: js
code: |
  console.log("1 order placed");
  setTimeout(() => console.log("4 SMS sent"), 0);
  Promise.resolve().then(() => console.log("3 stock held"));
  console.log("2 thank-you shown");
steps:
  - line: 1
    out: "1 order placed"
    say: Plain code runs first, top to bottom.
  - line: 2
    say: setTimeout puts its callback in the task queue. Even with 0 ms, it must wait its turn.
  - line: 3
    say: The promise is already resolved, so its then callback goes in the microtask queue.
  - line: 4
    out: "2 thank-you shown"
    say: The plain code carries on to the end.
  - line: 3
    out: "3 stock held"
    say: The current code is finished. Microtasks run first, before any timer.
  - line: 2
    out: "4 SMS sent"
    say: Only then does the timer's task run. Sync, then microtasks, then tasks.
```

You met this in [Waiting without freezing: the event loop](/t/web-06-async-javascript/topic-01-sync-and-async). In an interview, explain the queues out loud, not just the answer.

### The UI coding round

You get a short brief, like "build a star rating", "an accordion", "an autocomplete", "a modal" or "tabs", and an editor with React. In 45 to 90 minutes, interviewers look for:

1. **It works**, for the main path and the edges: nothing selected, the last item, fast clicks.
2. **Clean state**: as little state as possible, with the rest worked out from it.
3. **Accessibility**: real elements, labels, keyboard use, focus. Many candidates skip this, so it stands out when you don't. Everything from [Accessibility in depth](/t/react-10-professional-frontend/topic-02-accessibility-in-depth) counts here.
4. **Clear code**: small components, good names, props that make sense.
5. **Talking**: you say what you're doing and why, so they can follow and help.

Styling usually matters least. A plain, working, accessible component beats a beautiful one that only works with a mouse.

### A method for any coding round

The method is the same for a UI round and a problem-solving round:

1. **Clarify.** Repeat the problem in your words. Ask about inputs, edge cases and what "done" means. "Can the list be empty? Should it work with a keyboard?"
2. **Examples.** Write two or three small inputs and their answers, including an awkward one.
3. **Plan.** Say your approach, and its cost, before you code. Start with the simple version.
4. **Code.** Clear names, small functions. Keep talking.
5. **Test by hand.** Walk your examples through the code, line by line. Then the edges: empty, one item, duplicates, the biggest value.
6. **Improve.** If there's time, or they ask "can you do better?", find the repeated work and remove it.

When you get stuck, say what you're thinking. Interviewers give hints to candidates who explain where they are. They can't help silence.

### Classic utility questions

Some small functions come up so often that it's worth writing each one once yourself:

- **debounce** and **throttle**, from [Debounce: searching as you type](/t/web-06-async-javascript/topic-10-debounce);
- **group by** a key, and **count** things, with a `Map` or an object;
- **flatten** nested arrays;
- **deep clone** a plain object;
- your own **`Promise.all`**;
- a **custom hook** such as `useFetch` or `useLocalStorage`, with its cleanup.

For each one, know the edge cases. What if the list is empty? What if two calls overlap? What if a request finishes after the component has gone?

### Frontend system design

```callout jargon
**Frontend system design** is a discussion round about building a bigger feature, such as "design the store's search box" or "design a product feed". There is no single right answer: interviewers look at how you reason about trade-offs.
```

A shape that works for most questions:

1. **Requirements.** What must it do? How many products, how many users, which devices, how fast?
2. **Components.** Sketch the tree: `SearchBox`, `SuggestionList`, `SuggestionItem`.
3. **Data and state.** What does the API return? Where does each piece of state live?
4. **Network.** Debounce typing, cancel stale requests, cache recent answers, handle errors and slow 3G.
5. **Performance.** Bundle size, images, long lists, and the Core Web Vitals.
6. **Accessibility.** A search box with suggestions is a combobox: arrow keys, `aria-expanded`, announcing results.
7. **Trade-offs.** Say what you'd do first, and what you'd add later.

Talk through each step out loud, and draw. Interviewers care more about your reasons than about the boxes.

### Behavioural questions and STAR

Behavioural rounds ask about real things you've done:

- "Tell me about a bug you're proud of finding."
- "Tell me about a time you disagreed with a teammate."
- "Tell me about something you shipped that went wrong."

```callout jargon
**STAR** is a way to tell a work story: the **S**ituation, your **T**ask, the **A**ction you took, and the **R**esult, with numbers if you can.
```

```text
Situation: before Dhaaga's launch, the catalogue loaded 36 MB of photos on first visit.
Task: I had to make it usable on slow phone data, without redesigning the page.
Action: I measured with Lighthouse, resized the photos to 400 and 800 pixels,
added width, height and lazy loading, and checked LCP again after each change.
Result: first load went from about 36 MB to under 300 KB, and LCP from 9 s to 2.1 s.
```

Prepare five or six stories from your tickets before the loop. Most questions can be answered by one of them. Say "I", not "we", for your own part, and be honest about mistakes: what you learned matters more than looking perfect.

### Practising

- **Code with a timer**, out loud, in a plain editor without autocomplete.
- **Build the classic components**: tabs, accordion, modal, autocomplete, star rating, each with the keyboard and a screen reader in mind.
- **Do mock interviews** with a friend, then swap roles. Interviewing someone else shows you what interviewers see.
- **Read your old code** and explain it, as if to an interviewer.

## Worked example

Here's a UI round, start to finish. The brief: "Build a star rating for Golu's review form. It takes `value` and `onChange`."

**Clarify.** "Can the customer clear the rating? Should it work with the keyboard and a screen reader?" The interviewer says: no clearing, and yes to the keyboard.

**Plan, out loud.** "A rating is one choice out of five, so I'll use five real radio buttons in a fieldset. The browser then gives me arrow keys, focus and the announcement for free. I'll style the labels as stars."

**Code.**

```jsx
export function StarRating({ value, onChange }) {
  return (
    <fieldset className="stars">
      <legend>Your rating</legend>
      {[1, 2, 3, 4, 5].map((n) => (
        <label key={n}>
          <input type="radio" name="rating" value={n} checked={value === n} onChange={() => onChange(n)} />
          <span aria-hidden="true">{n <= value ? "★" : "☆"}</span>
          <span className="visually-hidden">{n} {n === 1 ? "star" : "stars"}</span>
        </label>
      ))}
    </fieldset>
  );
}
```

**Test by hand, out loud.** "With `value` 0, all five show ☆, and none is checked. Clicking the third calls `onChange(3)`. With `value` 3, three stars fill, and a screen reader hears '3 stars, radio button, checked, 3 of 5'. Arrow keys move between the radios, because they share a `name`."

**Improve.** "If I had more time, I'd write an RTL test that tabs in and presses the arrow keys, and check the colour contrast of the stars."

The interviewer saw a working component, accessibility by choosing native elements, testing, and clear talking. That's a strong round, even with no fancy styling.

## Try it

Here's a small problem-solving question: "Find two different products that together cost exactly a customer's gift budget." The answer uses a `Map` to remember prices already seen, so it needs only one pass.

```js run
function pairForBudget(products, budget) {
  const seen = new Map();
  for (const p of products) {
    const need = budget - p.paise;
    if (seen.has(need)) return [seen.get(need).name, p.name];
    seen.set(p.paise, p);
  }
  return null;
}

const shelf = [
  { name: "Keychain", paise: 12000 },
  { name: "Golu", paise: 45000 },
  { name: "Coaster set", paise: 15000 },
  { name: "Booties", paise: 35000 },
];
console.log(JSON.stringify(pairForBudget(shelf, 50000)));
console.log(JSON.stringify(pairForBudget(shelf, 100000)));
```

It prints:

```text
["Coaster set","Booties"]
null
```

1. Say the brute-force answer out loud: two loops over every pair. What's its cost for n products? And this one's?
2. Try a budget of `90000`, when only one Golu is on the shelf. Does the code wrongly use Golu twice? Why not?

## Recap

- A frontend loop has several rounds: JavaScript fundamentals, UI coding, problem solving, frontend system design and behavioural. Ask the recruiter which ones yours has.
- Know closures, `this`, `===`, promises and the event loop's order: sync code, then microtasks, then tasks.
- In UI rounds, a working, accessible component with clean state and clear talking beats a beautiful one.
- Use one method for every coding round: clarify, examples, plan, code, test by hand, improve. Talk when you're stuck.
- In system design, go from requirements to components, data, network, performance and accessibility, with trade-offs. In behavioural rounds, tell real STAR stories with results.

## Common mistakes

**1. Coding before clarifying.**

```text
Interviewer: "Build an autocomplete."   You: (starts typing at once)
```

Symptom: forty minutes in, you learn it had to work with the keyboard and on slow networks, and there's no time left. Fix: spend the first five minutes asking about inputs, edges, devices and what "done" means.

**2. A div soup component.**

```jsx
<div className="star" onClick={() => onChange(3)}>★</div>
```

Symptom: it works with a mouse, and the interviewer asks "how would a keyboard user rate?". You have no answer. Fix: reach for native elements first, such as radio buttons, buttons and labels, and say why.

**3. Never testing by hand.**

```text
"Done!"   (never tried an empty list, the last item, or a value of 0)
```

Symptom: the interviewer types one edge case, and the answer breaks. Fix: before saying done, walk your examples and the edge cases through the code, out loud.

**4. Silence when stuck.**

```text
(three minutes of quiet typing and deleting)
```

Symptom: the interviewer can't tell whether you're close or lost, so they can't give a hint. Fix: say what you know, what you're unsure about, and what you're trying next.

**5. A behavioural answer with no "I" and no result.**

```text
"We had a performance problem and we fixed it as a team."
```

Symptom: the interviewer can't tell what you did, or whether it worked. Fix: use STAR. Say what you did, and end with a result, ideally a number.
