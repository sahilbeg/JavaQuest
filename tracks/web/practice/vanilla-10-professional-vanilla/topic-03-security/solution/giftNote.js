// Gift notes and the sign-in redirect. Every value in a note was typed by a customer, so it is text, never HTML.

/** Makes text safe to put inside an HTML string: & < > " and ' become entities. */
export function escapeHtml(text) {
  return text
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

/** The packing slip as an HTML string, for the shop's printer. note is { to, message, from }. */
export function packingSlipHtml(note) {
  return (
    `<article class="slip">` +
    `<p class="to">For ${escapeHtml(note.to)}</p>` +
    `<p class="message">${escapeHtml(note.message)}</p>` +
    `<p class="from">From ${escapeHtml(note.from)}</p>` +
    `</article>`
  );
}

/** Shows a note on the gift card on the page. card holds empty .to, .message and .from paragraphs. */
export function showGiftNote(card, note) {
  card.querySelector(".to").textContent = `For ${note.to}`;
  card.querySelector(".message").textContent = note.message;
  card.querySelector(".from").textContent = `From ${note.from}`;
}

/** Where to go after signing in: the ?next= path, only when it is a path on this site. Otherwise "/". */
export function nextPage(search) {
  const next = new URLSearchParams(search).get("next");
  if (next === null || !next.startsWith("/") || next.startsWith("//") || next.startsWith("/\\")) {
    return "/";
  }
  return next;
}
