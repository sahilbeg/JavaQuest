// Two slow reports. In the shop they take seconds; the checks replace them with pretend ones.
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

/** Today's orders and takings, like { orders: 12, totalPaise: 845000 }. Slow. */
export async function getTodaySales() {
  await wait(8000);
  return { orders: 12, totalPaise: 845000 };
}

/** Items with 3 or fewer left, like [{ id: "DC-1042", name: "Golu the elephant", left: 2 }]. */
export async function getLowStock() {
  await wait(2000);
  return [
    { id: "DC-1042", name: "Golu the elephant", left: 2 },
    { id: "DC-3007", name: "Marigold tote bag", left: 3 },
  ];
}
