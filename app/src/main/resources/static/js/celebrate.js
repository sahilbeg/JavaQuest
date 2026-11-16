import { reducedMotion } from "./dom.js";

const COLOURS = ["#22b07d", "#f5b83d", "#8b5cf6", "#3b9ae1", "#ef6f5c", "#ffd84d"];

/** A short confetti burst from a point (default: the top centre). Nothing under reduced motion. */
export function confetti({ x = innerWidth / 2, y = innerHeight * 0.25, count = 140 } = {}) {
  if (reducedMotion()) return;
  const canvas = document.createElement("canvas");
  canvas.className = "confetti";
  canvas.width = innerWidth * devicePixelRatio;
  canvas.height = innerHeight * devicePixelRatio;
  document.body.append(canvas);
  const ctx = canvas.getContext("2d");
  ctx.scale(devicePixelRatio, devicePixelRatio);
  const pieces = Array.from({ length: count }, () => {
    const angle = Math.random() * Math.PI * 2;
    const speed = 4 + Math.random() * 9;
    return {
      x, y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed - 6,
      size: 6 + Math.random() * 7,
      spin: Math.random() * Math.PI,
      turn: (Math.random() - 0.5) * 0.3,
      colour: COLOURS[Math.floor(Math.random() * COLOURS.length)],
      round: Math.random() < 0.3,
    };
  });
  const start = performance.now();
  function frame(now) {
    const t = now - start;
    ctx.clearRect(0, 0, innerWidth, innerHeight);
    for (const p of pieces) {
      p.vy += 0.32;
      p.vx *= 0.985;
      p.x += p.vx;
      p.y += p.vy;
      p.spin += p.turn;
      ctx.save();
      ctx.globalAlpha = Math.max(0, 1 - t / 2200);
      ctx.translate(p.x, p.y);
      ctx.rotate(p.spin);
      ctx.fillStyle = p.colour;
      if (p.round) {
        ctx.beginPath();
        ctx.arc(0, 0, p.size / 2, 0, Math.PI * 2);
        ctx.fill();
      } else {
        ctx.fillRect(-p.size / 2, -p.size / 4, p.size, p.size / 2);
      }
      ctx.restore();
    }
    if (t < 2200) requestAnimationFrame(frame);
    else canvas.remove();
  }
  requestAnimationFrame(frame);
}

/** "+50 XP" floating up from an element (default: the XP chip in the top bar). */
export function xpFloat(amount, from = document.querySelector(".stat-level")) {
  if (reducedMotion() || !from) return;
  const box = from.getBoundingClientRect();
  const label = document.createElement("span");
  label.className = "xp-float";
  label.setAttribute("aria-hidden", "true");
  label.textContent = `+${amount} XP`;
  label.style.left = `${box.left + box.width / 2 - 30}px`;
  label.style.top = `${box.bottom + 4}px`;
  document.body.append(label);
  setTimeout(() => label.remove(), 1500);
}
