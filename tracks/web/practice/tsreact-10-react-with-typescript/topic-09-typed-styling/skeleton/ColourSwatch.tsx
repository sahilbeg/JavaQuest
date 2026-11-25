import type { CSSProperties } from "react";

export type Colour = "Marigold" | "Indigo" | "Rani pink" | "Mint" | "Off-white";

// Sana's exact yarn colours. A Record must have one entry for every Colour.
export const SWATCH_HEX: Record<Colour, string> = {
  Marigold: "#F2A81D",
  Indigo: "#2E3A8C",
  "Rani pink": "#E0218A",
  Mint: "#98D7C2",
  "Off-white": "#FAF7F0",
};

export interface ColourSwatchProps {
  colour: Colour;
  size?: number;
}

export function ColourSwatch({ colour, size = 24 }: ColourSwatchProps) {
  // TODO 4: Make a style object typed CSSProperties: backgroundColor from SWATCH_HEX[colour],
  //         and width and height set to size. Then pass it with style={style}.
  return <span className="swatch" role="img" aria-label={colour} />;
}
