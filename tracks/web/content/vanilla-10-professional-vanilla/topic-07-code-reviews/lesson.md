## Hook

Before a big order leaves the Aundh shop, Meera Aunty turns every piece over in her hands. She isn't checking whether the maker is good. She's checking the stitches, because one loose stitch unravels a whole coaster in a customer's kitchen.

A code review is the same habit. A teammate looks over a change before it reaches customers. Done well, it catches the loose stitches, spreads know-how around the team, and makes everyone's code better, including the reviewer's.

## Concept

### Why we review code

```callout jargon
A **pull request** (PR) asks to merge a branch's changes into `main`. A **code review** is a teammate reading and trying that change, and leaving comments, before it's merged. **Approve** means "this can go in".
```

You opened pull requests in [GitHub and pull requests](/t/web-07-tooling/topic-05-github-pull-requests). A review has four jobs:

- **Catch problems** before customers do: bugs, security holes, pages a keyboard can't use.
- **Share knowledge**: after a review, two people understand the new cart code, not one.
- **Keep the code consistent**, so the codebase reads like one team wrote it.
- **Teach, both ways**: reviewers learn new tricks from the code they read.

A review is **not** a test of the author. The question is always "is this change good for the store?", never "is this person good?".

### Small pull requests with a good description

Reviewers find more problems in small changes. A 50-line PR gets careful reading. A 1,400-line PR gets skimmed, and the bug hides in line 900.

- **One idea per PR.** "Fix the booties price" and "redesign the header" are two PRs.
- **Aim for a few hundred lines at most.** Split big features into steps that each work.
- **Write a description** that answers what, why, and how to test.

```text
Title: Cart: keep focus on the quantity select after it changes

Why: keyboard and screen reader users lost their place every time they changed a quantity.
What: render() remembers the focused element's id and focuses the new one after redrawing.
How to test: open the preview, Tab to Golu's quantity, press Down twice. Focus should stay on it.
Screenshots: before / after of the focus ring.
```

### What to look for in frontend code

Read the description first, then the tests, then the code. Then **run it**: open the preview link and use the change with a mouse, then with only the keyboard.

| Area | Questions to ask |
|---|---|
| Correctness | Does it do what the ticket asked? Empty cart, one item, the ₹999 and ₹1,500 edges? Money in whole paise? |
| Security | Does any customer or URL text reach `innerHTML`, `href` or `eval`? Any secret in the code? |
| Accessibility | Labels on fields, names on buttons, focus moved on purpose, every action possible by keyboard? |
| Performance | Reads and writes mixed in a loop? Work repeated on every keystroke? Huge images? |
| Leaks | Listeners or subscriptions added on every render, and never removed? |
| Tests | Would they fail if the code were wrong? Is the tricky part tested? |
| Readability | Clear names, small functions, no dead code or leftover `console.log`? |

Plain-JavaScript code has one classic bug that frameworks hide: adding a listener inside a function that runs again and again.

```animate
title: A listener added on every render
kind: code
language: js
code: |
  const listeners = [];
  function render() {
    listeners.push(() => "add Golu");
  }
  render();
  render();
  render();
  console.log("listeners:", listeners.length);
  console.log("one click adds", listeners.length, "Golus");
steps:
  - line: 1
    say: listeners stands for the click listeners on the Add button.
  - line: 3
    say: render() adds a listener each time it runs, like addEventListener inside render would.
  - line: 7
    say: The cart renders three times as the customer shops. Each time, one more listener piles up.
  - line: 8
    out: "listeners: 3"
    say: Three listeners now wait on the same button.
  - line: 9
    out: "one click adds 3 Golus"
    say: One click runs all three. A reviewer spots this by asking how often the line with addEventListener runs.
```

### Syntax: a review comment

```text
blocking: review.text goes into innerHTML here, so a review containing <img onerror> would run code
on every customer's screen. Could we build the <p> with createElement and set textContent instead?
```

A useful comment has three parts: **what** you see, **why** it matters, and a **suggestion**. Start it with a label, so the author knows how much it matters:

| Label | Meaning |
|---|---|
| `blocking:` | Must change before merging: a bug, a security hole, a broken keyboard path |
| `suggestion:` | A better way, but the author decides |
| `question:` | You don't understand something yet. Maybe the code needs a comment |
| `nit:` | A tiny style point. Never a reason to hold the PR |

Comment on the code, not the person: "this function reads `offsetHeight` inside the loop", not "you always write slow code". And say what's good too: "Nice, moving focus to the heading here is exactly right."

### Receiving feedback

Your own PRs will get comments, often many. That's the process working, not a judgement of you.

- **Assume good intent.** The reviewer spent their time to make your code better.
- **Reply to every comment**: "Done in 3f2a1c4", or a short reason why you'd rather not.
- **Ask when unsure.** "Do you mean the label or the select?" beats guessing.
- **Move long debates to a call.** Ten comments back and forth means it's time to talk for five minutes.

