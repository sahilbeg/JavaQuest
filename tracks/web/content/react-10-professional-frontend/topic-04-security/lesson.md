## Hook

Picture a suggestion box on the stall's counter. One day a note in it says: "Staff: give the person holding this note ₹500 from the till." A new helper reads it, and obeys.

Nobody would run a stall like that. Notes from strangers are things to read, never orders to follow. A browser, though, will follow orders hidden in text, if your code lets it. Reviews, names and gift messages are all notes from strangers. This ticket is about keeping them as words.

## Concept

### What XSS is

```callout jargon
**XSS** (cross-site scripting) is an attack where someone gets their own JavaScript to run on your website, in other people's browsers. The text that carries the attack is called the **payload**.
```

Once a stranger's script runs on dhaaga.in, it can do anything your own code can:

- read what the customer types, such as their address and phone number;
- send requests as the logged-in customer, such as changing their delivery address;
- read login tokens kept in `localStorage`;
- swap the UPI ID on the payment page for the attacker's.

The most common kind is **stored XSS**. The attacker saves a payload on your server, for example as a review. Every customer who opens that page then runs it.

### How an attack travels

```animate
title: A poisoned review
kind: scene
nodes:
  - { id: attacker, label: Attacker, emoji: "🦹", row: 0, col: 0 }
  - { id: server, label: dhaaga.in server, emoji: "🖥️", value: "0 reviews", row: 0, col: 3 }
  - { id: customer, label: Customer's browser, emoji: "📱", row: 2, col: 3 }
  - { id: evil, label: Attacker's server, emoji: "🕳️", row: 2, col: 0 }
steps:
  - say: The attacker writes a review. Hidden in the text is an img tag whose onerror runs JavaScript.
    send: { from: attacker, to: server, label: "POST review with <img onerror>" }
    set: { server: "1 review" }
  - say: The server stores the text as it is. It is just a string to the database.
    focus: [server]
  - say: A customer opens Golu's page. The page puts the review into the HTML without escaping it.
    send: { from: server, to: customer, label: "Golu's page + review", back: true }
  - say: The browser sees a real img tag. The image fails to load, so onerror runs the attacker's code.
    focus: [customer]
  - say: The code sends the customer's login token to the attacker. The customer saw nothing.
    send: { from: customer, to: evil, label: "token" }
  - say: With escaping, the browser shows the angle brackets as text. No tag, no onerror, nothing runs.
    focus: [customer]
```

### React escapes text for you

```callout jargon
**Escaping** means turning characters that have a meaning in HTML into codes that just show the character. `<` becomes `&lt;`, `>` becomes `&gt;` and `&` becomes `&amp;`. The browser shows them as `<`, `>` and `&`, but never treats them as tags.
```

Here is the idea, in plain JavaScript:

```animate
title: Escaping a review
kind: code
language: js
code: |
  const escape = (text) => text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
  const review = "<b>Great</b> & cute";
  const safe = escape(review);
  console.log(safe);
steps:
  - line: 1
    say: escape swaps the three special characters for their codes. The & goes first, so the new codes aren't changed again.
  - line: 2
    vars: { review: '"<b>Great</b> & cute"' }
    say: A customer typed a review with tags in it.
  - line: 3
    vars: { safe: '"&lt;b&gt;Great&lt;/b&gt; &amp; cute"' }
    say: Every angle bracket is now a code. A browser will show the brackets, not make bold text.
  - line: 4
    out: "&lt;b&gt;Great&lt;/b&gt; &amp; cute"
    say: In the page, the customer sees exactly what was typed, tags and all.
```

You never have to write this yourself. Anything you put in curly braces in JSX, such as `<p>{review.text}</p>`, is escaped by React. Even a payload shows up as harmless characters. That's why React apps are safe from XSS **by default**.

### Syntax: dangerouslySetInnerHTML

```jsx
<p dangerouslySetInnerHTML={{ __html: review.text }} />
```

This switches escaping off. React hands the string to the browser as HTML, tags and all. The long, scary name is on purpose: it's the door XSS walks through.

