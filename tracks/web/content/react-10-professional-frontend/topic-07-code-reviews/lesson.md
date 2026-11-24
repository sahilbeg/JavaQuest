## Hook

Before a big order of Diwali hampers goes out, a second person checks each box. Is the gift note right? Is the name stitched correctly? Is anything missing? The first person isn't careless. Two pairs of eyes just catch more than one.

Code works the same way. On a professional team, nothing reaches `main` until a teammate has read it. In [GitHub and pull requests](/t/web-07-tooling/topic-05-github-pull-requests) you learned the mechanics. This ticket is about the craft: what to look for in frontend code, how to say it kindly, and how to take comments on your own work without flinching.

## Concept

### Why teams review code

```callout jargon
A **code review** is a teammate reading your change before it's merged, and leaving comments. The person who wrote the change is the **author**; the person reading it is the **reviewer**.
```

Reviews pay for their time in four ways:

1. **Bugs are caught early.** A bug found in review costs a comment. Found by customers, it costs lost orders and a late night.
2. **Knowledge spreads.** At least two people understand every component. If the author is on holiday, the code isn't a mystery.
3. **The code stays consistent.** Problems get solved the same way, so the codebase reads like one team wrote it.
4. **People learn,** in both directions. Juniors learn from seniors' comments, and seniors pick up new tricks.

A review is not a stage to show off, a gate kept shut until the code matches your taste, or a verdict on the person. Approve once the change makes the code healthier, even if it isn't how you'd have written it.

### A pull request that is easy to review

Reviewing starts with the author. A good frontend PR is:

- **Small and about one thing.** Under about 400 changed lines. Never a big rename mixed with a behaviour change.
- **Green before review.** Tests, the linter and the build pass in CI before you ask anyone.
- **Described.** What changed, why, and how you tested it.
- **Shown, not just told.** For anything visible, add **before and after screenshots**, a phone-width one too, and the **preview deploy** link.

```text
Add "Your orders" list to the account page (WEB-1031)

What: the account page lists the customer's last 10 orders, newest first,
with an "Order again" button on each.
Why: Raju gets about 30 WhatsApp messages a week asking "what did I order last time?"
How tested: RTL tests for loading, empty, error and a list of 3 orders.
Tried on the preview at 360 px wide, and with only the keyboard.
Preview: https://pr-87--dhaaga.netlify.app/account
Screenshots: before / after / phone (attached)
Not in this PR: paging. That's WEB-1032.
```

Before you click "Ready for review", read your own diff on the PR page, top to bottom. Leftover `console.log` lines, commented-out code and stray files jump out there.

### The frontend review checklist

Spend review time where mistakes cost most. Open the **preview deploy first**, then read the diff in this order:

1. **Does it work?** Click through it on the preview, at phone width. Try the edges: empty lists, long names, slow loading, errors, zero stock.
2. **State and effects.** Are effect dependencies complete? Is a fetch cleaned up when the component goes away? Are list keys stable ids, not indexes? Is state kept in one place, or copied and drifting?
3. **Accessibility.** Real buttons and links, labels on inputs, alt text, focus moved for dialogs, nothing that needs a mouse. Try it with Tab.
4. **Security.** No `dangerouslySetInnerHTML` with user text, links from users checked, no secrets in `VITE_` variables.
5. **Tests.** Do they act like a user, and would they fail if the change were wrong?
6. **Performance.** New heavy packages, photos without sizes or lazy loading, work repeated on every render.
7. **Consistency.** Uses the team's shared components and design tokens, money as paise formatted the shared way.
8. **Readability.** Names, small components, comments that explain *why*.

What you should **not** spend time on is formatting: quotes, spaces, semicolons, import order. Prettier and ESLint, from [ESLint and Prettier](/t/web-07-tooling/topic-03-eslint-and-prettier), do that in CI. No human should ever write "missing semicolon".

### Keys, effects and other React traps

Some bugs are special to React and easy to miss in a diff. Here is the classic one: list keys that are indexes.

