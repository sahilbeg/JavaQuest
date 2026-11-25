import { useEffect, useState } from "react";
import { GRAMS_PER_AMIGURUMI, isYarnStock } from "./yarn.ts";
import type { Colour, Load, YarnStock } from "./yarn.ts";

/** Arjun's admin panel: how much yarn of one colour is left, and how many amigurumi it makes. */
export function YarnPanel({ colour }: { colour: Colour }) {
  const [load, setLoad] = useState<Load<YarnStock>>({ status: "loading" });

  useEffect(() => {
    let ignore = false;
    setLoad({ status: "loading" });

    async function loadYarn() {
      try {
        const res = await fetch(`/api/yarn/${encodeURIComponent(colour)}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data: unknown = await res.json();
        if (!isYarnStock(data)) throw new Error("Unexpected answer");
        if (!ignore) setLoad({ status: "ready", data });
      } catch {
        if (!ignore) setLoad({ status: "error", message: `Couldn't load ${colour} yarn.` });
      }
    }

    loadYarn();
    return () => {
      ignore = true;
    };
  }, [colour]);

  if (load.status === "loading") return <p>Checking {colour} yarn...</p>;
  if (load.status === "error") return <p role="alert">{load.message}</p>;
  const { grams } = load.data;
  return (
    <p>
      {colour}: {grams} g, enough for {Math.floor(grams / GRAMS_PER_AMIGURUMI)} amigurumi
    </p>
  );
}