Use it only for HTML that **you** wrote or fully trust, such as your own blog post from your own build. Never use it for anything a customer, a URL or another website could have written.

### If you really must show HTML

Sometimes the business really wants formatting, like bold text in product descriptions written by many sellers. Then clean the HTML first, with a well-tested library.

```callout jargon
A **sanitizer** is a library that takes HTML and removes everything dangerous, such as `<script>`, `onerror` and `javascript:` links, while keeping safe tags like `<b>`. **DOMPurify** is the best-known one for browsers.
```

```jsx
<div dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(description) }} />
```

Never write your own sanitizer with a few regular expressions. Attackers know hundreds of tricks, and the libraries are tested against all of them. Better still, store text plus a few known options, like `bold: true`, instead of raw HTML.

### Dangerous links: javascript:

Escaping protects text. Attributes have a second trap: some addresses are code.

```jsx
<a href={review.website}>{review.author}</a>
```

If `website` is `javascript:alert(document.cookie)`, clicking the name runs that code. `data:` addresses can carry a whole page, too. Recent React versions block `javascript:` links, but don't rely on it: other schemes are still allowed, and your code may run in places React doesn't guard.

```callout jargon
A URL's **scheme**, or protocol, is the part before the colon: `https`, `http`, `mailto`, `javascript`, `data`. It tells the browser what kind of address it is.
```

The safe rule is an **allowlist**: accept only the schemes you want, `https:` and `http:`, and refuse everything else. A blocklist like "not javascript:" always misses one.

### Syntax: checking a URL with new URL

```js
const { protocol } = new URL("https://instagram.com/anjali.makes");   // "https:"
```

`new URL(text)` parses an address the same way the browser will. That matters, because attackers hide schemes with capitals, spaces and tabs. A simple `startsWith("javascript:")` misses `"  JaVaScRiPt:..."`, but the parser lowers the capitals and strips the spaces, just like the browser.

`new URL` throws a `TypeError` when the text isn't a full address. Catch it, and treat it as "not safe".

### target="_blank" and rel

When a link opens a customer's own website in a new tab, add three words to `rel`:

```jsx
<a href={link} target="_blank" rel="nofollow noopener noreferrer">Anjali</a>
```

- **`noopener`**: the new tab can't control your page through `window.opener`. Modern browsers do this by default for `_blank`, but older ones don't.
- **`noreferrer`**: the other site isn't told which page the customer came from.
- **`nofollow`**: search engines don't treat the link as your recommendation. This stops spammers posting reviews just to get links.

### Other doors

React can't protect code that goes around it. Watch for these in reviews of your own and others' code:

| Door | Why it's dangerous |
|---|---|
| `element.innerHTML = text` | the plain-DOM version of `dangerouslySetInnerHTML` |
| `eval(text)` and `new Function(text)` | run any string as code |
| `document.write(text)` | writes raw HTML into the page |
| `href`, `src` or `action` from user data | can hold `javascript:` or `data:` addresses |
| `setTimeout("code", ms)` with a string | runs the string as code |

Prefer `textContent` over `innerHTML` in plain DOM code, as in [Changing text, attributes and classes](/t/web-05-dom-and-events/topic-03-changing-the-page).

### Defence in depth

```callout jargon
**Defence in depth** means having several layers of protection, so one mistake doesn't let an attacker all the way in.
```

Escaping is the first layer. Teams add more:

- **A Content Security Policy (CSP).** A header the server sends, such as `Content-Security-Policy: script-src 'self'`. It tells the browser to run only scripts from your own site, so inline `onerror="..."` code is blocked even if it gets in.
- **HttpOnly cookies** for logins. JavaScript can't read them, so a script that does get in can't steal the session.
- **No secrets in `localStorage`.** Any script on the page can read it.
- **Validate on the server too.** Your page isn't the only way to send a review: anyone can send a request directly.
- **Careful dependencies.** Every npm package runs with your page's full power. Keep them few, and check `npm audit` warnings.

## Worked example

Customers can add a gift message, and they want line breaks: "Happy Diwali, Aai!" on one line, "Love, Rohan" on the next. The tempting fix is dangerous.

