# Security: XSS and safe rendering

A customer's gift note ran code on the packing-slip page, and a fake sign-in link is going round on WhatsApp. Every value in a note, and everything in the address bar, was typed by a stranger. Make all of it plain text. Change `src/giftNote.js`. A note is `{ to, message, from }`.

1. **TODO 1:** `escapeHtml(text)` replaces `&` with `&amp;` **first**, then `<` with `&lt;`, `>` with `&gt;`, `"` with `&quot;` and `'` with `&#39;`.
2. **TODO 2:** `packingSlipHtml(note)` builds an HTML string for the shop's printer. Pass `note.to`, `note.message` and `note.from` through `escapeHtml`, so a tag in any of them stays text.
3. **TODO 3:** `showGiftNote(card, note)` fills the card on the page. Set each paragraph's `textContent`, not its `innerHTML`: `For Asha & Ravi`, the message, `From Meera`. Don't escape here, or the customer sees `&amp;`.
4. **TODO 4:** `nextPage(search)` reads `next` from a search like `"?next=/cart"`, with `new URLSearchParams(search).get("next")`. Return it only when it is a path on this site: it starts with `/`, and doesn't start with `//` or `/\`. Otherwise, or when `next` is missing, return `"/"`.

The checks feed in notes with `<img onerror>`, `<script>` and `<b>` tags, and links with `javascript:`, `//` and `/\`.

In the preview, the note from yesterday's order is filled in. Before your fix, its image tag runs code and a red warning appears. After it, the card shows the angle brackets as text, and the slip shows `&lt;img` instead of `<img`.
