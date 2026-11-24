## Hook

Imagine you can't use a mouse. Maybe your hand shakes, or you can't see the screen, or you broke your wrist last week. You press Tab to move from one thing to the next, and Enter to press it. A voice reads out where you are.

Now you land on a "Delivery and returns" title that only opens on a mouse click. The voice says nothing about it. Tab jumps straight past it. For you, that answer doesn't exist.

In [Accessibility: pages everyone can use](/t/web-01-html/topic-09-accessibility) you gave pages alt text, labels and headings. This ticket goes further: things that open, close and pop up, and where the keyboard's focus goes when they do.

## Concept

### Who uses the keyboard

Many people never touch a mouse:

- **Screen reader users.** A screen reader is a program that reads the page out loud, such as NVDA, VoiceOver or TalkBack. Its users move with the keyboard, or with swipes on a phone.
- **People with motor difficulties**, who use a keyboard, a switch or voice control.
- **Power users**, who are simply faster with Tab and Enter.

```callout jargon
**Focus** is the one element that keyboard keys go to right now. Tab moves focus to the next element that can take it. **Assistive technology** is any tool that helps a disabled person use a computer, such as a screen reader or a switch.
```

So every action on dhaaga.in must work with the keyboard alone, and every control must have a role and a name a screen reader can say.

### Native elements do the work

A real `<button>` gives you a lot for free:

- it can take focus, so Tab stops on it;
- Enter and Space press it;
- a screen reader says "button" and its words;
- `disabled` really disables it.

A `<div onClick>` gives you none of that. It looks like a button to a mouse user, but to the keyboard it's just text.

```jsx
<div className="add" onClick={add}>Add to cart</div>        // mouse only
<button type="button" onClick={add}>Add to cart</button>   // everyone
```

The same goes for links (`<a href>`), form fields, `<select>` and checkboxes. Reach for the real element first, every time.

### Syntax: tabIndex

```jsx
<h2 tabIndex={-1}>Pay by UPI</h2>
```

`tabIndex` changes whether an element can take focus:

| Value | What it does |
|---|---|
| `0` | Tab stops here, in page order. For custom widgets only. |
| `-1` | Tab skips it, but code can focus it with `.focus()`. Good for headings you jump to. |
| `1` or more | Tab visits it before everything else. Avoid: it scrambles the order. |

Buttons, links and form fields already have the right behaviour, so they don't need `tabIndex` at all.

### Focus you can see

Keyboard users need to see where focus is. Browsers draw a focus ring, and the worst thing you can do is remove it.

```css
button:focus { outline: none; }                                     /* never do this */
button:focus-visible { outline: 3px solid #F2A81D; outline-offset: 2px; }
```

`:focus-visible` shows the ring when the keyboard moved focus, and usually not after a mouse click. So designers get a clean look for mouse users, and keyboard users still see where they are.

### Syntax: ARIA attributes

```jsx
<button aria-expanded={open} aria-controls="faq-delivery">Delivery and returns</button>
```

```callout jargon
**ARIA** (Accessible Rich Internet Applications) is a set of HTML attributes that tell assistive technology what an element is and what state it's in. They change only what a screen reader announces. They never change how the element looks or behaves.
```

The ones you'll use most:

| Attribute | Says |
|---|---|
| `role="dialog"` | what the element is, when no HTML element fits |
| `aria-expanded="true"` | this button's panel is open |
| `aria-controls="id"` | this button controls the element with that id |
| `aria-pressed="true"` | this toggle button is switched on |
| `aria-modal="true"` | this dialog blocks the rest of the page |
| `aria-label="..."` | the element's name, when its visible text isn't enough |
| `aria-labelledby="id"` | the element's name is the text of another element |
| `aria-live="polite"` | read out changes to this area when the user is idle |

In React, you write `aria-*` attributes exactly as in HTML, with the dash. A `true` or `false` value becomes the text `"true"` or `"false"`.

### The first rule of ARIA

The first rule of ARIA is: **don't use ARIA if a native element does the job.** ARIA only adds words. It adds no behaviour.

```jsx
<div role="button" onClick={add}>Add to cart</div>
```

The screen reader now says "Add to cart, button". But Tab still skips it, and Enter still does nothing. To finish the job you'd need `tabIndex={0}` and a key handler for Enter and Space. A `<button>` already has all of that.

```callout tip
No ARIA is better than wrong ARIA. A wrong role or a stale aria-expanded tells a blind customer something false, and they have no way to check.
```

### Accessible names: aria-label and aria-labelledby

Picture a cart with three lines, each with a **Remove** button. A screen reader user hears "Remove, button. Remove, button. Remove, button." Which one removes Golu?

Give each button a name that says it all:

```jsx
<button type="button" aria-label={`Remove ${item.name}`}>Remove</button>
```

Sighted users still see **Remove**, next to the item's name. A screen reader says "Remove Golu the elephant, button".

