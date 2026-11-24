// The quick-view panel's stock line. GET /api/stock/DC-2001 answers { "id": "DC-2001", "stock": 12 }.

export const LOADING = "Checking stock...";
export const FAILED = "Couldn't check stock. Try again.";

/** Already written: the text for a stock number. */
export function stockText(stock) {
  return stock === 0 ? "Sold out" : `${stock} in stock`;
}

/**
 * Returns show(id), which asks for one item's stock and writes it into el.
 * el.dataset.state is "loading" while waiting, "ready" with an answer, and "error" when the newest request failed.
 */
export function createStockView(el) {
  let latest = 0;
  let controller = null;

  return async function show(id) {
    latest = latest + 1;
    const mine = latest;
    controller?.abort();
    controller = new AbortController();
    el.textContent = LOADING;
    el.dataset.state = "loading";

    try {
      const response = await fetch(`/api/stock/${id}`, { signal: controller.signal });
      if (!response.ok) {
        throw new Error(`Stock check failed: ${response.status}`);
      }
      const data = await response.json();
      if (mine !== latest) return;
      el.textContent = stockText(data.stock);
      el.dataset.state = "ready";
    } catch (error) {
      if (mine !== latest || error.name === "AbortError") return;
      el.textContent = FAILED;
      el.dataset.state = "error";
    }
  };
}
