// Colours, stock per colour and the yarn register, with Map and Set.

export function uniqueColours(items) {
  // TODO 1: put every item's colour into a new Set, then spread it back into an array
  return [];
}

export function stockByColour(items) {
  const totals = new Map();
  // TODO 2: for each item, add its stock to the total for its colour (a missing colour starts at 0)
  return totals;
}

export function soldOutColours(items) {
  const soldOut = [];
  // TODO 3: loop over the entries of stockByColour(items) and push each colour whose total is 0
  return soldOut;
}

export function restock(yarn, colour, grams) {
  // TODO 4: set colour to its current grams (or 0 when missing) plus grams
  return yarn;
}
