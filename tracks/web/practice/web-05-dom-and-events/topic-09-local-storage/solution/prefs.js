// Remembers a customer's PIN code and wishlist in this browser.

export const PIN_KEY = "dhaaga:pin";
export const WISHLIST_KEY = "dhaaga:wishlist";

/** Saves the PIN code under PIN_KEY. */
export function savePin(pin) {
  localStorage.setItem(PIN_KEY, pin);
}

/** The saved PIN code, or "" when none is saved. */
export function loadPin() {
  return localStorage.getItem(PIN_KEY) ?? "";
}

/** Saves the array of item ids under WISHLIST_KEY, as JSON text. */
export function saveWishlist(ids) {
  localStorage.setItem(WISHLIST_KEY, JSON.stringify(ids));
}

/** The saved array of item ids; [] when nothing is saved or the saved text is broken or not an array. */
export function loadWishlist() {
  const saved = localStorage.getItem(WISHLIST_KEY);
  if (saved === null) {
    return [];
  }
  try {
    const ids = JSON.parse(saved);
    return Array.isArray(ids) ? ids : [];
  } catch {
    return [];
  }
}

/** Adds the id to the end of the wishlist, or removes it if it is there. Saves and returns the new array. */
export function toggleWish(id) {
  const ids = loadWishlist();
  const next = ids.includes(id) ? ids.filter((saved) => saved !== id) : [...ids, id];
  saveWishlist(next);
  return next;
}

/** Forgets the PIN and the wishlist, and nothing else. */
export function forgetMe() {
  localStorage.removeItem(PIN_KEY);
  localStorage.removeItem(WISHLIST_KEY);
}
