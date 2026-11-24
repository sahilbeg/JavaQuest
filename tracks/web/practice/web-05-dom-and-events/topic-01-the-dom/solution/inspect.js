// A small page inspector. Each function gets the element it reads.

/** How many products the list holds: element children only, not the spaces between tags. */
export function countProducts(list) {
  return list.children.length;
}

/** Every product's name, in page order, trimmed, as a real array. */
export function productNames(list) {
  return [...list.children].map((item) => item.textContent.trim());
}

/** { first, last } with the first and last product names, or null when the list is empty. */
export function firstAndLast(list) {
  if (list.firstElementChild === null) {
    return null;
  }
  return {
    first: list.firstElementChild.textContent.trim(),
    last: list.lastElementChild.textContent.trim(),
  };
}

/** The id of the element that holds this one, like "catalogue". */
export function parentId(element) {
  return element.parentElement.id;
}
