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
  // TODO 1: build every productItem into one DocumentFragment, then put it in the list with list.replaceChildren(fragment)
  list.innerHTML = "";
  for (const product of products) {
    list.appendChild(productItem(product));
  }
}

/** Makes every card in the array as tall as the tallest: clear all heights, read all heights, then write all heights. */
export function equalHeights(cards) {
  // TODO 2: clear every card's style.height in one loop, read every offsetHeight in a second, then write the tallest in a third
  let tallest = 0;
  for (const card of cards) {
    card.style.height = "";
    tallest = Math.max(tallest, card.offsetHeight);
  }
  for (const card of cards) {
    card.style.height = `${tallest}px`;
  }
}

/** Returns schedule(state). Many calls before the next frame lead to one draw(state), with the newest state. */
export function createRenderScheduler(draw, raf = (callback) => requestAnimationFrame(callback)) {
  // TODO 3: remember the newest state; ask raf for a frame only when none is waiting; the frame clears the flag and draws once
  return function schedule(state) {
    draw(state);
  };
}
