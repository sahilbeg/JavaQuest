# Security: XSS and safe rendering

Customers can now write reviews on dhaaga.in. One "review" is an attack: it hides an `<img onerror=...>` in its text and a `javascript:` link as its website. Make the review list safe. Change `src/safeUrl.js` and `src/ReviewList.jsx`.

Each review is `{ id, author, rating, text, website }`. Every field except `id` was typed by a stranger. `website` may be missing.

1. **TODO 1:** `safeUrl(url)` returns `url` unchanged when it is a full address whose protocol is `https:` or `http:`. For anything else it returns `null`: `javascript:` (in any capitals, even with spaces in front), `data:`, `file:`, an address with no `https://`, an empty string, or `undefined`. Parse with `new URL(url)` inside `try`/`catch`: it throws for things that aren't full addresses.
2. **TODO 2:** show the review's words as plain text, with `{review.text}`. A review that says `<img src="x" onerror="alert(1)">` must show those characters, not make an image.
3. **TODO 3:** show the author's name as plain text too. Link it only when `safeUrl(review.website)` gives an address. The link gets `target="_blank"` and `rel="nofollow noopener noreferrer"`. Without a safe address, there is no `<a>` at all, just the name.

`ReviewList.jsx` must not contain `dangerouslySetInnerHTML` anywhere, not even in a comment: one check reads the file.

The preview shows three reviews. Before your fix, the second one runs code on the page and adds a message. After your fix, it shows its angle brackets as plain text.
