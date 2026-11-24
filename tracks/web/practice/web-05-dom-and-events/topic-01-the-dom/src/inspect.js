// A small page inspector. Each function gets the element it reads.

/** How many products the list holds: element children only, not the spaces between tags. */
export function countProducts(list) {
  // TODO 1: return how many element children the list has
  return 0;
}

/** Every product's name, in page order, trimmed, as a real array. */
export function productNames(list) {
  // TODO 2: spread list.children into an array and map each item to its trimmed textContent
  return [];
}

/** { first, last } with the first and last product names, or null when the list is empty. */
export function firstAndLast(list) {
  // TODO 3: use firstElementChild and lastElementChild; return null when there is no first child
  return null;
}

/** The id of the element that holds this one, like "catalogue". */
export function parentId(element) {
  // TODO 4: return the id of the element's parent element
  return "";
}
