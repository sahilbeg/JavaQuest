// Colours, stock per colour and the yarn register, with Map and Set.

export function uniqueColours(items) {
  return [...new Set(items.map((item) => item.colour))];
}

export function stockByColour(items) {
  const totals = new Map();
  for (const item of items) {
    totals.set(item.colour, (totals.get(item.colour) ?? 0) + item.stock);
  }
  return totals;
}

export function soldOutColours(items) {
  const soldOut = [];
  for (const [colour, stock] of stockByColour(items)) {
    if (stock === 0) {
      soldOut.push(colour);
    }
  }
  return soldOut;
}

export function restock(yarn, colour, grams) {
  yarn.set(colour, (yarn.get(colour) ?? 0) + grams);
  return yarn;
}
