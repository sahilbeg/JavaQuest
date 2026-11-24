# JSON: the catalogue as data

Sana's catalogue arrives as JSON text. `src/catalogueData.js` holds a copy of it in `CATALOGUE_JSON`, so you can try your code on it. Change `src/json.js`.

1. **TODO 1:** `parseCatalogue(text)` turns the JSON text into an array of items and returns it. When the text is not valid JSON, throw `new Error("catalogue.json is not valid JSON")` instead of the `SyntaxError`.
2. **TODO 2:** When the text is valid JSON but doesn't hold a list (for example, one object), throw `new Error("catalogue.json should hold a list of items")`.
3. **TODO 3:** `catalogueToJson(items)` returns the items as JSON text for people to read, indented by 2 spaces.
4. **TODO 4:** `stockSnapshot(items)` returns compact JSON (no indent) holding only each item's `id` and `stock`, in that order, like `[{"id":"DC-1042","stock":3}]`.

Press **Submit** in Cadence, or just save the file in your editor. The checks run on their own.
