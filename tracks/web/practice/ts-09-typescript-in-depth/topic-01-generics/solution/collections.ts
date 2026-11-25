// Small typed helpers the catalogue code shares. They work for items, orders, colours: a list of anything.

/** One page of a longer list. Pages are numbered from 1. */
export type Page<T> = {
  items: T[];
  page: number;
  totalPages: number;
};

/** The last element of the list, or undefined for an empty list. */
export function last<T>(list: T[]): T | undefined {
  return list.at(-1);
}

/**
 * The list without repeats: keeps the first element for each key, in the original order.
 * keyOf works out an element's key, for example (item) => item.code.
 */
export function uniqueBy<T>(list: T[], keyOf: (item: T) => string): T[] {
  const seen = new Set<string>();
  const result: T[] = [];
  for (const item of list) {
    const key = keyOf(item);
    if (!seen.has(key)) {
      seen.add(key);
      result.push(item);
    }
  }
  return result;
}

/** Puts the elements in groups by key. The Map's keys are in the order each key first appears. */
export function groupBy<T>(list: T[], keyOf: (item: T) => string): Map<string, T[]> {
  const groups = new Map<string, T[]>();
  for (const item of list) {
    const key = keyOf(item);
    const group = groups.get(key);
    if (group) {
      group.push(item);
    } else {
      groups.set(key, [item]);
    }
  }
  return groups;
}

/**
 * Page number `page` (1 is the first page) of the list, with `size` elements per page.
 * totalPages is at least 1, even for an empty list. A page past the end has no items.
 */
export function paginate<T>(list: T[], page: number, size: number): Page<T> {
  const start = (page - 1) * size;
  return {
    items: list.slice(start, start + size),
    page,
    totalPages: Math.max(1, Math.ceil(list.length / size)),
  };
}
