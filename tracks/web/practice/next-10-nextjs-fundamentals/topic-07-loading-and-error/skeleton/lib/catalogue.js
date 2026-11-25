// Dhaaga's catalogue, kept in memory. In the real shop this would be a database.
const ITEMS = [
  { id: "DC-1038", name: "Rani the pink elephant", code: "AMI", paise: 45000, colour: "Rani pink", stock: 4 },
  { id: "DC-1042", name: "Golu the elephant", code: "AMI", paise: 45000, colour: "Indigo", stock: 7 },
  { id: "DC-1043", name: "Marigold coaster set", code: "CST", paise: 15000, colour: "Marigold", stock: 12 },
  { id: "DC-1044", name: "Mint keychain", code: "KEY", paise: 12000, colour: "Mint", stock: 0 },
  { id: "DC-1045", name: "Indigo tote bag", code: "BAG", paise: 89900, colour: "Indigo", stock: 3 },
  { id: "DC-1046", name: "Rani pink baby booties", code: "BTY", paise: 35000, colour: "Rani pink", stock: 5 },
];

/** Every item, in catalogue order. It's async, like a real database call. */
export async function getItems() {
  return ITEMS.map((item) => ({ ...item }));
}

/** One item by its exact id, or null. */
export async function getItem(id) {
  const item = ITEMS.find((it) => it.id === id);
  return item ? { ...item } : null;
}