`aria-labelledby` points at other text on the page instead. A dialog usually takes its name from its own heading:

```jsx
<div role="dialog" aria-labelledby="remove-title">
  <h2 id="remove-title">Remove Golu the elephant?</h2>
```

### The disclosure pattern

```callout jargon
A **disclosure** is a button that shows and hides a section, like a "Delivery and returns" answer on a help page. A list of them is often called an accordion.
```

The recipe, from the WAI-ARIA Authoring Practices, the W3C's guide to common widgets:

1. The title is a `<button>`, usually inside a heading so it shows up in the page outline.
2. The button has `aria-expanded`, `"true"` or `"false"`.
3. The button has `aria-controls` with the panel's id.
4. The panel is hidden with the `hidden` attribute when closed.

The screen reader says "Delivery and returns, button, collapsed". After Enter, it says "expanded". Nobody has to guess.

In React, make the panel's id with `useId()`. It's unique on the page, even when the component appears ten times.

### Dialogs: focus in, focus out

```callout jargon
A **modal dialog** is a box that sits on top of the page and must be answered before you go back, like "Remove Golu the elephant?". **Modal** means the rest of the page is blocked while it's open.
```

A mouse user sees the dialog pop up and clicks in it. A keyboard user can't see where it opened, so you move their focus for them.

```animate
title: Focus during a Remove dialog
kind: scene
nodes:
  - { id: remove, label: Remove button, emoji: "🗑️", value: focused, row: 0, col: 0 }
  - { id: dialog, label: Dialog, emoji: "💬", value: closed, row: 0, col: 3 }
  - { id: cancel, label: Cancel, emoji: "↩️", value: "–", row: 2, col: 2 }
  - { id: yes, label: "Yes, remove", emoji: "✅", value: "–", row: 2, col: 4 }
steps:
  - say: Mrs. Bhosale tabs to Remove and presses Enter. Focus is on the Remove button.
    focus: [remove]
  - say: The dialog opens. Focus moves straight into it, to Cancel, the safe choice.
    send: { from: remove, to: cancel, label: "focus()" }
    set: { dialog: open, remove: "–", cancel: focused }
  - say: Her screen reader reads the dialog's name, its heading, "Remove Golu the elephant?".
    focus: [dialog]
  - say: Tab moves between Cancel and Yes, remove. It stays inside the dialog while it is open.
    set: { cancel: "–", yes: focused }
  - say: She changes her mind and presses Escape. The dialog closes.
    set: { dialog: closed, yes: "–" }
  - say: Focus goes back to the Remove button that opened it. She is exactly where she started.
    send: { from: dialog, to: remove, label: "focus()", back: true }
    set: { remove: focused }
```

The rules for a modal dialog:

- `role="dialog"`, `aria-modal="true"`, and a name with `aria-labelledby`.
- **Focus in:** when it opens, focus moves inside, usually to the safest button.
- **Focus stays:** Tab cycles inside the dialog while it's open. This is called a **focus trap**.
- **Escape closes it.**
- **Focus out:** when it closes, focus returns to the element that opened it.

Without the last rule, focus falls back to the top of the page. The customer has to Tab through the whole header to find their cart again.

```callout tip
The HTML <dialog> element, opened with showModal(), does the focus trap and Escape for you in modern browsers. Many teams use it, or a tested library, rather than building a dialog by hand. Knowing the rules lets you check either one.
```

### Syntax: moving focus with a ref

```jsx
const cancelRef = useRef(null);
useEffect(() => {
  if (open) cancelRef.current.focus();
}, [open]);
```

You met refs in [Refs: focus and measuring](/t/react-09-react-in-depth/topic-08-refs). The Cancel button only exists after the dialog renders. So you focus it in an effect, which runs after React has put the dialog on the page.

To send focus back, keep a ref to the opener too, and call `openerRef.current.focus()` when you close.

### Announcing changes: live regions

Some changes happen away from focus. A customer presses **Add to cart**, and the cart count in the header changes from 2 to 3. A sighted customer sees it. A screen reader user hears nothing.

```jsx
<p role="status">Golu the elephant added. 3 items in your cart.</p>
```

```callout jargon
A **live region** is an area whose changes a screen reader reads out without moving focus. `role="status"` (the same as `aria-live="polite"`) waits until the user pauses. `role="alert"` interrupts at once, so keep it for errors.
```

Put the live region on the page from the start, and change its text. A region that appears together with its text is often not announced.

### Testing it

Your tests from [Testing components like a user](/t/react-10-professional-frontend/topic-01-testing-components) can check all of this:

- `getByRole("button", { name: "Remove Golu the elephant" })` proves the role and the name.
- `button.getAttribute("aria-expanded")` reads a state.
- `await user.tab()` and `await user.keyboard("{Escape}")` press keys.
- `document.activeElement` is the element that has focus now.

Automated checks find maybe half of all problems. Before launch, also try the page yourself: unplug the mouse, and turn on your phone's screen reader for five minutes.

