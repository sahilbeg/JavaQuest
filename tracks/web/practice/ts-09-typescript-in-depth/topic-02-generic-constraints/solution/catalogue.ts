// Generic helpers for any list whose elements have the right fields: items, bundles, cart lines.

/** The element with the fewest paise, or undefined for an empty list. On a tie, the first one wins. */
export function cheapest<T extends { paise: number }>(list: T[]): T | undefined {
  let best: T | undefined;
  for (const item of list) {
    if (best === undefined || item.paise < best.paise) {
      best = item;
    }
  }
  return best;
}

/** Every element's value for one key, in order. pluck(items, "id") gives the ids. */
export function pluck<T, K extends keyof T>(list: T[], key: K): T[K][] {
  return list.map((item) => item[key]);
}

/** The first element whose `key` field equals `value` (with ===), or undefined when none does. */
export function findBy<T, K extends keyof T>(list: T[], key: K, value: T[K]): T | undefined {
  return list.find((item) => item[key] === value);
}

/** paise times qty, added up over every line. 0 for no lines. */
export function linesTotal<T extends { paise: number; qty: number }>(lines: T[]): number {
  return lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
}
