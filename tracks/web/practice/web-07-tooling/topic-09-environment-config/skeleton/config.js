// The site's settings, read once from an env object like Vite's import.meta.env.
// Every value in env is text, or missing.

export const DEFAULTS = {
  apiUrl: "http://localhost:3000",
  freeDeliveryAbovePaise: 99900,
  showDiwaliOffer: false,
};

/** Finished: the trimmed text of a setting, or undefined when it is missing or blank. */
export function read(env, name) {
  const value = env[name];
  if (typeof value !== "string" || value.trim() === "") return undefined;
  return value.trim();
}

export function loadConfig(env) {
  const mode = read(env, "MODE") ?? "development";

  // TODO 1: read VITE_API_URL. Missing in "production": throw an Error naming VITE_API_URL.
  //         Missing anywhere else: use DEFAULTS.apiUrl. Then remove any trailing "/".
  const apiUrl = DEFAULTS.apiUrl;

  // TODO 2: read VITE_FREE_DELIVERY_ABOVE. Digits only (/^\d+$/) becomes a Number.
  //         Missing: DEFAULTS.freeDeliveryAbovePaise. Anything else: throw an Error naming VITE_FREE_DELIVERY_ABOVE.
  const freeDeliveryAbovePaise = DEFAULTS.freeDeliveryAbovePaise;

  // TODO 3: read VITE_SHOW_DIWALI. "true" is true, "false" is false, missing is false.
  //         Anything else: throw an Error naming VITE_SHOW_DIWALI.
  const showDiwaliOffer = DEFAULTS.showDiwaliOffer;

  // TODO 4: freeze the object with Object.freeze before returning it.
  return { mode, apiUrl, freeDeliveryAbovePaise, showDiwaliOffer };
}
