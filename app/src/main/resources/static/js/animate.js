// Plays the animated explanations in a lesson (```animate blocks, rendered as <figure class="animation">).
// Format: docs/CONTENT_SPEC.md and content-model Animation.java.
//
// code  : a program stepped line by line, with variables, objects (and arrows to them) and console output.
// scene : labelled boxes; steps highlight boxes, change their values and send tokens between them.
//
// Every step has a caption from Gojo, the helper, read out through an aria-live region. Under reduced motion nothing
// moves on its own: the learner steps through, and changes appear without tweening.

import { h, icon, reducedMotion } from "./dom.js";
import { byte } from "./byte.js";

const SVG = "http://www.w3.org/2000/svg";
let markerCount = 0;
const TOKEN_MS = 1100;

export function enhanceAnimations(container) {
  const players = [...container.querySelectorAll("figure.animation")].map((figure) => player(figure));
  return () => players.forEach((p) => p.dispose());
}

function player(figure) {
  const spec = JSON.parse(figure.dataset.spec);
  const steps = spec.steps;
  let index = -1;
  let playing = false;
  let timer;
  let disposed = false;

  figure.classList.add("anim");
  figure.tabIndex = -1;
  const title = figure.querySelector("figcaption");
  const head = h("div", { class: "anim-head" }, title, h("span", { class: "anim-tag" }, icon("film"), "Animation"));
  const body = h("div", { class: "anim-body" });
  const caption = h("p", { "aria-live": "polite" }, "Press Watch to see it happen, one step at a time.");
  const captionRow = h("div", { class: "anim-caption" }, byte("idle", "", { size: "sm" }), caption);
  const playButton = h("button", { type: "button", class: "icon-button", "aria-label": "Play", onclick: togglePlay }, icon("play"));
  const dots = h("div", { class: "anim-dots", "aria-hidden": "true" }, steps.map(() => h("span")));
  const count = h("span", { class: "anim-count" });
  const controls = h("div", { class: "anim-controls" },
    h("button", { type: "button", class: "icon-button", "aria-label": "Start again", onclick: () => { stop(); go(0); } }, icon("skip-back")),
    h("button", { type: "button", class: "icon-button", "aria-label": "Previous step", onclick: () => { stop(); go(index - 1); } }, icon("chevron-left")),
    playButton,
    h("button", { type: "button", class: "icon-button", "aria-label": "Next step", onclick: () => { stop(); go(index + 1); } }, icon("chevron-right")),
    dots, count);
  const overlay = h("button", { type: "button", class: "anim-play", onclick: () => { overlay.remove(); start(); } },
    h("span", {}, icon("play"), "Watch"));

  const view = spec.kind === "code" ? codeView(spec, body) : sceneView(spec, body);
  body.append(overlay);
  figure.replaceChildren(head, body, captionRow, controls, figure.querySelector(".animation-fallback") ?? "");
  view.render(-1, false);
  updateChrome();

  figure.addEventListener("keydown", (e) => {
    if (e.target.closest("input, textarea")) return;
    if (e.key === "ArrowRight" || e.key === "ArrowLeft") {
      e.preventDefault();
      e.stopPropagation();
      stop();
      go(index + (e.key === "ArrowRight" ? 1 : -1));
    }
  });
  const resize = new ResizeObserver(() => view.relayout());
  resize.observe(body);

  function start() {
    if (reducedMotion()) {
      go(0);
      return;
    }
    playing = true;
    go(index >= steps.length - 1 ? 0 : index + 1);
  }

  function togglePlay() {
    overlay.remove();
    if (playing) stop();
    else start();
    updateChrome();
  }

  function stop() {
    playing = false;
    clearTimeout(timer);
    updateChrome();
  }

  function go(next) {
    if (disposed) return;
    overlay.remove();
    next = Math.max(0, Math.min(steps.length - 1, next));
    const forward = next > index;
    index = next;
    const step = steps[index];
    caption.textContent = step.say;
    captionRow.querySelector(".byte").dataset.mood = index === steps.length - 1 ? "cheering" : "idle";
    const busyFor = view.render(index, forward && !reducedMotion());
    updateChrome();
    clearTimeout(timer);
    if (playing) {
      if (index >= steps.length - 1) {
        playing = false;
        updateChrome();
      } else {
        const reading = 1400 + step.say.length * 38;
        timer = setTimeout(() => go(index + 1), Math.max(2600, reading, busyFor + 900));
      }
    }
  }

  function updateChrome() {
    const atEnd = index >= steps.length - 1;
    playButton.replaceChildren(icon(playing ? "pause" : atEnd && index >= 0 ? "rotate-ccw" : "play"));
    playButton.setAttribute("aria-label", playing ? "Pause" : atEnd && index >= 0 ? "Play again" : "Play");
    [...dots.children].forEach((dot, i) => {
      dot.className = i === index ? "is-current" : i < index ? "is-seen" : "";
    });
    count.textContent = index < 0 ? `${steps.length} steps` : `Step ${index + 1} of ${steps.length}`;
  }

  return {
    dispose() {
      disposed = true;
      clearTimeout(timer);
      resize.disconnect();
    },
  };
}

