// Reading and writing the catalogue as JSON text.

export function parseCatalogue(text) {
  // TODO 1: JSON.parse the text inside try. If it throws, throw new Error("catalogue.json is not valid JSON").
  // TODO 2: if the result is not an array (Array.isArray), throw new Error("catalogue.json should hold a list of items").
  return [];
}

export function catalogueToJson(items) {
  // TODO 3: return the items as JSON text, indented by 2 spaces
  return "";
}

export function stockSnapshot(items) {
  // TODO 4: return compact JSON of [{ id, stock }, ...], one object per item
  return "";
}
