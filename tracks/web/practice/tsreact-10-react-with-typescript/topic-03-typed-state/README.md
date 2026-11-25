# Typed state

The yarn picker in `YarnPicker.tsx` doesn't remember anything. Make it work, with typed state.

1. **TODO 1:** `colour` must start as `null` but hold a `Colour` later. Give `useState` a type argument: `useState<Colour | null>(null)`.
2. **TODO 2:** `bag` starts as an empty list of colours. Write `useState<Colour[]>([])`.
3. **TODO 3:** Each colour button calls `setColour` with its colour. The chosen button has `aria-pressed` true (this part is already written).
4. **TODO 4:** `Add to bag` adds the chosen colour to the end of the bag, with a new array. It stays disabled while no colour is chosen.

What the page shows:

- `p.choice`: `Pick a colour` at first, then `Colour: Mint`.
- `p.bag`: `Your bag is empty` at first, then `In your bag: Mint, Rani pink`.

`setColour("Mint")` is a type error until TODO 1 is done: read the error, then fix the type. The preview shows two pickers; check that each keeps its own choice. Press **Submit** to run the checks.
