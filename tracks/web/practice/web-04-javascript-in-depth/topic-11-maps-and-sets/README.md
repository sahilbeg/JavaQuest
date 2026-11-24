# Map and Set

The colour buttons list Indigo twice, and the page can't tell which colours are sold out. Fix it in `src/stockroom.js`.

Items look like `{ id: "DC-1042", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 }`.

1. **TODO 1:** `uniqueColours(items)` returns an array of colours, each once, in the order they first appear. Use a `Set`.
2. **TODO 2:** `stockByColour(items)` returns a `Map` from each colour to the total stock of that colour, in the order colours first appear.
3. **TODO 3:** `soldOutColours(items)` returns an array of the colours whose total stock is 0, in the same order. Use `stockByColour`.
4. **TODO 4:** `restock(yarn, colour, grams)` adds `grams` to that colour in the `yarn` Map (colour to grams). A colour that isn't in the Map yet starts from 0. It changes the Map it was given and returns it.

Press **Submit** in Cadence, or just save the file in your editor. The checks run on their own.
