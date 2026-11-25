// Dhaaga's catalogue, kept in memory. alt describes each item's photo for people who can't see it.
const ITEMS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, description: "A small crocheted elephant in Indigo and Marigold, modelled on our Golu.", alt: "A small Indigo crocheted elephant with Marigold ears" },
  { id: "DC-1101", name: "Marigold coaster set", paise: 15000, description: "Four round coasters in Marigold cotton.", alt: "Four round Marigold coasters in a stack" },
  { id: "DC-1205", name: "Indigo keychain", paise: 12000, description: "A tiny Indigo flower on a steel ring.", alt: "An Indigo crocheted flower on a key ring" },
  { id: "DC-1301", name: "Tote bag", paise: 89900, description: "A strong Off-white tote with Rani pink straps.", alt: "An Off-white crocheted tote bag with Rani pink straps" },
  { id: "DC-1402", name: "Baby booties", paise: 35000, description: "Soft Mint booties for the first six months.", alt: "A pair of Mint crocheted baby booties" },
];

/** Every item, in catalogue order, as copies. */
export async function listItems() {
  return ITEMS.map((item) => ({ ...item }));
}

/** One item by id, as a copy, or null. */
export async function getItem(id) {
  const item = ITEMS.find((candidate) => candidate.id === id);
  return item ? { ...item } : null;
}
