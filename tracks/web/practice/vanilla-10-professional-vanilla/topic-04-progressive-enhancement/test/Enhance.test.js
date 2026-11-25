import { describe, it, expect, beforeEach, vi } from "vitest";
import { enhance } from "../src/enhance.js";

const product = (id, name) => `
  <li class="product" data-name="${name}">
    <h2><a href="/products/${id}">${name}</a></h2>
    <form class="add" action="/cart" method="post">
      <input type="hidden" name="id" value="${id}">
      <button type="submit">Add to cart</button>
    </form>
  </li>`;

beforeEach(() => {
  history.replaceState(null, "", "/");
  document.body.innerHTML = `
    <a id="cart-link" href="/cart">Cart (<span id="cart-count">0</span>)</a>
    <form id="search-form" action="/search" method="get" role="search">
      <label for="q">Search</label>
      <input id="q" name="q" type="search">
      <button type="submit">Search</button>
    </form>
    <p id="status" role="status"></p>
    <ul id="products">
      ${product("DC-1042", "Golu the elephant")}
      ${product("DC-1101", "Marigold coaster set")}
      ${product("DC-1201", "Mint keychain")}
      ${product("DC-1301", "Rani pink tote bag")}
    </ul>`;
});

const $ = (selector) => document.querySelector(selector);
const visibleNames = () => [...document.querySelectorAll(".product")].filter((li) => !li.hidden).map((li) => li.dataset.name);
const addForm = (id) => $(`form.add input[name="id"][value="${id}"]`).closest("form");

/** Sends a form the way the browser would. Returns false when a script stopped the normal page load. */
function submit(form) {
  return form.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
}

function search(text) {
  $("#q").value = text;
  return submit($("#search-form"));
}

/** Lets waiting promises (the pretend server's answers) finish. */
async function settle() {
  for (let i = 0; i < 20; i += 1) {
    await Promise.resolve();
  }
}

const answer = (count) => ({ ok: true, status: 200, json: async () => ({ count }) });

describe("without fetch", () => {
  it("leaves the forms to work the old way", () => {
    enhance(document.body, undefined);
    expect(search("golu"), "with no fetch, the search form should submit normally (no preventDefault)").toBe(true);
    expect(visibleNames().length, "with no fetch, nothing should be hidden").toBe(4);
  });
});

describe("the enhanced search", () => {
  it("filters in place instead of loading a new page", () => {
    enhance(document.body, vi.fn());
    expect(search("golu"), "the search form's submit should be stopped with event.preventDefault()").toBe(false);
    expect(visibleNames(), "only Golu should still show").toEqual(["Golu the elephant"]);
    expect($("#status").textContent, "the status line should say 1 product matches").toBe("1 product matches");
  });

  it("ignores capitals and spaces around the words", () => {
    enhance(document.body, vi.fn());
    search("  MINT ");
    expect(visibleNames(), "'  MINT ' should match the Mint keychain").toEqual(["Mint keychain"]);
  });

  it("counts the matches in the status line", () => {
    enhance(document.body, vi.fn());
    search("o");
    expect($("#status").textContent, "three names contain an o").toBe("3 products match");
    search("zebra");
    expect(visibleNames(), "nothing should show for zebra").toEqual([]);
    expect($("#status").textContent, "the status should say No products match").toBe("No products match");
    search("");
    expect(visibleNames().length, "an empty search should show all four again").toBe(4);
    expect($("#status").textContent, "an empty search should say 4 products match").toBe("4 products match");
  });

  it("keeps the search in the address, so it can be shared", () => {
    enhance(document.body, vi.fn());
    search("golu");
    expect(new URLSearchParams(location.search).get("q"), "the address should end with ?q=golu, like the plain form's").toBe("golu");
  });
});

describe("the enhanced Add to cart", () => {
  it("posts the item to /cart without leaving the page", async () => {
    const fetchFn = vi.fn(async () => answer(3));
    enhance(document.body, fetchFn);
    expect(submit(addForm("DC-1042")), "the add form's submit should be stopped with event.preventDefault()").toBe(false);
    expect(fetchFn.mock.calls.length, "one Add should make one request").toBe(1);
    const [url, options] = fetchFn.mock.calls[0] ?? [];
    expect(new URL(url ?? "", location.href).pathname, "the request should go to the form's action, /cart").toBe("/cart");
    expect(options?.method, "the request should use POST, like the form").toBe("POST");
    expect(options?.body?.get?.("id"), "the body should be new FormData(form), holding id=DC-1042").toBe("DC-1042");
    await settle();
    expect($("#cart-count").textContent, "the cart count should show the server's count, 3").toBe("3");
    expect($("#status").textContent, "the status should say what was added").toBe("Added Golu the elephant to your cart.");
  });

  it("disables the button while the request is on its way", async () => {
    let reply;
    const fetchFn = vi.fn(() => new Promise((resolve) => { reply = resolve; }));
    enhance(document.body, fetchFn);
    const button = addForm("DC-1101").querySelector("button");
    submit(addForm("DC-1101"));
    expect(button.disabled, "the button should be disabled until the server answers, so a double-tap can't add twice").toBe(true);
    reply?.(answer(1));
    await settle();
    expect(button.disabled, "the button should work again once the server has answered").toBe(false);
  });

  it("says sorry when the server answers with an error", async () => {
    enhance(document.body, vi.fn(async () => ({ ok: false, status: 503, json: async () => ({}) })));
    submit(addForm("DC-1201"));
    await settle();
    expect($("#status").textContent, "a 503 answer should show the sorry message").toBe("Sorry, that didn't work. Please try again.");
    expect($("#cart-count").textContent, "the cart count should not change when adding failed").toBe("0");
    expect(addForm("DC-1201").querySelector("button").disabled, "the button should work again after a failure").toBe(false);
  });

  it("says sorry when the network fails", async () => {
    enhance(document.body, vi.fn(async () => { throw new TypeError("Failed to fetch"); }));
    submit(addForm("DC-1301"));
    await settle();
    expect($("#status").textContent, "a failed fetch should show the sorry message").toBe("Sorry, that didn't work. Please try again.");
    expect(addForm("DC-1301").querySelector("button").disabled, "the button should work again after a failure").toBe(false);
  });
});
