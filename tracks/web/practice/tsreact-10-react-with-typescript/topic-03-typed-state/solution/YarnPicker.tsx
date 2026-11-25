import { useState } from "react";

export const COLOURS = ["Marigold", "Indigo", "Rani pink", "Mint", "Off-white"] as const;
export type Colour = (typeof COLOURS)[number];

export function YarnPicker() {
  const [colour, setColour] = useState<Colour | null>(null);
  const [bag, setBag] = useState<Colour[]>([]);

  function addToBag() {
    if (colour !== null) {
      setBag([...bag, colour]);
    }
  }

  return (
    <div className="yarn-picker">
      <div className="colours">
        {COLOURS.map((c) => (
          <button key={c} type="button" aria-pressed={colour === c} onClick={() => setColour(c)}>
            {c}
          </button>
        ))}
      </div>
      <p className="choice">{colour === null ? "Pick a colour" : `Colour: ${colour}`}</p>
      <button type="button" disabled={colour === null} onClick={addToBag}>
        Add to bag
      </button>
      <p className="bag">{bag.length === 0 ? "Your bag is empty" : `In your bag: ${bag.join(", ")}`}</p>
    </div>
  );
}
