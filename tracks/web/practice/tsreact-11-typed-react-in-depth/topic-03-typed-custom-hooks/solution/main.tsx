import { useState } from "react";
import { createRoot } from "react-dom/client";
import { WishList } from "./WishList.tsx";

function App() {
  // Changing the key makes React throw the old WishList away and mount a new one, like a page refresh.
  const [mount, setMount] = useState<number>(0);
  return (
    <main>
      <h1>Your wishlist</h1>
      <WishList key={mount} />
      <button onClick={() => setMount(mount + 1)}>Refresh the list</button>
    </main>
  );
}

createRoot(document.getElementById("root")!).render(<App />);
