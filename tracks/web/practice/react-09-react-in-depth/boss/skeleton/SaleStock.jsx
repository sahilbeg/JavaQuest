import { useEffect, useState } from "react";

/** The stock badge on an item's page during the sale: "DC-1042: 7 left". */
export function SaleStock({ itemId }) {
  const [left, setLeft] = useState(null);

  // TODO 1 (bronze): one page view must send exactly one request.
  // TODO 2 (silver): a different item needs one new request; the same item needs none.
  // TODO 3 (gold): never show one item's stock under another item's id, even when answers arrive late.
  useEffect(() => {
    fetch(`/api/stock/${itemId}`)
      .then((res) => res.json())
      .then((data) => setLeft(data.left));
  });

  if (left === null) return <p>Checking stock for {itemId}...</p>;
  if (left === 0) return <p>{itemId} is sold out</p>;
  return (
    <p>
      {itemId}: {left} left
    </p>
  );
}
