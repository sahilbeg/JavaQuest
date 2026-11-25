import { useEffect, useState } from "react";
import { GRAMS_PER_AMIGURUMI, isYarnStock } from "./yarn.ts";
import type { Colour, Load, YarnStock } from "./yarn.ts";

/** Arjun's admin panel: how much yarn of one colour is left, and how many amigurumi it makes. */
export function YarnPanel({ colour }: { colour: Colour }) {
  const [load, setLoad] = useState<Load<YarnStock>>({ status: "loading" });

  // TODO 2: in an effect, write an inner async function that fetches `/api/yarn/${encodeURIComponent(colour)}`.
  //         Store await res.json() as unknown. If isYarnStock says yes, setLoad({ status: "ready", data }).
  // TODO 3: if !res.ok, the request fails, or the shape is wrong, setLoad({ status: "error", message: `Couldn't load ${colour} yarn.` }).
  // TODO 4: start the effect with setLoad({ status: "loading" }) and let ignore = false. Only set state when !ignore,
  //         return () => { ignore = true; }, and list [colour] as the dependency.

  if (load.status === "loading") return <p>Checking {colour} yarn...</p>;
  if (load.status === "error") return <p role="alert">{load.message}</p>;
  const { grams } = load.data;
  return (
    <p>
      {colour}: {grams} g, enough for {Math.floor(grams / GRAMS_PER_AMIGURUMI)} amigurumi
    </p>
  );
}