/* ---------- code ---------- */

function codeView(spec, body) {
  const lines = spec.code.replace(/\n$/, "").split("\n");
  const lineEls = lines.map((text, i) => h("div", { class: "anim-line" }, h("span", { class: "ln" }, String(i + 1)), h("code", {}, text || " ")));
  const vars = h("div", { class: "anim-vars" });
  const heap = h("div", { class: "anim-heap" });
  const consoleLines = h("pre", { class: "anim-console-lines" });
  const arrows = document.createElementNS(SVG, "svg");
  arrows.setAttribute("class", "anim-arrows");
  arrows.setAttribute("aria-hidden", "true");
  const markerId = `anim-head-${++markerCount}`;
  const hasObjects = spec.steps.some((s) => s.objects);
  const hasOutput = spec.steps.some((s) => s.out != null);
  body.append(h("div", { class: `anim-code-layout${hasObjects ? " has-heap" : ""}` },
    h("pre", { class: "anim-code", "aria-label": "The code" }, lineEls),
    h("div", { class: "anim-side" },
      h("div", { class: "anim-memory" }, h("p", { class: "anim-panel-label" }, "Memory"), vars, hasObjects && heap),
      hasOutput && h("div", { class: "anim-console" }, h("p", { class: "anim-panel-label" }, "Console"), consoleLines))),
    arrows);

  const varEls = new Map();
  const objEls = new Map();

  function stateAt(index) {
    const v = new Map();
    const o = new Map();
    const out = [];
    for (let i = 0; i <= index; i++) {
      const s = spec.steps[i];
      for (const [k, val] of Object.entries(s.vars ?? {})) val == null || val === "~" ? v.delete(k) : v.set(k, val);
      for (const [k, obj] of Object.entries(s.objects ?? {})) obj == null || obj === "~" ? o.delete(k) : o.set(k, obj);
      if (s.out != null) out.push(...String(s.out).replace(/\n$/, "").split("\n"));
    }
    return { v, o, out };
  }

  function render(index, animate) {
    const step = spec.steps[index] ?? {};
    const { v, o, out } = stateAt(index);
    const before = index > 0 ? stateAt(index - 1) : { v: new Map(), o: new Map(), out: [] };
    lineEls.forEach((el, i) => el.classList.toggle("is-current", step.line === i + 1));
    const current = lineEls[(step.line ?? 0) - 1];
    if (current) current.scrollIntoView?.({ block: "nearest", inline: "nearest" });

    // Variables, in the order they first appear.
    for (const [name, el] of varEls) {
      if (!v.has(name)) { el.remove(); varEls.delete(name); }
    }
    for (const [name, value] of v) {
      let el = varEls.get(name);
      if (!el) {
        el = h("div", { class: "anim-var" }, h("span", { class: "anim-var-name" }, name), h("span", { class: "anim-box" }));
        varEls.set(name, el);
        vars.append(el);
        if (animate) el.classList.add("slide-in");
      }
      const box = el.querySelector(".anim-box");
      const isRef = typeof value === "string" && value.startsWith("@");
      box.classList.toggle("is-ref", isRef);
      box.dataset.ref = isRef ? value.slice(1) : "";
      box.textContent = isRef ? "" : String(value);
      box.setAttribute("aria-label", isRef ? `${name} points to the ${o.get(value.slice(1))?.type ?? "object"}` : `${name} is ${value}`);
      if (animate && before.v.has(name) && before.v.get(name) !== value) replay(box, "pop");
    }

    // Objects on the heap.
    for (const [id, el] of objEls) {
      if (!o.has(id)) { el.remove(); objEls.delete(id); }
    }
    for (const [id, obj] of o) {
      let el = objEls.get(id);
      if (!el) {
        el = h("div", { class: "anim-obj", "data-id": id });
        objEls.set(id, el);
        heap.append(el);
        if (animate) el.classList.add("slide-in");
      }
      const changed = JSON.stringify(before.o.get(id)) !== JSON.stringify(obj);
      el.replaceChildren(h("div", { class: "anim-obj-type" }, obj.type),
        h("div", { class: "anim-obj-fields" }, Object.entries(obj.fields ?? {}).map(([f, val]) => {
          const isRef = typeof val === "string" && val.startsWith("@");
          return h("div", { class: "anim-field" }, h("span", {}, f),
            isRef ? h("span", { class: "anim-box is-ref", "data-ref": val.slice(1), style: "min-width:24px;min-height:22px;padding:2px" })
              : h("span", {}, String(val)));
        })));
      if (animate && changed && before.o.has(id)) replay(el, "pop");
    }

    // Console.
    consoleLines.replaceChildren(...out.map((line, i) =>
      h("span", { class: animate && i >= before.out.length ? "is-new" : "" }, `${line}\n`)));

    requestAnimationFrame(drawArrows);
    return 0;
  }

  function drawArrows() {
    arrows.replaceChildren(arrowHeadDefs(markerId));
    const origin = body.getBoundingClientRect();
    for (const box of body.querySelectorAll(".anim-box.is-ref")) {
      const target = body.querySelector(`.anim-obj[data-id="${CSS.escape(box.dataset.ref)}"]`);
      if (!target) continue;
      const a = box.getBoundingClientRect();
      const b = target.getBoundingClientRect();
      const x1 = a.left + a.width / 2 - origin.left;
      const y1 = a.top + a.height / 2 - origin.top;
      const x2 = b.left + Math.min(24, b.width / 2) - origin.left;
      const y2 = b.top - origin.top - 2;
      const path = document.createElementNS(SVG, "path");
      const bend = Math.max(30, Math.abs(y2 - y1) / 2);
      path.setAttribute("d", `M${x1},${y1} C${x1},${y1 + bend} ${x2},${y2 - bend} ${x2},${y2}`);
      path.setAttribute("marker-end", `url(#${markerId})`);
      arrows.append(path);
    }
  }

  return { render, relayout: () => requestAnimationFrame(drawArrows) };
}

