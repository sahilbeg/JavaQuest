// The engine behind the catalogue controls: colour, price range, in stock, search and sort.

export function filterCatalogue(items, options = {}) {
  const { colour = "", minPaise, maxPaise, inStockOnly = false, search = "", sort } = options;
  let result = items.slice();

  if (colour !== "") {
    const wantedColour = colour.toLowerCase();
    result = result.filter((item) => item.colour.toLowerCase() === wantedColour);
  }
  if (minPaise !== undefined) {
    result = result.filter((item) => item.paise >= minPaise);
  }
  if (maxPaise !== undefined) {
    result = result.filter((item) => item.paise <= maxPaise);
  }
  if (inStockOnly) {
    result = result.filter((item) => item.stock > 0);
  }
  const wanted = search.trim().toLowerCase();
  if (wanted !== "") {
    result = result.filter((item) => item.name.toLowerCase().includes(wanted));
  }
  if (sort === "price-asc") {
    result.sort((a, b) => a.paise - b.paise);
  } else if (sort === "price-desc") {
    result.sort((a, b) => b.paise - a.paise);
  }

  return result;
}

export function colourOptions(items) {
  return [...new Set(items.map((item) => item.colour))].sort();
}

export function priceRange(items) {
  if (items.length === 0) {
    return null;
  }
  const prices = items.map((item) => item.paise);
  return { minPaise: Math.min(...prices), maxPaise: Math.max(...prices) };
}
