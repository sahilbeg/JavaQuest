import { useEffect, useState } from "react";

/** The stock badge on an item's page during the sale: "DC-1042: 7 left". */
export function SaleStock({ itemId }) {
  const [left, setLeft] = useState(null);

  useEffect(() => {
    let ignore = false;
    setLeft(null);
    fetch(`/api/stock/${itemId}`)
      .then((res) => res.json())
      .then((data) => {
        if (!ignore) setLeft(data.left);
      });
    return () => {
      ignore = true;
    };
  }, [itemId]);

  if (left === null) return <p>Checking stock for {itemId}...</p>;
  if (left === 0) return <p>{itemId} is sold out</p>;
  return (
    <p>
      {itemId}: {left} left
    </p>
  );
}
