// The WhatsApp basket: an array of item codes, like ["KEY", "CST", "KEY"].

export function hasItem(basket, code) {
  return basket.includes(code);
}

export function countOf(basket, code) {
  let count = 0;
  for (const item of basket) {
    if (item === code) {
      count = count + 1;
    }
  }
  return count;
}

export function addItem(basket, code) {
  basket.push(code);
  return basket;
}

export function codesLine(basket) {
  if (basket.length === 0) {
    return "Basket is empty";
  }
  return basket.join(", ");
}
