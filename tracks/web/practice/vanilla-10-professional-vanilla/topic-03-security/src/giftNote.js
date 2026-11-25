// Gift notes and the sign-in redirect. Every value in a note was typed by a customer, so it is text, never HTML.

/** Makes text safe to put inside an HTML string: & < > " and ' become entities. */
export function escapeHtml(text) {
  // TODO 1: replace & with &amp; FIRST, then < with &lt;, > with &gt;, " with &quot; and ' with &#39;
  return text;
}

/** The packing slip as an HTML string, for the shop's printer. note is { to, message, from }. */
export function packingSlipHtml(note) {
  // TODO 2: pass every customer value (note.to, note.message, note.from) through escapeHtml
  return (
    `<article class="slip">` +
    `<p class="to">For ${note.to}</p>` +
    `<p class="message">${note.message}</p>` +
    `<p class="from">From ${note.from}</p>` +
    `</article>`
  );
}

/** Shows a note on the gift card on the page. card holds empty .to, .message and .from paragraphs. */
export function showGiftNote(card, note) {
  // TODO 3: set each paragraph's textContent instead of its innerHTML
  card.querySelector(".to").innerHTML = `For ${note.to}`;
  card.querySelector(".message").innerHTML = note.message;
  card.querySelector(".from").innerHTML = `From ${note.from}`;
}

/** Where to go after signing in: the ?next= path, only when it is a path on this site. Otherwise "/". */
export function nextPage(search) {
  // TODO 4: return next only when it starts with "/" and doesn't start with "//" or "/\"; otherwise (or when missing) "/"
  return new URLSearchParams(search).get("next") ?? "/";
}
