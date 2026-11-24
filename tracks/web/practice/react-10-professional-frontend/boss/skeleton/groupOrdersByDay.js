/**
 * Round 1. Groups orders by the day they were placed, in India time.
 * Each order is { id, placedAt, paise }, where placedAt looks like "2026-11-08T23:50:00+05:30".
 * Returns [{ day: "2026-11-08", count, totalPaise }], one entry per day, oldest day first.
 *
 * This is the candidate's answer. It passed their one example.
 */
export function groupOrdersByDay(orders) {
  // TODO 1: fix the four bugs: the loop skips an order, each order overwrites its day instead of adding to it,
  // toISOString() moves early-morning orders to the day before (it is UTC), and the days are not sorted.
  const groups = {};
  for (let i = 1; i < orders.length; i++) {
    const order = orders[i];
    const day = new Date(order.placedAt).toISOString().slice(0, 10);
    groups[day] = { day, count: 1, totalPaise: order.paise };
  }
  return Object.values(groups);
}
