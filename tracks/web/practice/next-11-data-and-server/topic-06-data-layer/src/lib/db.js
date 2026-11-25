// A pretend database, kept in memory. Only the data layer (orders.js) may use it.
const SEED = [
  { id: "order-17", name: "Asha Rao", phone: "9876543210", lines: [{ itemId: "DC-4410", qty: 1 }], totalPaise: 41000, status: "NEW", upiRef: "UPI-55120" },
  { id: "order-18", name: "Ravi Joshi", phone: "9876543211", lines: [{ itemId: "DC-1042", qty: 1 }], totalPaise: 51000, status: "PACKED", upiRef: "UPI-55121" },
  { id: "order-19", name: "Neha Desai", phone: "9876543212", lines: [{ itemId: "DC-2001", qty: 2 }], totalPaise: 36000, status: "NEW", upiRef: "UPI-55122" },
];

const copy = (row) => JSON.parse(JSON.stringify(row));

let tables = { orders: SEED.map(copy) };
let nextNumber = 20;

/** Puts the database back to how it started. The checks call this before each test. */
export function resetDb() {
  tables = { orders: SEED.map(copy) };
  nextNumber = 20;
}

/** Copies of every row in a table, oldest first. */
export function dbAll(table) {
  return tables[table].map(copy);
}

/** A copy of the row with this id, or undefined. */
export function dbFind(table, id) {
  const row = tables[table].find((r) => r.id === id);
  return row ? copy(row) : undefined;
}

/** Saves a new row. */
export function dbInsert(table, row) {
  tables[table].push(copy(row));
}

/** The next order number: 20, then 21, and so on. */
export function nextOrderNumber() {
  const number = nextNumber;
  nextNumber += 1;
  return number;
}