/* ---------- scene ---------- */

function sceneView(spec, body) {
  const shownLater = new Set(spec.steps.flatMap((s) => s.show ?? []));
  const cols = Math.max(...spec.nodes.map((n, i) => (n.col ?? i) + 1));
  const grid = h("div", { class: "anim-scene", style: `grid-template-columns: repeat(${cols}, minmax(0, 1fr))` });
  const nodeEls = new Map();
  spec.nodes.forEach((node, i) => {
    const el = h("div", { class: "anim-node", "data-id": node.id,
        style: `grid-row:${(node.row ?? 0) + 1}; grid-column:${(node.col ?? i) + 1}` },
      node.emoji && h("span", { class: "anim-node-emoji", "aria-hidden": "true" }, node.emoji),
      h("span", { class: "anim-node-label" }, node.label),
      h("span", { class: "anim-node-value" }, node.value ?? ""));
    nodeEls.set(node.id, el);
    grid.append(el);
  });
  const arrows = document.createElementNS(SVG, "svg");
  arrows.setAttribute("class", "anim-arrows");
  arrows.setAttribute("aria-hidden", "true");
  const markerId = `anim-head-${++markerCount}`;
  body.append(grid, arrows);
  const tokens = [];

  function stateAt(index) {
    const visible = new Set(spec.nodes.filter((n) => !shownLater.has(n.id)).map((n) => n.id));
    const values = new Map(spec.nodes.map((n) => [n.id, n.value ?? ""]));
    for (let i = 0; i <= index; i++) {
      const s = spec.steps[i];
      (s.show ?? []).forEach((id) => visible.add(id));
      (s.hide ?? []).forEach((id) => visible.delete(id));
      for (const [id, value] of Object.entries(s.set ?? {})) values.set(id, value ?? "");
    }
    return { visible, values };
  }

  function center(el) {
    const origin = body.getBoundingClientRect();
    const r = el.getBoundingClientRect();
    return { x: r.left + r.width / 2 - origin.left, y: r.top + r.height / 2 - origin.top };
  }

  let sends = [];
  function render(index, animate) {
    const step = spec.steps[index] ?? {};
    const { visible, values } = stateAt(index);
    const before = index > 0 ? stateAt(index - 1) : stateAt(-1);
    const focus = new Set(step.focus ?? []);
    for (const [id, el] of nodeEls) {
      el.classList.toggle("is-hidden", !visible.has(id));
      el.classList.toggle("is-focus", focus.has(id));
      const valueEl = el.querySelector(".anim-node-value");
      valueEl.textContent = values.get(id);
      if (animate && before.values.get(id) !== values.get(id)) replay(valueEl, "pop");
    }
    tokens.splice(0).forEach((t) => t.remove());
    sends = step.send ?? [];
    requestAnimationFrame(() => {
      drawLinks();
      if (index >= 0) playSends(animate);
    });
    return animate ? sends.length * TOKEN_MS : 0;
  }

  function drawLinks() {
    arrows.replaceChildren(arrowHeadDefs(markerId));
    for (const link of spec.links ?? []) {
      const from = nodeEls.get(link.from);
      const to = nodeEls.get(link.to);
      if (from.classList.contains("is-hidden") || to.classList.contains("is-hidden")) continue;
      const a = center(from);
      const b = center(to);
      const line = document.createElementNS(SVG, "path");
      line.setAttribute("d", `M${a.x},${a.y} L${b.x},${b.y}`);
      line.setAttribute("class", "anim-link");
      arrows.append(line);
      if (link.label) {
        const text = document.createElementNS(SVG, "text");
        text.setAttribute("x", String((a.x + b.x) / 2));
        text.setAttribute("y", String((a.y + b.y) / 2 - 6));
        text.setAttribute("class", "anim-link-label");
        text.textContent = link.label;
        arrows.append(text);
      }
    }
    for (const send of sends) {
      const a = center(nodeEls.get(send.from));
      const b = center(nodeEls.get(send.to));
      const path = document.createElementNS(SVG, "path");
      const lift = send.back ? 26 : -26;
      const mx = (a.x + b.x) / 2;
      const my = (a.y + b.y) / 2 + (Math.abs(a.y - b.y) < 20 ? lift : 0);
      path.setAttribute("d", `M${a.x},${a.y} Q${mx},${my} ${b.x},${b.y}`);
      path.setAttribute("stroke-dasharray", "6 6");
      path.setAttribute("opacity", "0.55");
      path.setAttribute("marker-end", `url(#${markerId})`);
      arrows.append(path);
    }
  }

  function playSends(animate) {
    sends.forEach((send, i) => {
      const a = center(nodeEls.get(send.from));
      const b = center(nodeEls.get(send.to));
      const token = h("span", { class: `anim-token${send.back ? " is-back" : ""}` }, send.label ?? "");
      body.append(token);
      tokens.push(token);
      const w = token.offsetWidth;
      const ht = token.offsetHeight;
      const lift = Math.abs(a.y - b.y) < 20 ? (send.back ? 26 : -26) / 2 : 0;
      const at = (p, dy = 0) => `translate(${p.x - w / 2}px, ${p.y - ht / 2 + dy}px)`;
      if (!animate) {
        token.style.transition = "none";
        token.style.transform = at(b, lift);
        return;
      }
      token.style.transition = "none";
      token.style.transform = at(a);
      token.style.opacity = "0";
      setTimeout(() => {
        token.style.transition = "";
        token.style.opacity = "1";
        token.style.transform = at(b, lift);
      }, 60 + i * TOKEN_MS);
    });
  }

  return {
    render,
    relayout: () => requestAnimationFrame(() => {
      tokens.splice(0).forEach((t) => t.remove());
      drawLinks();
      playSends(false);
    }),
  };
}

/* ---------- shared ---------- */

function arrowHeadDefs(id) {
  const defs = document.createElementNS(SVG, "defs");
  defs.innerHTML = `<marker id="${id}" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
    <path class="arrow-head" d="M0,0 L10,5 L0,10 z"/></marker>`;
  return defs;
}

function replay(el, className) {
  el.classList.remove(className);
  void el.offsetWidth;
  el.classList.add(className);
}
