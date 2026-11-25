export const ITEMS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-2001", name: "Coaster set", paise: 15000 },
  { id: "DC-3007", name: "Marigold tote bag", paise: 89900 },
  { id: "DC-4410", name: "Baby booties", paise: 35000 },
  { id: "DC-5120", name: "Keychain", paise: 12000 },
];

/** The item with this id, or undefined. */
export function findItem(id) {
  return ITEMS.find((item) => item.id === id);
}