```animate
title: Gift notes kept by index, after removing Golu
kind: code
language: js
code: |
  const notesByIndex = ["For Aai", "(none)", "Happy birthday!"];
  const cart = ["Golu", "Coasters", "Tote bag"];
  cart.splice(0, 1);
  console.log(cart[0] + ": " + notesByIndex[0]);
  console.log(cart[1] + ": " + notesByIndex[1]);
steps:
  - line: 1
    vars: { notesByIndex: '["For Aai", "(none)", "Happy birthday!"]' }
    say: React keeps each row's state by its key. With key={index}, the notes are stored by position.
  - line: 2
    vars: { cart: '["Golu", "Coasters", "Tote bag"]' }
    say: Golu's note says "For Aai". The tote bag's says "Happy birthday!".
  - line: 3
    vars: { cart: '["Coasters", "Tote bag"]' }
    say: The customer removes Golu. Every item moves up one place, but the notes stay where they were.
  - line: 4
    out: "Coasters: For Aai"
    say: Now the coasters have Golu's note.
  - line: 5
    out: "Tote bag: (none)"
    say: And the tote bag lost its birthday note. With key={item.id}, each note would stay with its item.
```

Other React traps to look for:

- **Missing effect dependencies.** `useEffect(() => load(customerId), [])` never reloads when `customerId` changes.
- **No cleanup.** A fetch that can finish after the component is gone, or after the input changed, can show stale data. Use an `AbortController`.
- **State copied from props.** `useState(props.total)` keeps the first value forever, even when the prop changes.
- **New objects on every render** passed to memoised children, which defeats the memo.

### How to write a review comment

A good comment is **specific, kind and useful**. It points at the line, says what's wrong, why it matters, and offers a way forward.

| Unhelpful | Helpful |
|---|---|
| "This is wrong." | "This shows ₹149.9 for 14990 paise. Could we use `formatRupees`, so it says ₹149.90?" |
| "Why would you do this??" | "What made you pick `key={index}`? Orders have ids, and with ids a removed row can't pass its state to another." |
| "No tests again." | "Could we add a test for an empty order list? I think it shows a blank box today." |

Five habits:

- **Talk about the code, not the person.** "This effect", not "you forgot".
- **Explain why.** A rule with a reason teaches; a rule alone feels like an order.
- **Suggest a fix.** A small code suggestion is often the clearest comment.
- **Ask when unsure.** "Can this run before the orders arrive?" invites an answer, not a defence.
- **Praise what's good.** "Nice: the loading state is announced with role=status."

### Label the severity: Conventional Comments

When 14 comments arrive, the author needs to know which ones block the merge. **Conventional Comments** puts a label at the start of each one:

```text
issue (blocking): the gift note is typed by customers, and dangerouslySetInnerHTML turns it into HTML. Please render {order.giftNote}.
suggestion: an AbortController in the effect would stop a slow response overwriting a newer one.
question: should "Order again" be disabled for items that are now out of stock?
nitpick (non-blocking): `res` could be `response`.
praise: the before/after screenshots at phone width made this quick to review.
```

```callout jargon
A **nit**, short for nitpick, is a tiny point that shouldn't block the merge. A **blocking** comment must be fixed or settled before the change can merge.
```

### Approve, comment or request changes

When you finish, GitHub asks for one of three:

- **Approve**: nothing blocking is left. You can still leave nits: "fix them if you agree, no need for another round".
- **Comment**: feedback with no verdict, for questions or an early look at a draft.
- **Request changes**: a real problem, such as a bug, a security hole, a broken keyboard path or missing tests for the main behaviour. Never for taste.

Answer review requests within a working day, often within a few hours. A PR that waits on you is work that's finished but helps nobody, and it grows merge conflicts while it waits.

### Receiving feedback

Now your own PR, with 14 comments. That's normal, especially in a new codebase.

- **Assume good intent.** Short comments are usually busy, not rude.
- **It's about the code, not you.** Senior engineers get comments too.
- **Reply to every comment.** "Done", a link to the fix, or a reason. Silence leaves the reviewer guessing.
- **Disagree with reasons.** "I kept the `<select>` because the design system's dropdown has no keyboard support yet. Ticket WEB-1040."
- **Move long threads to a call.** After about three rounds, ten minutes of talking beats twenty more messages. Write the outcome in the PR.
- **Say thank you.** "Good catch, thanks" costs nothing and builds trust.

```callout tip
When a comment stings, wait an hour before you answer, then read it again as if a friend wrote it. It usually reads kinder the second time.
```

## Worked example

Here's the "Your orders" PR waiting for you. The description is the one above. CI is green, and the preview works for the author's test account.

```diff
+export function OrdersList({ customerId }) {
+  const [orders, setOrders] = useState([]);
+
+  useEffect(() => {
+    fetch(`/api/customers/${customerId}/orders`)
+      .then((res) => res.json())
+      .then(setOrders);
+  }, []);
+
+  return (
+    <ul>
+      {orders.map((order, index) => (
+        <li key={index}>
+          <span>{order.id}</span>
+          <span>₹{order.totalPaise / 100}</span>
+          <div className="reorder" onClick={() => onReorder(order.id)}>Order again</div>
+          <p dangerouslySetInnerHTML={{ __html: order.giftNote }} />
+        </li>
+      ))}
+    </ul>
+  );
+}
```

