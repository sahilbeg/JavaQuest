export function h(tag, attrs = {}, ...children) {
  const node = document.createElement(tag);
  for (const [key, value] of Object.entries(attrs)) {
    if (value === false || value == null) continue;
    if (key.startsWith("on")) node.addEventListener(key.slice(2), value);
    else if (key === "class") node.className = value;
    else if (key === "html") node.innerHTML = value;
    else node.setAttribute(key, value === true ? "" : value);
  }
  node.append(...children.flat(Infinity).filter((c) => c != null && c !== false));
  return node;
}

export function icon(name, label) {
  const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  svg.setAttribute("class", "icon");
  if (label) {
    svg.setAttribute("role", "img");
    svg.setAttribute("aria-label", label);
  } else {
    svg.setAttribute("aria-hidden", "true");
  }
  const use = document.createElementNS("http://www.w3.org/2000/svg", "use");
  use.setAttribute("href", `/vendor/icons/icons.svg#i-${name}`);
  svg.append(use);
  return svg;
}

export const TEAM = {
  // The keys are the ids in meta.yaml (ticket.from) and kudos; only the names shown change.
  // The cast is Konoha while Tsunade is the Fifth Hokage. See docs/STORY.md.
  maya: { name: "Tsunade", role: "Fifth Hokage", initials: "TS" },
  theo: { name: "Kakashi Hatake", role: "Jōnin, Team 7 leader", initials: "KH" },
  priya: { name: "Jiraiya", role: "Legendary Sannin, the Toad Sage", initials: "JI" },
  sam: { name: "Itachi Uchiha", role: "Akatsuki, former ANBU captain", initials: "IU" },
};

let pictures = {};

/** The first picture file whose name, without its extension, is one of names. */
export function pictureFor(files = [], names = []) {
  const file = names.map((n) => n.toLowerCase()).map((n) => files.find((f) => f.replace(/\.[a-z]+$/, "") === n)).find(Boolean);
  return file ? `/api/team/avatars/${encodeURIComponent(file)}` : null;
}

/**
 * Takes the picture files in the learner's avatars/ folder and picks one per teammate: the character's first name
 * (tsunade.png), their full name (kakashi-hatake.jpg) or their id (maya.png).
 */
export function setTeamPictures(files = []) {
  pictures = {};
  for (const [id, person] of Object.entries(TEAM)) {
    const url = pictureFor(files, [person.name.split(" ")[0], person.name.replace(/\s+/g, "-"), id]);
    if (url) pictures[id] = url;
  }
}

/** A teammate's picture in a circle, or their initials when there is no picture (or it fails to load). */
export function avatar(who, size = "") {
  const person = TEAM[who];
  const initials = person?.initials ?? "?";
  const node = h("span", { class: `avatar avatar-${who} ${size}`, "aria-hidden": "true" });
  if (pictures[who]) {
    const img = h("img", { src: pictures[who], alt: "", loading: "lazy", decoding: "async" });
    img.addEventListener("error", () => {
      node.classList.remove("has-picture");
      node.replaceChildren(initials);
    });
    node.classList.add("has-picture");
    node.append(img);
  } else {
    node.append(initials);
  }
  return node;
}

export function toast(text) {
  const region = document.getElementById("toasts");
  const node = h("div", { class: "toast" }, text);
  region.append(node);
  setTimeout(() => node.remove(), 5000);
}

export const reducedMotion = () => matchMedia("(prefers-reduced-motion: reduce)").matches;
