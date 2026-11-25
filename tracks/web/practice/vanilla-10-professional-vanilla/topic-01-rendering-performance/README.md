# Fast rendering: fragments, batching and requestAnimationFrame

The catalogue works, but it stutters on slow phones. Make it do less work for the same result. Change `src/render.js`. `productItem(product)` is ready: it builds one card.

1. **TODO 1:** `renderList(list, products)` must put all the cards into the list as **one** change to the page. Build every `productItem` into a `DocumentFragment`, then call `list.replaceChildren(fragment)`. Old cards go, new cards come, in one step. An empty `products` array leaves the list empty.
2. **TODO 2:** `equalHeights(cards)` takes an **array** of cards and makes every card as tall as the tallest. Do it in three passes, so the browser lays out only once:
   - clear every card's height: `card.style.height = ""`;
   - read every card's `offsetHeight` into an array;
   - write the tallest to every card, like `"180px"`.
   Never read `offsetHeight` while any card still has a height set.
3. **TODO 3:** `createRenderScheduler(draw, raf)` returns `schedule(state)`. Calling `schedule` doesn't draw. It remembers the newest state and asks `raf` for a frame, but only if no frame is waiting yet. When the frame runs, it draws once with the newest state. After that, the next `schedule` asks for a new frame. In the page, `raf` is `requestAnimationFrame`; the checks pass a pretend one.

The checks count changes to the list with a `MutationObserver`, watch every `offsetHeight` read, and run pretend frames. In the preview, type in the search box: the list should follow your typing smoothly.
