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

  let apiUrl = read(env, "VITE_API_URL");
  if (apiUrl === undefined) {
    if (mode === "production") {
      throw new Error("VITE_API_URL is missing: the live site cannot take orders without it");
    }
    apiUrl = DEFAULTS.apiUrl;
  }
  apiUrl = apiUrl.replace(/\/+$/, "");

  const limit = read(env, "VITE_FREE_DELIVERY_ABOVE");
  if (limit !== undefined && !/^\d+$/.test(limit)) {
    throw new Error(`VITE_FREE_DELIVERY_ABOVE must be whole paise, like 99900, not "${limit}"`);
  }
  const freeDeliveryAbovePaise = limit === undefined ? DEFAULTS.freeDeliveryAbovePaise : Number(limit);

  const flag = read(env, "VITE_SHOW_DIWALI");
  if (flag !== undefined && flag !== "true" && flag !== "false") {
    throw new Error(`VITE_SHOW_DIWALI must be "true" or "false", not "${flag}"`);
  }
  const showDiwaliOffer = flag === "true";

  return Object.freeze({ mode, apiUrl, freeDeliveryAbovePaise, showDiwaliOffer });
}
