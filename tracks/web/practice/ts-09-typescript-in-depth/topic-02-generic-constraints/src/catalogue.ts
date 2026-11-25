// Generic helpers for any list whose elements have the right fields: items, bundles, cart lines.

/** The element with the fewest paise, or undefined for an empty list. On a tie, the first one wins. */
export function cheapest<T extends { paise: number }>(list: T[]): T | undefined {
  // TODO 1: remember the best element so far (type T | undefined) and replace it when an element is cheaper.
  return undefined;
}

/** Every element's value for one key, in order. pluck(items, "id") gives the ids. */
export function pluck<T, K extends keyof T>(list: T[], key: K): T[K][] {
  // TODO 2: map each element to element[key].
  return [];
}

/** The first element whose `key` field equals `value` (with ===), or undefined when none does. */
export function findBy<T, K extends keyof T>(list: T[], key: K, value: T[K]): T | undefined {
  // TODO 3: find the first element where element[key] === value.
  return undefined;
}

/** paise times qty, added up over every line. 0 for no lines. */
export function linesTotal<T extends { paise: number; qty: number }>(lines: T[]): number {
  // TODO 4: add up line.paise * line.qty for every line.
  return 0;
}
