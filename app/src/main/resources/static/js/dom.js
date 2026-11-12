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
  maya: { name: "Maya Okafor", role: "Lead engineer", initials: "MO" },
  theo: { name: "Theo Lindqvist", role: "Operations", initials: "TL" },
  priya: { name: "Priya Raman", role: "Product", initials: "PR" },
  sam: { name: "Sam Ortiz", role: "Rider support", initials: "SO" },
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
