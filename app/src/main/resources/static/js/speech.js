import { h, icon } from "./dom.js";

/*
 * Read aloud, with the browser's own speech (Web Speech API): nothing is downloaded or sent anywhere.
 * A Listen button reads a card or section paragraph by paragraph, highlighting the one being read.
 * Code blocks, animations and buttons are skipped. The voice and speed are remembered in this browser.
 */

const synth = typeof window !== "undefined" ? window.speechSynthesis : undefined;
export const speechSupported = Boolean(synth && window.SpeechSynthesisUtterance);

const VOICE_KEY = "jq-voice";
const RATE_KEY = "jq-rate";
const MIN_RATE = 0.5;
const MAX_RATE = 2;
const SAMPLE = "Yo, I'm Gojo. This is how I sound when I read your lessons.";
/** macOS and Chrome ship joke voices (bells, whispers, robots); they make poor teachers. */
const NOVELTY = /^(Albert|Bad News|Bahh|Bells|Boing|Bubbles|Cellos|Good News|Jester|Organ|Superstar|Trinoids|Whisper|Wobble|Zarvox|Deranged|Hysterical|Pipe Organ)\b/i;
/** Reading these elements, in page order, covers the text of a card. */
const BLOCKS = "h1, h2, h3, h4, p, li, .callout-label, td, th";
const SKIP = "pre, code.block, .snippet, figure.animation, .anim, .mermaid, .listen-bar, button, nav, .intro-meta, [aria-hidden='true'], .visually-hidden";

const store = {
  get(key) { try { return localStorage.getItem(key); } catch { return null; } },
  set(key, value) { try { localStorage.setItem(key, value); } catch { /* not remembered: fine */ } },
};

let voices = [];
let session = null;
let token = 0;

function loadVoices() {
  if (!speechSupported) return [];
  const lang = (navigator.language || "en").slice(0, 2).toLowerCase();
  // The lessons are in English: offer English voices, plus the browser's own language, never joke voices.
  const usable = synth.getVoices().filter((v) => !NOVELTY.test(v.name));
  const fitting = usable.filter((v) => /^en/i.test(v.lang) || v.lang.toLowerCase().startsWith(lang));
  const all = fitting.length ? fitting : usable;
  const locale = (navigator.language || "en-US").toLowerCase().replace("_", "-");
  const rank = (v) => {
    const own = v.lang.toLowerCase().replace("_", "-");
    return own === locale ? 0 : own.startsWith(lang) ? 1 : own.startsWith("en") ? 2 : 3;
  };
  voices = all.sort((a, b) => rank(a) - rank(b) || a.lang.localeCompare(b.lang) || a.name.localeCompare(b.name));
  return voices;
}
if (speechSupported) {
  loadVoices();
  synth.addEventListener?.("voiceschanged", () => {
    loadVoices();
    document.dispatchEvent(new Event("jq-voices"));
  });
}

export function rate() {
  const value = Number(store.get(RATE_KEY));
  return value >= MIN_RATE && value <= MAX_RATE ? value : 1;
}

function chosenVoice() {
  if (voices.length === 0) loadVoices();
  const saved = store.get(VOICE_KEY);
  return voices.find((v) => v.voiceURI === saved)
    ?? voices.find((v) => v.default && rankEnglish(v))
    ?? voices.find((v) => /^en[-_](US|GB)/i.test(v.lang))
    ?? voices[0];
}
const rankEnglish = (v) => v.lang.toLowerCase().startsWith("en");

/** The readable text of a container, as blocks: each has its element (for the highlight) and its sentences. */
function blocksOf(container) {
  const blocks = [];
  for (const el of container.querySelectorAll(BLOCKS)) {
    if (el.closest(SKIP)) continue;
    // A list item that holds paragraphs is read through those paragraphs.
    if (el.matches("li") && el.querySelector(":scope > p")) continue;
    const text = textOf(el);
    if (text) blocks.push({ el, sentences: sentences(text) });
  }
  return blocks;
}

