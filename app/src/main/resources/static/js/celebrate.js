import { reducedMotion } from "./dom.js";

/** A brief, soft glow of the accent colour across the page: a quiet "well done". Nothing under reduced motion. */
export function glow() {
  if (reducedMotion()) return;
  const layer = document.createElement("div");
  layer.className = "success-glow";
  layer.setAttribute("aria-hidden", "true");
  document.body.append(layer);
  setTimeout(() => layer.remove(), 1600);
}

/** "+50 XP" rising from an element (default: the level stat in the top bar). */
export function xpFloat(amount, from = document.querySelector(".stat-level")) {
  if (reducedMotion() || !from) return;
  const box = from.getBoundingClientRect();
  const label = document.createElement("span");
  label.className = "xp-float";
  label.setAttribute("aria-hidden", "true");
  label.textContent = `+${amount} XP`;
  label.style.left = `${box.left + box.width / 2 - 28}px`;
  label.style.top = `${box.bottom + 6}px`;
  document.body.append(label);
  setTimeout(() => label.remove(), 1500);
}
