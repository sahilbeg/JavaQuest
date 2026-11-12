const EYES = {
  idle: `<circle cx="11" cy="13.5" r="1.8" fill="currentColor"/><circle cx="21" cy="13.5" r="1.8" fill="currentColor"/>`,
  thinking: `<circle cx="10.2" cy="12.6" r="1.8" fill="currentColor"/><circle cx="20.2" cy="12.6" r="1.8" fill="currentColor"/>`,
  cheering: `<path d="M8.4 14.2q2.6-3 5.2 0M18.4 14.2q2.6-3 5.2 0" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>`,
  encouraging: `<circle cx="11" cy="13.8" r="1.8" fill="currentColor"/><path d="M18.6 13.8q2.4 1.6 4.8 0" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>`,
};

export function byte(mood = "idle", label = "Byte the owl") {
  const wrap = document.createElement("span");
  wrap.className = "avatar avatar-byte";
  wrap.innerHTML = `<svg class="byte-owl" viewBox="0 0 32 32" role="img" aria-label="${label}">
    <path d="M6 9 L8 3 L12 7 Z M26 9 L24 3 L20 7 Z" fill="currentColor"/>
    <rect x="5" y="6" width="22" height="23" rx="10" fill="currentColor" opacity="0.22"/>
    <circle cx="11" cy="13.5" r="4.6" fill="var(--surface)" stroke="currentColor" stroke-width="1.4"/>
    <circle cx="21" cy="13.5" r="4.6" fill="var(--surface)" stroke="currentColor" stroke-width="1.4"/>
    ${EYES[mood] ?? EYES.idle}
    <path d="M16 17.2 L17.6 19.4 L16 21.6 L14.4 19.4 Z" fill="currentColor"/>
    <path d="M10 25.5q6 3 12 0" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" opacity="0.6"/>
  </svg>`;
  return wrap;
}
