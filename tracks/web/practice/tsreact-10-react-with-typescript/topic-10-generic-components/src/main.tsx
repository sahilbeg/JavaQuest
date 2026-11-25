// Starts the page for the preview. The tests never import this file.
import { useState } from "react";
import { createRoot } from "react-dom/client";
import { rupees } from "./money.ts";
import { PickList } from "./PickList.tsx";

const COLOURS: readonly string[] = ["Marigold", "Indigo", "Rani pink", "Mint", "Off-white"];

interface GiftBox {
  id: string;
  label: string;
  paise: number;
}

const BOXES: readonly GiftBox[] = [
  { id: "box-none", label: "No box", paise: 0 },
  { id: "box-s", label: "Small box", paise: 3000 },
  { id: "box-l", label: "Large box", paise: 6000 },
];

function GiftPage() {
  const [colour, setColour] = useState<string | null>(null);
  const [box, setBox] = useState<GiftBox | null>(null);

  return (
    <main>
      {/* Here T is string. */}
      <PickList
        label="Yarn colour"
        options={COLOURS}
        selected={colour}
        getKey={(c) => c}
        renderOption={(c) => c}
        onPick={(c) => setColour(c)}
      />
      {/* Here T is GiftBox, an object. */}
      <PickList
        label="Gift box"
        options={BOXES}
        selected={box}
        getKey={(b) => b.id}
        renderOption={(b) => `${b.label}, ${rupees(b.paise)}`}
        onPick={(b) => setBox(b)}
      />
      <p>
        {colour ?? "No colour yet"}, {box?.label ?? "no box yet"}
      </p>
    </main>
  );
}

const root = document.getElementById("root");
if (root) {
  createRoot(root).render(<GiftPage />);
}
