import { useStoredState } from "./useStoredState.ts";
import { useToggle } from "./useToggle.ts";

export interface WishItem {
  id: string;
  name: string;
}

export const ITEMS: WishItem[] = [
  { id: "DC-1042", name: "Golu the elephant" },
  { id: "DC-2001", name: "Marigold coaster set" },
  { id: "DC-3005", name: "Indigo keychain" },
];

/** Only a list of strings counts as a saved wishlist. */
function isIdList(value: unknown): value is string[] {
  return Array.isArray(value) && value.every((v) => typeof v === "string");
}

/** The hearts on the catalogue, remembered across refreshes. */
export function WishList() {
  const [saved, setSaved] = useStoredState<string[]>("dhaaga-wishlist", [], isIdList);
  const [showSaved, toggleSaved] = useToggle(false);

  function flip(id: string) {
    setSaved(saved.includes(id) ? saved.filter((s) => s !== id) : [...saved, id]);
  }

  return (
    <section>
      <ul>
        {ITEMS.map((item) => (
          <li key={item.id}>
            {item.name}{" "}
            <button aria-pressed={saved.includes(item.id)} onClick={() => flip(item.id)}>
              Save {item.name}
            </button>
          </li>
        ))}
      </ul>
      <button onClick={toggleSaved}>{showSaved ? "Hide saved" : "Show saved"}</button>
      {showSaved && <p>Saved: {saved.length === 0 ? "nothing yet" : saved.join(", ")}</p>}
    </section>
  );
}
