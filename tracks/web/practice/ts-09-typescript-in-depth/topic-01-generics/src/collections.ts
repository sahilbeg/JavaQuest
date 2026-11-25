// Small typed helpers the catalogue code shares. They work for items, orders, colours: a list of anything.

/** One page of a longer list. Pages are numbered from 1. */
export type Page<T> = {
  items: T[];
  page: number;
  totalPages: number;
};

/** The last element of the list, or undefined for an empty list. */
export function last<T>(list: T[]): T | undefined {
  // TODO 1: return the last element of list (undefined when the list is empty).
  return undefined;
}

/**
 * The list without repeats: keeps the first element for each key, in the original order.
 * keyOf works out an element's key, for example (item) => item.code.
 */
export function uniqueBy<T>(list: T[], keyOf: (item: T) => string): T[] {
  // TODO 2: keep an element only when its key hasn't been seen yet. A Set<string> can remember the keys.
  return [];
}

/** Puts the elements in groups by key. The Map's keys are in the order each key first appears. */
export function groupBy<T>(list: T[], keyOf: (item: T) => string): Map<string, T[]> {
  // TODO 3: make a new Map<string, T[]> and push each element into the array for its key.
  return new Map<string, T[]>();
}

/**
 * Page number `page` (1 is the first page) of the list, with `size` elements per page.
 * totalPages is at least 1, even for an empty list. A page past the end has no items.
 */
export function paginate<T>(list: T[], page: number, size: number): Page<T> {
  // TODO 4: slice out this page's elements, and work out totalPages with Math.ceil.
  return { items: [], page, totalPages: 0 };
}
