import { useState } from "react";
import { createRoot } from "react-dom/client";
import { YarnPanel } from "./YarnPanel.tsx";
import type { Colour } from "./yarn.ts";

const PRETEND: Record<string, { body: unknown; delayMs: number }> = {
  Marigold: { body: { colour: "Marigold", grams: 1200 }, delayMs: 1500 },
  Indigo: { body: { colour: "Indigo", grams: 600 }, delayMs: 200 },
  Mint: { body: { colour: "Mint", grams: "300" }, delayMs: 300 },
};

// A pretend stock API for the preview: Marigold is slow, Indigo is quick, and Mint sends grams as text.
window.fetch = async (input: RequestInfo | URL): Promise<Response> => {
  const colour = decodeURIComponent(String(input).split("/").pop() ?? "");
  const answer = PRETEND[colour];
  await new Promise((resolve) => setTimeout(resolve, answer ? answer.delayMs : 100));
  if (!answer) return new Response(JSON.stringify({ message: "not found" }), { status: 404 });
  return new Response(JSON.stringify(answer.body), { status: 200 });
};

const COLOURS: Colour[] = ["Marigold", "Indigo", "Mint"];

function App() {
  const [colour, setColour] = useState<Colour>("Marigold");
  return (
    <main>
      <h1>Yarn stock</h1>
      {COLOURS.map((c) => (
        <button key={c} onClick={() => setColour(c)}>
          {c}
        </button>
      ))}
      <YarnPanel colour={colour} />
    </main>
  );
}

createRoot(document.getElementById("root")!).render(<App />);
