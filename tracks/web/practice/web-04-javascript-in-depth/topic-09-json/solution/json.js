// Reading and writing the catalogue as JSON text.

export function parseCatalogue(text) {
  let data;
  try {
    data = JSON.parse(text);
  } catch (error) {
    throw new Error("catalogue.json is not valid JSON");
  }
  if (!Array.isArray(data)) {
    throw new Error("catalogue.json should hold a list of items");
  }
  return data;
}

export function catalogueToJson(items) {
  return JSON.stringify(items, null, 2);
}

export function stockSnapshot(items) {
  return JSON.stringify(items.map(({ id, stock }) => ({ id, stock })));
}
