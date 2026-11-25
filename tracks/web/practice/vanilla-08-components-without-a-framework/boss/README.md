# INC-08: The product grid redraws and loses what customers typed

The sale page calls `grid.update(products)` from `src/grid.js` on every stock refresh. Each refresh wipes the "Name to stitch on" boxes, and some taps on Add to cart count twice. Fix the grid without changing how the cards look.

A product looks like `{ id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 4 }`. `createGrid(root, onAdd)` draws into `root` (a `<ul>`) and returns `{ update }`. Each card is an `<li class="card" data-id="...">`, and pressing its Add to cart button calls `onAdd({ id, stitch })` with the name typed on that card, trimmed.

1. **Bronze:** after a refresh, a name typed into any card's `.card__stitch-input` is still there.
2. **Silver:** a refresh keeps the very same `<li>` for every product that is still in the data, and updates its `.card__stock` text and its button's `disabled`. New products appear at their place, products that are gone are removed, and the cards follow the data's order. The box the customer is typing in keeps its focus.
3. **Gold:** one press of Add to cart calls `onAdd` exactly once, with the trimmed name, however many refreshes came before. A product that leaves and later comes back gets a fresh card with an empty box.

The checks type into the boxes, call `update` with new stock, and look at what is still there.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
