import { useState } from "react";
import type { ChangeEvent, KeyboardEvent } from "react";
import { ITEM_NAMES, isItemCode } from "./items.ts";
import type { ItemCode } from "./items.ts";

export interface CounterPadProps {
  onAdd: (code: ItemCode) => void;
}

export function CounterPad({ onAdd }: CounterPadProps) {
  const [text, setText] = useState("");
  const [message, setMessage] = useState("");

  // TODO 1: Check the typed code, trimmed:
  //         - empty: set the message to "Type a code first"
  //         - a real code (isItemCode): call onAdd(code), set the message to "Added <name>", and empty the box
  //         - anything else: set the message to "Unknown code: <code>"
  function addTyped() {}

  // TODO 2: Write handleChange(e: ChangeEvent<HTMLInputElement>), which keeps the typed text in capitals.

  // TODO 3: Write handleKeyDown(e: KeyboardEvent<HTMLInputElement>), which runs addTyped() when the key is "Enter".

  return (
    <div className="counter-pad">
      <label htmlFor="code">Item code</label>
      {/* TODO 4: Use onChange={handleChange} and onKeyDown={handleKeyDown} here. */}
      <input id="code" value={text} onChange={(e) => setText(e.target.value)} />
      <button type="button" onClick={addTyped}>
        Add
      </button>
      <p className="message">{message}</p>
    </div>
  );
}