And the test:

```diff
+it("renders", () => {
+  render(<OrdersList customerId="cust-7" />);
+});
```

Before you read on, open the preview in your head and walk the checklist. What would you comment?

### The review

Seven comments, in checklist order:

```text
issue (blocking), on the gift note line: customers type gift notes, so this turns their text into
HTML. A note with <img onerror=...> would run code on every account page that shows it.
Please render {order.giftNote}; a CSS white-space: pre-line keeps their line breaks.

issue (blocking), on the effect: the dependency list is empty, so switching customer (admins can)
keeps showing the first customer's orders. Please add customerId, and an AbortController
cleanup so an old response can't overwrite a newer one.

issue (blocking), on "Order again": a div can't be reached with Tab or pressed with Enter.
Could it be a <button type="button">, with aria-label={`Order ${order.id} again`}?

issue (blocking), on the test: it has no expect, so it passes even if nothing renders. Could we stub
fetch and check the list, plus the empty and error states?

suggestion, on the price: 14990 paise shows as ₹149.9 and 129900 as ₹1299. Our formatRupees gives
₹149.90 and ₹1,299.00, like the rest of the store.

suggestion, on key={index}: orders have ids, so key={order.id} keeps each row's state with its order.

praise: the PR description, with the WhatsApp numbers and the phone-width check, is great.
```

The verdict is **Request changes**. Four comments are real problems: a security hole, wrong data, a broken keyboard path and a test that can't fail. None of them is about taste, and there's no comment about quotes or spacing. Prettier handles those.

## Try it

This shows the price line from the PR next to the store's shared formatter. Run it.

```js run
const shown = (paise) => "₹" + paise / 100;
const formatRupees = (paise) =>
  "₹" + (paise / 100).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

for (const paise of [45000, 14990, 129900]) {
  console.log(shown(paise) + " vs " + formatRupees(paise));
}
```

It prints:

```text
₹450 vs ₹450.00
₹149.9 vs ₹149.90
₹1299 vs ₹1,299.00
```

1. Add `5` (five paise) to the list. What does each version show?
2. Write the review comment for the price line yourself, with a Conventional Comments label.

## Recap

- Reviews catch bugs early, spread knowledge, keep code consistent and teach both sides. They aren't a stage, a closed gate or a verdict on a person.
- Make PRs easy to review: small, one purpose, green CI, a description with what, why and how tested, plus screenshots and a preview link for anything visible.
- Open the preview first, then review in order of cost: it works, state and effects, accessibility, security, tests, performance, consistency, readability. Leave formatting to Prettier and ESLint.
- Write specific, kind comments about the code, explain why, suggest a fix, and label each one with Conventional Comments.
- Approve when nothing blocks, request changes only for real problems, answer within a day, and take feedback with good intent, a reply to every comment, and reasons when you disagree.

## Common mistakes

**1. Reviewing the diff without opening the preview.**

```text
LGTM!  (never opened pr-87--dhaaga.netlify.app)
```

Symptom: the list looks fine in code, but on a phone the "Order again" text overflows, and it can't be reached with Tab. Customers find it first. Fix: open the preview at phone width and with the keyboard before reading the diff.

**2. Nitpicking while a bug walks past.**

```text
nitpick: use double quotes
nitpick: add a blank line
LGTM!  (the gift note is rendered as HTML)
```

Symptom: the PR merges with tidy quotes and an XSS hole. Fix: walk the checklist from the top, and leave formatting to the tools.

**3. A comment with no why and no way forward.**

```text
Don't use index as key.
```

Symptom: the author doesn't see the problem, argues, and the thread runs for two days. Fix: say what goes wrong and what to do: "Removing an order moves its row's state to the next one. Could we use key={order.id}?"

**4. Approving because CI is green.**

```jsx
it("renders", () => { render(<OrdersList customerId="cust-7" />); });
```

Symptom: the test passes even if the component shows nothing at all. Fix: ask "would this test fail if the code were wrong?" If not, ask for assertions on what the customer sees.

**5. Treating your own review as a fight.**

```text
"It works on my machine. Not changing it."
```

Symptom: the thread grows to 20 replies, trust drops, and the PR waits a week. Fix: assume good intent, answer with reasons or a fix, and move to a short call after about three rounds.
