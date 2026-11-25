import { useState } from "react";

export const COLOURS = ["Marigold", "Indigo", "Rani pink", "Mint", "Off-white"] as const;
export type Colour = (typeof COLOURS)[number];

export function YarnPicker() {
  // TODO 1: This state's type is inferred as null, so it can never hold a colour.
  //         Give useState a type argument: useState<Colour | null>(null).
  const [colour, setColour] = useState(null);

  // TODO 2: An empty array is inferred as never[]. Make it a list of colours: useState<Colour[]>([]).
  const [bag, setBag] = useState([]);

  return (
    <div className="yarn-picker">
      <div className="colours">
        {COLOURS.map((c) => (
          // TODO 3: Give each colour button onClick={() => setColour(c)}.
          <button key={c} type="button" aria-pressed={colour === c}>
            {c}
          </button>
        ))}
      </div>
      <p className="choice">{colour === null ? "Pick a colour" : `Colour: ${colour}`}</p>
      {/* TODO 4: On click, if a colour is chosen, add it to the end of the bag with a NEW array: [...bag, colour]. */}
      <button type="button" disabled={colour === null}>
        Add to bag
      </button>
      <p className="bag">{bag.length === 0 ? "Your bag is empty" : `In your bag: ${bag.join(", ")}`}</p>
    </div>
  );
}
