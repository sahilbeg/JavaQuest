# INC-09: A render loop floods the API during the sale

`SaleStock` in `src/SaleStock.jsx` is the stock badge on every item page. It asks `GET /api/stock/<itemId>` and shows `DC-1042: 7 left`. During the sale it sent request after request, without stopping. Fix it without changing what it shows.

1. **Bronze:** one page view sends exactly one request, and the badge shows the stock from the answer.
2. **Silver:** rendering again for the same item sends no request. Switching `itemId` to another item sends exactly one new request, and the badge shows the new item's stock.
3. **Gold:** straight after switching, the badge says `Checking stock for DC-2001...`, never the old number under the new id. A slow answer for the old item that arrives late is ignored.

`src/main.jsx` gives the preview a pretend stock API and counts the requests it gets.
