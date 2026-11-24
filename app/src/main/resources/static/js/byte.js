// The team's helper bot, Satoru Gojo, as a small round mark: the logo, the Hints button, chat messages,
// animation captions and walkthrough bubbles. With the learner's own gojo.jpg in avatars/ it shows that
// picture; without one, a simple mark (spiky hair and a blindfold) that draws in currentColor.
// (The file and function keep the old name "byte" so callers stay the same.)

export const HELPER = { name: "Gojo", fullName: "Satoru Gojo" };

/**
 * The helper follows the course: Gojo for the Java course, Golu (Dhaaga's crocheted elephant, docs/STORY.md) for the
 * web courses. It changes in place, so every import sees the new name.
 */
export function setHelper(courseId) {
  Object.assign(HELPER, courseId === "java-swe"
    ? { name: "Gojo", fullName: "Satoru Gojo" }
    : { name: "Golu", fullName: "Golu the elephant" });
}

/** Picture file names the helper answers to, without the extension. */
export const HELPER_PICTURES = ["gojo", "satoru-gojo", "satoru", "byte"];

const MARK = `<path d="M6.2 7.4 7.6 3.6l1.9 2.6L11 2.6l1.7 3.5L14.8 3l1 3.6 2.6-1.6-.5 3.2"
    fill="none" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round" stroke-linecap="round"/>
  <path d="M5.6 11.2a6.4 6.4 0 0 1 12.8 0v1.6a6.4 6.4 0 0 1-12.8 0z"
    fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
  <rect x="4.6" y="10" width="14.8" height="3.2" rx="1.2" fill="currentColor"/>
  <path d="M10.4 16.6q1.6 1 3.2 0" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>`;

let picture = null;

/** The helper's picture from the learner's avatars/ folder, or null to use the drawn mark. */
export function setHelperPicture(url) {
  picture = url;
}

function drawnMark(wrap, a11y) {
  wrap.classList.remove("has-picture");
  wrap.innerHTML = `<svg class="byte-mark" viewBox="0 0 24 24" ${a11y}>${MARK}</svg>`;
}

/** The helper's mark. size: "sm" (20px), "md" (28px), "lg" (40px). Mood is kept for callers; the mark stays calm. */
export function byte(mood = "idle", label = HELPER.fullName, { size = "sm" } = {}) {
  const wrap = document.createElement("span");
  wrap.className = `byte byte-${size === "hero" ? "lg" : size}`;
  wrap.dataset.mood = mood;
  const a11y = label ? `role="img" aria-label="${label}"` : `aria-hidden="true"`;
  if (picture) {
    const img = document.createElement("img");
    img.src = picture;
    img.alt = label || "";
    img.decoding = "async";
    if (!label) img.setAttribute("aria-hidden", "true");
    img.addEventListener("error", () => drawnMark(wrap, a11y));
    wrap.classList.add("has-picture");
    wrap.append(img);
  } else {
    drawnMark(wrap, a11y);
  }
  return wrap;
}

/** A short message from the helper: the mark and a line of text. */
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
