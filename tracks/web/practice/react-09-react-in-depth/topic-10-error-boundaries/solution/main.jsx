import { useState } from "react";
import { createRoot } from "react-dom/client";
import { ItemPage } from "./ItemPage.jsx";

const GOOD_REVIEWS = [{ id: "r1", author: "Asha", stars: 5, text: "So soft! My niece won't let go of him." }];
const NO_TEXT = { id: "r2", author: "Vikram", stars: 4, text: null };

function App() {
  const [reviews, setReviews] = useState(GOOD_REVIEWS);
  return (
    <>
      <button onClick={() => setReviews([...GOOD_REVIEWS, NO_TEXT])}>Post Vikram's review (stars, no text)</button>
      <ItemPage reviews={reviews} />
    </>
  );
}

createRoot(document.getElementById("root")).render(<App />);
