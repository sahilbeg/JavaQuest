// Remembers a customer's PIN code and wishlist in this browser.

export const PIN_KEY = "dhaaga:pin";
export const WISHLIST_KEY = "dhaaga:wishlist";

/** Saves the PIN code under PIN_KEY. */
export function savePin(pin) {
  // TODO 1: save pin with localStorage.setItem under PIN_KEY
}

/** The saved PIN code, or "" when none is saved. */
export function loadPin() {
  // TODO 2: read PIN_KEY with getItem, and use ?? to turn null into ""
  return "";
}

/** Saves the array of item ids under WISHLIST_KEY, as JSON text. */
export function saveWishlist(ids) {
  // TODO 3: save JSON.stringify(ids) under WISHLIST_KEY
}

/** The saved array of item ids; [] when nothing is saved or the saved text is broken or not an array. */
export function loadWishlist() {
  // TODO 4: return [] for null; otherwise JSON.parse inside try/catch, and return the value only if Array.isArray says yes
  return [];
}

/** Adds the id to the end of the wishlist, or removes it if it is there. Saves and returns the new array. */
export function toggleWish(id) {
  // TODO 5: load the wishlist, make the new array with includes, filter or a spread, save it and return it
  return [];
}

/** Forgets the PIN and the wishlist, and nothing else. */
export function forgetMe() {
  // TODO 6: remove both keys with localStorage.removeItem
}
