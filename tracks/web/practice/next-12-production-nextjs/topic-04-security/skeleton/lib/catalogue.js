// Dhaaga's catalogue, kept in memory. The server's prices are the only prices that count.
const ITEMS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1101", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-1205", name: "Indigo keychain", paise: 12000 },
];

/** One item by id, as a copy, or null. */
export async function getItem(id) {
  const item = ITEMS.find((candidate) => candidate.id === id);
  return item ? { ...item } : null;
}