## Worked example

Dhaaga offers gift wrap for ₹30. The checkout has a toggle for it, and a sentence that confirms the price. Here it is, done right.

```jsx
import { useState } from "react";

export function GiftWrapToggle({ onChange }) {
  const [on, setOn] = useState(false);

  function toggle() {
    setOn(!on);
    onChange(!on);
  }

  return (
    <div>
      <button type="button" aria-pressed={on} onClick={toggle}>
        Gift wrap (₹30)
      </button>
      <p role="status">{on ? "Gift wrap added: ₹30.00" : ""}</p>
    </div>
  );
}
```

Line by line:

- A real `<button>`, so Tab, Enter and Space all work with no extra code.
- `aria-pressed={on}` makes it a **toggle button**. A screen reader says "Gift wrap (₹30), toggle button, not pressed", and then "pressed".
- The `role="status"` paragraph is always on the page. Only its text changes, so the screen reader reads "Gift wrap added: ₹30.00" without moving focus.

And a test for it, using only what a customer can see and do:

```jsx
it("can be switched on with the keyboard", async () => {
  const user = userEvent.setup();
  render(<GiftWrapToggle onChange={vi.fn()} />);
  const toggle = screen.getByRole("button", { name: "Gift wrap (₹30)" });
  await user.tab();
  await user.keyboard("{Enter}");
  expect(toggle.getAttribute("aria-pressed"), "Enter should switch gift wrap on").toBe("true");
  expect(screen.getByRole("status").textContent, "the price should be announced").toBe("Gift wrap added: ₹30.00");
});
```

## Try it

Tab visits elements in a fixed order. This program works out that order for a small page, the same way a browser does.

```js run
const page = [
  { name: "Logo link", tabIndex: 0 },
  { name: "Search box", tabIndex: 0 },
  { name: "Offer banner", tabIndex: 3 },
  { name: "Golu card (a div)", tabIndex: -1 },
  { name: "Add to cart", tabIndex: 0 },
  { name: "Cart", tabIndex: 1 },
];

const positive = page.filter((el) => el.tabIndex > 0).sort((a, b) => a.tabIndex - b.tabIndex);
const natural = page.filter((el) => el.tabIndex === 0);
console.log([...positive, ...natural].map((el) => el.name).join(" -> "));
console.log("Skipped: " + page.filter((el) => el.tabIndex < 0).map((el) => el.name).join(", "));
```

It prints:

```text
Cart -> Offer banner -> Logo link -> Search box -> Add to cart
Skipped: Golu card (a div)
```

Positive `tabIndex` values jump the queue, so the cart comes before the logo.

1. Set every positive `tabIndex` to `0`. Is the order now the same as the page order?
2. Why is it a problem that "Golu card (a div)" is skipped, if it has a click handler?

## Recap

- Every action must work with the keyboard alone, and every control needs a role and a name a screen reader can say.
- Use native elements first: a `<button>` gets focus, Enter, Space and its role for free. ARIA only changes what is announced, never how things behave.
- Name things fully: `aria-label="Remove Golu the elephant"`, or `aria-labelledby` pointing at a heading.
- A disclosure is a `<button>` with `aria-expanded` and `aria-controls`, and a panel hidden with `hidden`.
- A modal dialog has `role="dialog"`, `aria-modal` and a name. Focus moves in when it opens, Escape closes it, and focus returns to the opener.

## Common mistakes

**1. A clickable div.**

```jsx
<div className="disclosure-title" onClick={() => setOpen(!open)}>Delivery and returns</div>
```

Symptom: Tab skips it, Enter does nothing, and a screen reader reads it as plain text. The check says the title should be a button. Fix: `<button type="button" onClick={...}>`.

**2. Adding a role instead of using the element.**

```jsx
<div role="button" onClick={add}>Add to cart</div>
```

Symptom: the screen reader now says "button", but Tab still skips it and Enter still does nothing. Fix: use `<button>`. You would otherwise need `tabIndex={0}` and your own key handler.

**3. aria-controls pointing at nothing.**

```jsx
{open && <div id={panelId}>{children}</div>}
```

Symptom: while closed, `aria-controls` points at an id that isn't on the page. The check says nothing has that id. Fix: always render the panel, and hide it with `hidden={!open}`.

**4. Opening a dialog without moving focus.**

```jsx
{open && <div role="dialog" aria-modal="true" aria-labelledby={titleId}>...</div>}
```

Symptom: focus stays on the Remove button behind the dialog. A keyboard user tabs around the hidden page, and Escape does nothing, because the dialog never hears the key. Fix: focus the Cancel button in an effect when `open` becomes true.

**5. Forgetting to send focus back.**

```jsx
function close() {
  setOpen(false);
}
```

Symptom: when the dialog closes, focus falls back to the top of the page, and the customer has to Tab through the whole header again. Fix: keep a ref to the opener and call `openerRef.current.focus()` in `close()`.