function textOf(el) {
  const copy = el.cloneNode(true);
  copy.querySelectorAll(SKIP).forEach((n) => n.remove());
  return copy.textContent.replace(/\s+/g, " ").replace(/[→←]/g, " ").trim();
}

/** Short pieces keep some browsers from stopping halfway through a long utterance. */
function sentences(text) {
  const parts = text.match(/[^.!?]+[.!?]+["')\]]*\s*|[^.!?]+$/g) ?? [text];
  const out = [];
  for (const part of parts.map((p) => p.trim()).filter(Boolean)) {
    if (out.length && (out.at(-1).length + part.length) < 160) out[out.length - 1] += ` ${part}`;
    else out.push(part);
  }
  return out;
}

/** Stops reading, wherever it was started. */
export function stopSpeaking() {
  token++;
  if (speechSupported) synth.cancel();
  if (session) {
    session.blocks[session.index]?.el.classList.remove("is-reading");
    session.onStop?.();
  }
  session = null;
}

function speakFrom(index, sentence = 0) {
  const current = session;
  if (!current) return;
  const mine = ++token;
  const block = current.blocks[index];
  if (!block) {
    stopSpeaking();
    return;
  }
  if (index !== current.index) current.blocks[current.index]?.el.classList.remove("is-reading");
  current.index = index;
  current.sentence = sentence;
  block.el.closest("details:not([open])")?.setAttribute("open", "");
  block.el.classList.add("is-reading");
  if (sentence === 0) block.el.scrollIntoView?.({ block: "nearest", behavior: "smooth" });
  const utterance = new SpeechSynthesisUtterance(block.sentences[sentence]);
  const voice = chosenVoice();
  if (voice) {
    utterance.voice = voice;
    utterance.lang = voice.lang;
  }
  utterance.rate = rate();
  const next = () => {
    if (mine !== token || session !== current) return;
    if (sentence + 1 < block.sentences.length) speakFrom(index, sentence + 1);
    else speakFrom(index + 1, 0);
  };
  utterance.onend = next;
  utterance.onerror = (e) => {
    if (e.error === "interrupted" || e.error === "canceled") return;
    next();
  };
  synth.speak(utterance);
}

/** A new voice or speed takes effect from the sentence being read. */
function restartCurrent() {
  if (!session) return;
  const { index, sentence } = session;
  token++;
  synth.cancel();
  speakFrom(index, sentence);
}

/**
 * A Listen button and a voice settings button for a card or section. getContainer returns the element whose text
 * is read; it is looked up on each click, so content drawn later is read too.
 */
export function listenBar(getContainer, { label = "Listen", what = "this card" } = {}) {
  if (!speechSupported) return null;
  const text = h("span", {}, label);
  const button = h("button", { type: "button", class: "listen-button", "aria-pressed": "false",
    "aria-label": `Read ${what} aloud` }, icon("volume"), text);
  const setPlaying = (on) => {
    button.setAttribute("aria-pressed", String(on));
    button.classList.toggle("is-playing", on);
    button.replaceChildren(icon(on ? "stop" : "volume"), h("span", {}, on ? "Stop" : label));
    button.setAttribute("aria-label", on ? "Stop reading" : `Read ${what} aloud`);
  };
  button.addEventListener("click", () => {
    if (session?.button === button) {
      stopSpeaking();
      return;
    }
    stopSpeaking();
    const container = typeof getContainer === "function" ? getContainer() : getContainer;
    const blocks = container ? blocksOf(container) : [];
    if (blocks.length === 0) return;
    session = { button, blocks, index: 0, sentence: 0, onStop: () => setPlaying(false) };
    setPlaying(true);
    speakFrom(0, 0);
  });
  const settings = h("button", { type: "button", class: "listen-settings", "aria-label": "Voice and speed",
    "aria-haspopup": "dialog", "aria-expanded": "false", title: "Voice and speed" }, icon("settings"));
  settings.addEventListener("click", () => toggleVoicePanel(settings));
  return h("div", { class: "listen-bar" }, button, settings);
}

let panel = null;

function closeVoicePanel() {
  if (!panel) return;
  panel.anchor.setAttribute("aria-expanded", "false");
  panel.el.remove();
  document.removeEventListener("pointerdown", panel.outside, true);
  document.removeEventListener("keydown", panel.escape, true);
  const anchor = panel.anchor;
  panel = null;
  return anchor;
}

/** A small panel under the gear: the voice, the speed, and a sample to hear them. */
function toggleVoicePanel(anchor) {
  if (panel?.anchor === anchor) {
    closeVoicePanel();
    return;
  }
  closeVoicePanel();
  const select = h("select", { id: "voice-select" });
  const fillVoices = () => {
    const current = chosenVoice();
    select.replaceChildren(...(voices.length ? voices : loadVoices()).map((v) =>
      h("option", { value: v.voiceURI, selected: v === current }, `${v.name} · ${v.lang}`)));
    if (voices.length === 0) select.append(h("option", { value: "" }, "No voices found in this browser"));
  };
  fillVoices();
  document.addEventListener("jq-voices", fillVoices, { once: true });
  const speed = h("input", { type: "range", id: "voice-speed", min: MIN_RATE, max: MAX_RATE, step: 0.1, value: rate() });
  const speedValue = h("output", { for: "voice-speed", class: "voice-speed-value" }, `${rate().toFixed(1)}×`);
  select.addEventListener("change", () => {
    store.set(VOICE_KEY, select.value);
    restartCurrent();
  });
  speed.addEventListener("input", () => {
    speedValue.textContent = `${Number(speed.value).toFixed(1)}×`;
  });
  speed.addEventListener("change", () => {
    store.set(RATE_KEY, speed.value);
    restartCurrent();
  });
  const sample = h("button", { type: "button", class: "button", onclick: () => {
    stopSpeaking();
    const utterance = new SpeechSynthesisUtterance(SAMPLE);
    const voice = chosenVoice();
    if (voice) {
      utterance.voice = voice;
      utterance.lang = voice.lang;
    }
    utterance.rate = rate();
    synth.speak(utterance);
  } }, icon("play"), "Try it");
  const el = h("div", { class: "voice-panel", role: "dialog", "aria-label": "Voice and speed" },
    h("label", { for: "voice-select" }, "Voice"), select,
    h("div", { class: "voice-speed" }, h("label", { for: "voice-speed" }, "Speed"), speedValue),
    speed,
    h("div", { class: "voice-speed-scale", "aria-hidden": "true" }, h("span", {}, "Slower"), h("span", {}, "Faster")),
    h("div", { class: "voice-panel-foot" }, sample,
      h("button", { type: "button", class: "button button-quiet", onclick: () => closeVoicePanel()?.focus() }, "Done")));
  document.body.append(el);
  const r = anchor.getBoundingClientRect();
  const width = Math.min(300, innerWidth - 24);
  el.style.width = `${width}px`;
  el.style.left = `${Math.max(12, Math.min(innerWidth - width - 12, r.right - width))}px`;
  el.style.top = `${r.bottom + 6 + scrollY}px`;
  anchor.setAttribute("aria-expanded", "true");
  panel = {
    el, anchor,
    outside: (e) => { if (!el.contains(e.target) && !anchor.contains(e.target)) closeVoicePanel(); },
    escape: (e) => { if (e.key === "Escape") { e.stopPropagation(); closeVoicePanel()?.focus(); } },
  };
  document.addEventListener("pointerdown", panel.outside, true);
  document.addEventListener("keydown", panel.escape, true);
  select.focus();
}

/** Leaving a page or card stops the reading and closes the panel. */
export function stopAll() {
  stopSpeaking();
  closeVoicePanel();
}
