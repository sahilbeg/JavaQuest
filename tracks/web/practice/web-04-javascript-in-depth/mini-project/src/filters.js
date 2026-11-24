// The engine behind the catalogue controls: colour, price range, in stock, search and sort.

export function filterCatalogue(items, options = {}) {
  const { colour = "", minPaise, maxPaise, inStockOnly = false, search = "", sort } = options;
  let result = items.slice();

  // TODO 1: when colour isn't "", keep items whose colour matches it, ignoring capital letters
  // TODO 2: when minPaise is not undefined, keep paise >= minPaise. Do the same for maxPaise with <=.
  // TODO 3: when inStockOnly is true, keep items with stock above 0
  // TODO 4: trim and lower-case search. When it isn't "", keep items whose lower-cased name includes it.
  // TODO 5: sort result by paise for "price-asc" (cheapest first) or "price-desc" (dearest first)

  return result;
}

export function colourOptions(items) {
  // TODO 6: each colour once (a Set), then sorted A to Z
  return [];
}

export function priceRange(items) {
  // TODO 7: return null for an empty list, otherwise { minPaise, maxPaise } of all the prices
  return null;
}
