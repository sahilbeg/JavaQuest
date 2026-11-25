# Typed lists and keys

Show the yarn shelf in `YarnShelf.tsx`. Its prop is `yarns: readonly Yarn[]` (see `yarn.ts`).

1. **TODO 1:** Work out `shown`: the yarns as they are for `"shelf"`, or a sorted copy with the fewest grams first for `"grams"`. `yarns.sort(...)` is a type error on purpose.
2. **TODO 2:** Work out `total`, all the grams added up.
3. **TODO 3:** Inside `<ul className="yarn-list">`, draw one `<li>` per yarn in `shown`, keyed by its id, reading `Marigold: 120 g`.
4. **TODO 4:** A row with fewer than `LOW_GRAMS` (40) grams has `className="low"`. Other rows have no class.

The two sort buttons and `p.total` are already there. React must not warn about keys, even with two Mint cones. Press **Submit** to run the checks.