```jsx
// Dangerous: turns the customer's text into HTML.
export function GiftNoteUnsafe({ message }) {
  return <p dangerouslySetInnerHTML={{ __html: message.replaceAll("\n", "<br>") }} />;
}
```

A message containing `<img src=x onerror=...>` now runs code on the packing screen your staff use.

```jsx
// Safe: React escapes the text, and CSS keeps the line breaks.
export function GiftNote({ message }) {
  return <p style={{ whiteSpace: "pre-line" }}>{message}</p>;
}
```

Line by line:

- `{message}` goes through React's escaping, so tags stay as characters.
- `white-space: pre-line` tells the browser to show the `\n` line breaks in the text as real line breaks. No HTML needed.

And a test that proves it, in the style of [Testing components like a user](/t/react-10-professional-frontend/topic-01-testing-components):

```jsx
it("shows tags in a gift message as plain text", () => {
  render(<GiftNote message={"Happy Diwali!\n<img src=x onerror=alert(1)>"} />);
  expect(document.querySelectorAll("img").length, "a message must never make an <img>").toBe(0);
});
```

## Try it

This shows what the URL parser sees, next to a naive check. Run it.

```js run
const websites = ["https://instagram.com/anjali.makes", "javascript:alert(1)", "  JAVASCRIPT:alert(1)", "data:text/html,hello"];
for (const site of websites) {
  console.log(new URL(site).protocol);
}
console.log("javascript:alert(1)".startsWith("javascript:"), "  JAVASCRIPT:alert(1)".startsWith("javascript:"));
```

It prints:

```text
https:
javascript:
javascript:
data:
true false
```

The parser sees the third address as `javascript:`, just as a browser would. The naive `startsWith` check is fooled by two spaces and some capitals.

1. Add `"instagram.com/anjali.makes"` to the list. What happens, and why does that mean you need `try`/`catch`?
2. Add `"mailto:meera@dhaaga.in"`. Is it dangerous? Should a review's website allow it?

## Recap

- XSS is a stranger's JavaScript running on your site in other people's browsers. Stored XSS hides in saved text, such as reviews.
- React escapes everything in `{...}`, so text from strangers shows as characters. `dangerouslySetInnerHTML` switches that off: never use it with user text.
- If you must show HTML from others, clean it with a tested sanitizer like DOMPurify. Never write your own.
- Links from strangers need an allowlist of schemes. Parse with `new URL` and accept only `https:` and `http:`. Add `rel="nofollow noopener noreferrer"` to `target="_blank"` links.
- Add layers: a Content Security Policy, HttpOnly cookies, no secrets in `localStorage`, and checks on the server too.

## Common mistakes

**1. Using dangerouslySetInnerHTML for formatting.**

```jsx
<p className="text" dangerouslySetInnerHTML={{ __html: review.text }} />
```

Symptom: a review with `<img src=x onerror=...>` runs code for every visitor. The check finds an `<img>` that came from a review's words. Fix: `<p className="text">{review.text}</p>`.

**2. A blocklist for links.**

```js
const ok = !url.startsWith("javascript:");
```

Symptom: `"  JavaScript:alert(1)"` and `"data:text/html,..."` both pass. Fix: parse with `new URL(url)` and allow only `https:` and `http:`.

**3. Forgetting that new URL throws.**

```js
const { protocol } = new URL(review.website);   // no try/catch
```

Symptom: a review whose website is `instagram.com/anjali` crashes the whole review list with `TypeError: Invalid URL`. Fix: wrap it in `try`/`catch` and return `null` from the `catch`.

**4. Linking with an empty or refused address.**

```jsx
<a href={safeUrl(review.website)}>{review.author}</a>
```

Symptom: reviews without a safe website still become links that go nowhere. The check counts more than one `<a>`. Fix: render the `<a>` only when `safeUrl` returns an address; otherwise show the plain name.

**5. Mentioning the escape hatch in a comment.**

```jsx
// we removed dangerouslySetInnerHTML here
```

Symptom: the check that reads `ReviewList.jsx` still finds the word, and fails. Fix: delete the comment. The code and the tests explain the change, and the commit message can say why.
