// Makes the shop page nicer once the script has arrived. Without it, the forms still work by loading pages.

/** "No products match", "1 product matches", "3 products match". */
function matchMessage(count) {
  if (count === 0) {
    return "No products match";
  }
  return count === 1 ? "1 product matches" : `${count} products match`;
}

/** Adds search-in-place and add-without-reload to the page inside root. fetchFn is fetch, or a pretend one. */
export function enhance(root, fetchFn) {
  if (typeof fetchFn !== "function") {
    return;
  }
  const searchForm = root.querySelector("#search-form");
  const searchInput = root.querySelector("#q");
  const status = root.querySelector("#status");
  const cartCount = root.querySelector("#cart-count");
  const products = [...root.querySelectorAll(".product")];

  searchForm.addEventListener("submit", (event) => {
    // TODO 3: stop the page load (event.preventDefault()), then hide every product whose data-name
    //         doesn't include the trimmed, lower-case search words, show the rest, put matchMessage(count)
    //         in the status line, and keep the search in the address with
    //         history.replaceState(null, "", `?q=${encodeURIComponent(text)}`)
  });

  root.addEventListener("submit", async (event) => {
    const form = event.target.closest("form.add");
    if (form === null) {
      return;
    }
    // TODO 4: preventDefault first, disable the form's button, then POST with
    //         fetchFn(form.getAttribute("action"), { method: "POST", body: new FormData(form) }).
    //         When response.ok, read { count } from response.json(): put it in #cart-count and say
    //         "Added <data-name> to your cart." in the status line. When it isn't ok, or fetch throws,
    //         say "Sorry, that didn't work. Please try again." Re-enable the button in finally.
  });
}
