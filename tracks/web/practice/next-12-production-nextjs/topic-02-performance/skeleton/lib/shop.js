// Dhaaga's shop data, kept in memory. In production each of these calls takes about 600 ms.
const ITEMS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, description: "A small crocheted elephant in Indigo and Marigold, modelled on our Golu." },
  { id: "DC-1101", name: "Marigold coaster set", paise: 15000, description: "Four round coasters in Marigold cotton." },
  { id: "DC-1205", name: "Indigo keychain", paise: 12000, description: "A tiny Indigo flower on a steel ring." },
  { id: "DC-1301", name: "Tote bag", paise: 89900, description: "A strong Off-white tote with Rani pink straps." },
  { id: "DC-1402", name: "Baby booties", paise: 35000, description: "Soft Mint booties for the first six months." },
];

const STOCK = { "DC-1042": 7, "DC-1101": 12, "DC-1205": 0, "DC-1301": 2, "DC-1402": 4 };

const REVIEWS = {
  "DC-1042": [
    { by: "Asha", stars: 5, text: "Softer than I hoped. My niece sleeps with him." },
    { by: "Imran", stars: 4, text: "Lovely, but smaller than I thought." },
  ],
};

/** Every item, in catalogue order: { id, name, paise }. */
export async function listItems() {
  return ITEMS.map(({ id, name, paise }) => ({ id, name, paise }));
}

/** One item by id, or null. */
export async function getItem(id) {
  const item = ITEMS.find((candidate) => candidate.id === id);
  return item ? { ...item } : null;
}

/** The reviews for one item (an empty list when it has none). */
export async function getReviews(id) {
  return (REVIEWS[id] ?? []).map((review) => ({ ...review }));
}

/** How many of one item are in stock right now. */
export async function getStock(id) {
  return STOCK[id] ?? 0;
}
