# Typed data fetching

Make Arjun's yarn panel show what the stock API really says, and refuse answers with the wrong shape.

The API: `GET /api/yarn/<colour>` answers JSON like `{ "colour": "Marigold", "grams": 1200 }`. Put the colour in the address with `encodeURIComponent`, so `Rani pink` becomes `/api/yarn/Rani%20pink`.

1. **TODO 1** in `src/yarn.ts`: `isYarnStock(value: unknown): value is YarnStock` returns `true` only for an object (not `null`) whose `colour` is a string and whose `grams` is a number.
2. **TODO 2** in `src/YarnPanel.tsx`: in an effect, fetch the colour's stock with an inner `async` function.
   - A good answer sets `load` to `{ status: "ready", data }`. The panel then shows `Marigold: 1200 g, enough for 30 amigurumi`.
   - Store `res.json()` as `unknown` and check it with `isYarnStock`.
3. **TODO 3:** a response that isn't ok (like a 404), a failed request, or an answer with the wrong shape sets `load` to `{ status: "error", message: "Couldn't load Marigold yarn." }` (with the real colour).
4. **TODO 4:** fetch again when `colour` changes, starting from `{ status: "loading" }`, so the panel says `Checking Indigo yarn...` until Indigo's answer arrives. Return a cleanup so a late answer for an old colour is ignored.

The JSX at the bottom is done.

`src/main.tsx` gives the preview a pretend API: Marigold is slow, Indigo is quick and Mint sends grams as text. The checks use their own pretend `fetch`.
