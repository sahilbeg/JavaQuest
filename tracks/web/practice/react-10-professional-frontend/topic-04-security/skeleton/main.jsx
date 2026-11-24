import { createRoot } from "react-dom/client";
import { ReviewList } from "./ReviewList.jsx";
import "./style.css";

// Real reviews arrive from strangers. The second one is an attack, written to run code on every visitor's page.
const REVIEWS = [
  { id: "r1", author: "Anjali", rating: 5, text: "Golu is even cuter in real life!", website: "https://instagram.com/anjali.makes" },
  {
    id: "r2",
    author: "<b>Rohan</b>",
    rating: 4,
    text: `Lovely coasters! <img src="nope.png" onerror="this.insertAdjacentText('afterend', ' 😈 This review just ran code on your page.')">`,
    website: "javascript:alert('your cookies')",
  },
  { id: "r3", author: "Farah", rating: 3, text: "The booties were a little big for my niece.", website: "" },
];

createRoot(document.getElementById("root")).render(
  <main>
    <h1>Golu the elephant: reviews</h1>
    <ReviewList reviews={REVIEWS} />
  </main>
);
