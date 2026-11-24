import { useState } from "react";
import { createRoot } from "react-dom/client";
import { CartTitle } from "./CartTitle.jsx";
import { SaleCountdown } from "./SaleCountdown.jsx";

function App() {
  const [count, setCount] = useState(0);
  return (
    <main>
      <h1>Dhaaga &amp; Co.</h1>
      <SaleCountdown seconds={30} />
      <CartTitle count={count} />
      <button onClick={() => setCount(count + 1)}>Add a coaster set</button>
    </main>
  );
}

createRoot(document.getElementById("root")).render(<App />);
