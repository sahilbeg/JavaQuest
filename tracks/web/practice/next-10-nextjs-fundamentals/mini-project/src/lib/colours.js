/** The five yarn colours. The slug is how a colour is written in an address. */
export const COLOURS = [
  { slug: "marigold", name: "Marigold" },
  { slug: "indigo", name: "Indigo" },
  { slug: "rani-pink", name: "Rani pink" },
  { slug: "mint", name: "Mint" },
  { slug: "off-white", name: "Off-white" },
];

/** The colour with this slug, or null. */
export function colourBySlug(slug) {
  return COLOURS.find((colour) => colour.slug === slug) ?? null;
}
