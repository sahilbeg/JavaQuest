// Dhaaga's catalogue, kept in memory, in catalogue order. costPaise is what the yarn and time cost us: never show it to customers.
const ITEMS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, costPaise: 21000, stock: 7 },
  { id: "DC-1101", name: "Marigold coaster set", paise: 15000, costPaise: 6000, stock: 12 },
  { id: "DC-1205", name: "Indigo keychain", paise: 12000, costPaise: 4000, stock: 0 },
  { id: "DC-1301", name: "Tote bag", paise: 89900, costPaise: 41000, stock: 2 },
  { id: "DC-1402", name: "Baby booties", paise: 35000, costPaise: 15000, stock: 4 },
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
