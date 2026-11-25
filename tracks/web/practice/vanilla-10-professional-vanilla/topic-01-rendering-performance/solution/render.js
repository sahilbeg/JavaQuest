// Fast drawing for the catalogue: one change to the page per redraw, reads before writes, one draw per frame.
import { rupees } from "./money.js";

/** One product card: <li class="product" data-id="DC-1042"><h3>Golu the elephant</h3><p class="price">₹450.00</p></li>. */
export function productItem(product) {
  const li = document.createElement("li");
  li.className = "product";
  li.dataset.id = product.id;
  const name = document.createElement("h3");
  name.textContent = product.name;
  const price = document.createElement("p");
  price.className = "price";
  price.textContent = rupees(product.paise);
  li.append(name, price);
  return li;
}

/** Draws every product into the list, replacing what was there, as one change to the page. */
export function renderList(list, products) {
  const fragment = document.createDocumentFragment();
  for (const product of products) {
    fragment.append(productItem(product));
  }
  list.replaceChildren(fragment);
}

/** Makes every card in the array as tall as the tallest: clear all heights, read all heights, then write all heights. */
export function equalHeights(cards) {
  for (const card of cards) {
    card.style.height = "";
  }
  const heights = cards.map((card) => card.offsetHeight);
  const tallest = Math.max(0, ...heights);
  for (const card of cards) {
    card.style.height = `${tallest}px`;
  }
}

/** Returns schedule(state). Many calls before the next frame lead to one draw(state), with the newest state. */
export function createRenderScheduler(draw, raf = (callback) => requestAnimationFrame(callback)) {
  let latest = null;
  let waiting = false;
  return function schedule(state) {
    latest = state;
    if (waiting) {
      return;
    }
    waiting = true;
    raf(() => {
      waiting = false;
      draw(latest);
    });
  };
}
