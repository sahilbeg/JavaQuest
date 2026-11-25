import { rupees } from "./money.ts";

export type Colour = "Marigold" | "Indigo" | "Rani pink" | "Mint" | "Off-white";

export interface ItemCardProps {
  name: string;
  paise: number;
  colour: Colour;
  badge?: string;
}

export function ItemCard({ name, paise, colour, badge = "Handmade" }: ItemCardProps) {
  return (
    <article className="item-card">
      <h3>{name}</h3>
      <p className="price">{rupees(paise)}</p>
      <p className="colour">{colour}</p>
      <span className="badge">{badge}</span>
    </article>
  );
}
