/**
 * Round 1. Groups orders by the day they were placed, in India time.
 * Each order is { id, placedAt, paise }, where placedAt looks like "2026-11-08T23:50:00+05:30".
 * Returns [{ day: "2026-11-08", count, totalPaise }], one entry per day, oldest day first.
 */
export function groupOrdersByDay(orders) {
  const groups = new Map();
  for (const order of orders) {
    const day = order.placedAt.slice(0, 10);
    const group = groups.get(day) ?? { day, count: 0, totalPaise: 0 };
    group.count += 1;
    group.totalPaise += order.paise;
    groups.set(day, group);
  }
  return [...groups.values()].sort((a, b) => (a.day < b.day ? -1 : 1));
}
