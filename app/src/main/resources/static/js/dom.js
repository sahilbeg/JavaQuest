export function h(tag, attrs = {}, ...children) {
  const node = document.createElement(tag);
  for (const [key, value] of Object.entries(attrs)) {
    if (value === false || value == null) continue;
    if (key.startsWith("on")) node.addEventListener(key.slice(2), value);
    else if (key === "class") node.className = value;
    else if (key === "html") node.innerHTML = value;
    else node.setAttribute(key, value === true ? "" : value);
  }
  node.append(...children.flat().filter((c) => c != null && c !== false));
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
  priya: { name: "Shizune", role: "The Hokage's assistant", initials: "SZ" },
  sam: { name: "Iruka Umino", role: "Chūnin, mission desk", initials: "IU" },
};

export function avatar(who, size = "") {
  const person = TEAM[who];
  return h("span", { class: `avatar avatar-${who} ${size}`, "aria-hidden": "true" }, person?.initials ?? "?");
}

export function toast(text) {
  const region = document.getElementById("toasts");
  const node = h("div", { class: "toast" }, text);
  region.append(node);
  setTimeout(() => node.remove(), 5000);
}

export const reducedMotion = () => matchMedia("(prefers-reduced-motion: reduce)").matches;
