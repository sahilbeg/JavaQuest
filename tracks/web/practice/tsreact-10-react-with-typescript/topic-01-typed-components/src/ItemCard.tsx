import { rupees } from "./money.ts";

export type Colour = "Marigold" | "Indigo" | "Rani pink" | "Mint" | "Off-white";

export interface ItemCardProps {
  name: string;
  paise: number;
  colour: Colour;
  // TODO 1: Add an optional prop called badge that holds text.
}

// TODO 2: Also take badge from the props, with "Handmade" as its default value.
export function ItemCard({ name, paise, colour }: ItemCardProps) {
  return (
    <article className="item-card">
      <h3>{name}</h3>
      {/* TODO 3: Show the price in rupees with rupees(paise), not the raw paise. */}
      <p className="price">{paise}</p>
      <p className="colour">{colour}</p>
      {/* TODO 4: Show the badge in <span className="badge">. */}
    </article>
  );
}
