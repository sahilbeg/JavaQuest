/** The widths, in pixels, that every product photo is saved at on the server. */
export const PHOTO_WIDTHS = [400, 800];

/** Where one size of a product's photo lives: photoUrl("DC-1042", 400) gives "/img/DC-1042-400.webp". */
export function photoUrl(id, width) {
  return `/img/${id}-${width}.webp`;
}

/** Every size of a product's photo, for srcset: "/img/DC-1042-400.webp 400w, /img/DC-1042-800.webp 800w". */
export function photoSrcSet(id) {
  // TODO 1: for each width in PHOTO_WIDTHS, make "<photoUrl> <width>w", and join them with ", ".
  return "";
}
