// Dhaaga's catalogue, kept in memory. The real store reads a database here.
const ITEMS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 7 },
  { id: "DC-1101", name: "Marigold coaster set", paise: 15000, stock: 12 },
  { id: "DC-1205", name: "Indigo keychain", paise: 12000, stock: 0 },
];

/** One item by id, as a copy, or null when there is no such item. */
export async function getItem(id) {
  const item = ITEMS.find((candidate) => candidate.id === id);
  return item ? { ...item } : null;
}
