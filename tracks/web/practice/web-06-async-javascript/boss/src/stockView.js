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
  // TODO 1 (bronze): keep a counter here for the newest request.
  // TODO 3 (silver): keep the current AbortController here too (start with null).

  return async function show(id) {
    // TODO 2 (bronze): add one to the counter and remember this request's own number.
    // TODO 4 (silver): abort the previous request, make a new AbortController, and pass { signal } to fetch.
    // TODO 5 (gold): before asking, show LOADING and set el.dataset.state = "loading".

    const response = await fetch(`/api/stock/${id}`);
    const data = await response.json();

    // TODO 2 (bronze): if a newer request has started since, return without touching el.
    el.textContent = stockText(data.stock);
    el.dataset.state = "ready";

    // TODO 6 (gold): wrap the request in try...catch and throw when response.ok is false.
    //   In catch: return quietly if this request is old or was aborted (error.name === "AbortError").
    //   Otherwise show FAILED and set el.dataset.state = "error". show must never throw.
  };
}
