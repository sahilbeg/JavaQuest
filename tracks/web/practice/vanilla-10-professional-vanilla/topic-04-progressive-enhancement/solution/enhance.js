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
    event.preventDefault();
    const text = searchInput.value.trim();
    const words = text.toLowerCase();
    let shown = 0;
    for (const product of products) {
      const match = product.dataset.name.toLowerCase().includes(words);
      product.hidden = !match;
      if (match) {
        shown += 1;
      }
    }
    status.textContent = matchMessage(shown);
    history.replaceState(null, "", `?q=${encodeURIComponent(text)}`);
  });

  root.addEventListener("submit", async (event) => {
    const form = event.target.closest("form.add");
    if (form === null) {
      return;
    }
    event.preventDefault();
    const button = form.querySelector("button");
    const name = form.closest(".product").dataset.name;
    button.disabled = true;
    try {
      const response = await fetchFn(form.getAttribute("action"), { method: "POST", body: new FormData(form) });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      const data = await response.json();
      cartCount.textContent = String(data.count);
      status.textContent = `Added ${name} to your cart.`;
    } catch {
      status.textContent = "Sorry, that didn't work. Please try again.";
    } finally {
      button.disabled = false;
    }
  });
}
