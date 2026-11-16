// Byte, the team's owl. One illustrated SVG with a few moods. Colours come from CSS tokens (--owl-*),
// so Byte looks right in both themes. Blinking and waving are CSS animations, off under reduced motion.

const EYES = {
  idle: `<circle class="pupil" cx="25" cy="27" r="4.2"/><circle class="pupil" cx="39" cy="27" r="4.2"/>
         <circle class="glint" cx="26.4" cy="25.6" r="1.3"/><circle class="glint" cx="40.4" cy="25.6" r="1.3"/>`,
  thinking: `<circle class="pupil" cx="23.6" cy="25" r="4.2"/><circle class="pupil" cx="37.6" cy="25" r="4.2"/>
         <circle class="glint" cx="25" cy="23.6" r="1.3"/><circle class="glint" cx="39" cy="23.6" r="1.3"/>
         <path class="brow" d="M33 17.5q6-3 11 0.5"/>`,
  cheering: `<path class="happy" d="M20 28q5-6 10 0M34 28q5-6 10 0"/>`,
  encouraging: `<circle class="pupil" cx="25" cy="27.5" r="4.2"/><circle class="glint" cx="26.4" cy="26" r="1.3"/>
         <path class="happy" d="M34 28.5q5-5 10 0"/>`,
  surprised: `<circle class="pupil" cx="25" cy="27" r="5.2"/><circle class="pupil" cx="39" cy="27" r="5.2"/>
         <circle class="glint" cx="26.8" cy="25.2" r="1.6"/><circle class="glint" cx="40.8" cy="25.2" r="1.6"/>`,
};

const BEAKS = {
  open: `<path class="beak" d="M29 33.5h6l-3 5.5z"/><path class="beak-inner" d="M30.4 34.4h3.2l-1.6 2.6z"/>`,
  closed: `<path class="beak" d="M29 33h6l-3 5z"/>`,
};

/** An inline Byte. size: "sm" (avatar), "md", "lg" or "hero". wave: lift a wing now and then. */
export function byte(mood = "idle", label = "Byte the owl", { size = "sm", wave = false } = {}) {
  const wrap = document.createElement("span");
  wrap.className = `byte byte-${size}${wave ? " byte-wave" : ""}`;
  wrap.dataset.mood = mood;
  const a11y = label ? `role="img" aria-label="${label}"` : `aria-hidden="true"`;
  const beak = mood === "cheering" || mood === "surprised" ? BEAKS.open : BEAKS.closed;
  wrap.innerHTML = `<svg class="byte-owl" viewBox="0 0 64 64" ${a11y}>
    <ellipse class="shadow" cx="32" cy="60" rx="15" ry="2.6"/>
    <path class="tuft" d="M14 15 L12 4 L22 11 Z M50 15 L52 4 L42 11 Z"/>
    <path class="wing wing-left" d="M12 30 q-7 9 -3 21 q6 -2 9 -9z"/>
    <path class="wing wing-right" d="M52 30 q7 9 3 21 q-6 -2 -9 -9z"/>
    <path class="body" d="M32 8 C17 8 11 18 11 31 C11 47 20 57 32 57 C44 57 53 47 53 31 C53 18 47 8 32 8 Z"/>
    <path class="belly" d="M32 33 C24 33 20 40 20 46 C20 52 25 56 32 56 C39 56 44 52 44 46 C44 40 40 33 32 33 Z"/>
    <path class="feather" d="M27 42l2 2 2-2M33 42l2 2 2-2M30 47l2 2 2-2"/>
    <circle class="eye-ring" cx="25" cy="27" r="8.4"/><circle class="eye-ring" cx="39" cy="27" r="8.4"/>
    <circle class="eye-white" cx="25" cy="27" r="6.8"/><circle class="eye-white" cx="39" cy="27" r="6.8"/>
    <g class="eyes">${EYES[mood] ?? EYES.idle}</g>
    ${beak}
    <path class="foot" d="M25 56.5q-2 2.5 0 3.5M27 56.5v3.5M37 56.5v3.5M39 56.5q2 2.5 0 3.5"/>
  </svg>`;
  return wrap;
}

/** Byte with a speech bubble beside it. */
export function byteSays(text, { mood = "idle", size = "md", wave = false } = {}) {
  const bubble = document.createElement("div");
  bubble.className = "byte-says";
  const words = document.createElement("div");
  words.className = "speech";
  for (const line of Array.isArray(text) ? text : [text]) {
    const p = document.createElement("p");
    p.append(line);
    words.append(p);
  }
  bubble.append(byte(mood, "", { size, wave }), words);
  return bubble;
}