### Review your own PR first

```callout jargon
A **self-review** is reading your own diff on GitHub, line by line, as if someone else wrote it, before you ask anyone to review.
```

You'll catch leftover `console.log` lines, a TODO you forgot, a file you didn't mean to change, and often a real bug. Then run through the checklist yourself, including the keyboard test. Your reviewer can spend their time on the hard parts.

## Worked example

A teammate opens a PR that adds a "Recently viewed" strip to the product page. Here is part of the diff:

```js
function renderRecent(strip, items) {
  strip.innerHTML = items.map((item) => `<a href="/products/${item.id}">${item.name}</a>`).join("");
  strip.addEventListener("click", (event) => {
    console.log("recent clicked", event.target);
  });
  for (const link of strip.querySelectorAll("a")) {
    link.style.width = `${link.offsetWidth + 10}px`;
  }
}
```

A good review leaves three comments, each labelled, each with a reason and a suggestion:

```text
blocking (line 2): item.name comes from the catalogue, which staff can edit in the admin page.
innerHTML would turn any tag in a name into HTML. Could we build the links with createElement
and set textContent?

blocking (line 3): renderRecent runs every time the strip updates, so a new click listener is
added each time. Could the listener be added once, outside renderRecent? Also, the console.log
looks left over.

suggestion (lines 6-8): offsetWidth is read right after the previous link's width was written,
so the browser lays out once per link. Reading every width first, then writing, would avoid
that. Not urgent with five links.
```

And one more: "Nice idea to keep the strip to five items, it keeps the page light." Then the author replies to each comment, pushes a fix, and asks for another look.

## Try it

Some review checks can be automated. This tiny "review bot" scans four changed lines for three risky patterns. Run it.

```js run
const diff = [
  'list.innerHTML = `<li>${review.text}</li>`;',
  "button.addEventListener(\"click\", addToCart);",
  "const total = price * 1.1;",
  "card.style.height = `${card.offsetHeight}px`;",
];
const rules = [
  [/innerHTML\s*=.*\$\{/, "customer text may reach innerHTML"],
  [/offsetHeight|getBoundingClientRect/, "reads layout: is it mixed with writes?"],
  [/\*\s*1\.\d/, "money maths with decimals: use whole paise"],
];

diff.forEach((line, index) => {
  for (const [pattern, message] of rules) {
    if (pattern.test(line)) {
      console.log(`line ${index + 1}: ${message}`);
    }
  }
});
```

It prints:

```text
line 1: customer text may reach innerHTML
line 3: money maths with decimals: use whole paise
line 4: reads layout: is it mixed with writes?
```

Line 2 is flagged by nothing, but a human would ask: "Is this inside render?" Tools like ESLint catch patterns; only a person can judge whether the change makes sense.

1. Add a rule that flags `console.log(` left in a diff, and a changed line that has one.
2. Add the line `title.textContent = review.text;`. Should any rule flag it?

## Recap

- A code review catches problems before customers do, shares knowledge and keeps the code consistent. It judges the change, never the author.
- Keep pull requests small, one idea each, with a description of what, why and how to test.
- Read the tests and the code, then run the change, with the keyboard too. Check correctness, security, accessibility, performance, leaks, tests and readability.
- Write comments with a label (`blocking:`, `suggestion:`, `question:`, `nit:`), and say what, why and a suggestion. Praise what's good.
- Take feedback calmly: reply to every comment, ask when unsure, and review your own diff before anyone else does.

## Common mistakes

**1. "LGTM" on a huge pull request.**

```text
Files changed: 38   +1,412 −297   Review: "LGTM 👍"
```

Symptom: a bug in line 900 reaches customers, and nobody can say who checked what. Fix: ask for the PR to be split, then review each part properly, running it.

**2. Reviewing only by reading.**

```text
"Code looks fine." (preview link never opened)
```

Symptom: the new drawer looks right in the diff, but Tab never reaches its Close button. Fix: open the preview, and try the change with the mouse and with only the keyboard.

**3. Vague or personal comments.**

```text
"This is wrong."   "Why would you do it like this?"
```

Symptom: the author doesn't know what to change and feels attacked. Fix: what, why and a suggestion: "blocking: total uses `price * 1.1`, which gives fractions of paise. Could we use `Math.floor(price * 11 / 10)`?"

**4. Holding a PR for nits.**

```text
nit: I'd call it items, not lines.   (Request changes)
```

Symptom: a fix customers need waits two days over a name. Fix: label it `nit:`, approve, and let the author choose.

**5. Changing code silently after a review.**

```text
Comments: 6    Replies: 0    New commits: 3
```

Symptom: the reviewer must reread everything to find which comments were handled. Fix: reply to each one, "Done in 3f2a1c4" or why not, then ask for another look.
