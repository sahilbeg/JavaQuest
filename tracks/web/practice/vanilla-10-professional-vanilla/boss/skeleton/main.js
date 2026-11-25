// Shows the three rounds in the preview. The checks never load this file.
import { promiseAll } from "./promiseAll.js";
import { createStarRating } from "./starRating.js";
import { createCachedFetcher } from "./cachedFetch.js";

// Round 1: three stock answers that arrive out of order, plus a plain number.
const later = (value, ms) => new Promise((resolve) => setTimeout(() => resolve(value), ms));
const allResult = document.querySelector("#all-result");
promiseAll([later(7, 300), later(0, 100), later(12, 200), 45000]).then(
  (results) => {
    allResult.textContent = `promiseAll gave ${JSON.stringify(results)} (should be [7,0,12,45000])`;
  },
  (error) => {
    allResult.textContent = `promiseAll failed: ${error.message}`;
  },
);

// Round 2: the star rating.
const ratingStatus = document.querySelector("#rating-status");
createStarRating(document.querySelector("#rating"), {
  label: "Rate Golu the elephant",
  onChange: (stars) => {
    ratingStatus.textContent = `You rated Golu ${stars} out of 5.`;
  },
});

// Round 3: a pretend stock API behind the cache.
let requests = 0;
const stock = createCachedFetcher(async (url) => {
  requests += 1;
  return { url, stock: 7 };
});
const cacheStatus = document.querySelector("#cache-status");
document.querySelector("#ask").addEventListener("click", async () => {
  const data = await stock.get("/api/stock/DC-1042");
  cacheStatus.textContent = `Golu: ${data.stock} in stock. Requests to the server so far: ${requests}.`;
});
