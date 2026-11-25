import { useState } from "react";
import { createRoot } from "react-dom/client";
import { QuickView } from "./QuickView.tsx";
import { CartNudge } from "./CartNudge.tsx";

function App() {
  const [open, setOpen] = useState<boolean>(false);
  const [count, setCount] = useState<number>(0);
  return (
    <main>
      <h1>Dhaaga &amp; Co.</h1>
      <button onClick={() => setOpen(true)}>Quick view</button>
      <QuickView name="Golu the elephant" paise={45000} open={open} onClose={() => setOpen(false)} />
      <p>Cart: {count} items</p>
      <button onClick={() => setCount(count + 1)}>Add a keychain</button>
      <CartNudge count={count} delayMs={5000} />
    </main>
  );
}

createRoot(document.getElementById("root")!).render(<App />);
