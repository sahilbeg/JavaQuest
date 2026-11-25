// Starts the enhancements. The checks never load this file.
// The preview has no real server, so a pretend one answers POST /cart with the new item count.
import { enhance } from "./enhance.js";

let itemsInCart = 0;

async function pretendServer(url, options) {
  if (url === "/cart" && options.method === "POST") {
    itemsInCart += 1;
    return { ok: true, status: 200, json: async () => ({ count: itemsInCart }) };
  }
  return { ok: false, status: 404, json: async () => ({}) };
}

enhance(document.body, pretendServer);
