// Byte, the team's owl, as a small monochrome mark. It draws in currentColor, so it takes the colour of
// whatever holds it: the logo, the helper button, chat messages and walkthrough bubbles.

const MARK = `<path d="M5 4.2 8.1 6.4A7.4 7.4 0 0 1 12 5.3a7.4 7.4 0 0 1 3.9 1.1L19 4.2v8.3A7 7 0 0 1 12 19.6a7 7 0 0 1-7-7.1z"
    fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
  <circle class="byte-eye" cx="9.3" cy="11" r="1.75" fill="currentColor"/>
  <circle class="byte-eye" cx="14.7" cy="11" r="1.75" fill="currentColor"/>
  <path d="M11.1 14.1h1.8L12 15.6z" fill="currentColor"/>`;

/** The Byte mark. size: "sm" (20px), "md" (28px), "lg" (40px). Mood is kept for callers; the mark stays calm. */
export function byte(mood = "idle", label = "Byte", { size = "sm" } = {}) {
  const wrap = document.createElement("span");
  wrap.className = `byte byte-${size === "hero" ? "lg" : size}`;
  wrap.dataset.mood = mood;
  const a11y = label ? `role="img" aria-label="${label}"` : `aria-hidden="true"`;
  wrap.innerHTML = `<svg class="byte-mark" viewBox="0 0 24 24" ${a11y}>${MARK}</svg>`;
  return wrap;
}

/** A short message from Byte: the mark and a line of text. */
export function byteSays(text, { mood = "idle", size = "md" } = {}) {
  const box = document.createElement("div");
  box.className = "byte-says";
  const words = document.createElement("div");
  words.className = "speech";
  for (const line of Array.isArray(text) ? text : [text]) {
    const p = document.createElement("p");
    p.append(line);
    words.append(p);
  }
  box.append(byte(mood, "", { size }), words);
  return box;
}
