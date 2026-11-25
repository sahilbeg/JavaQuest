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

  function addTyped() {
    const code = text.trim();
    if (code === "") {
      setMessage("Type a code first");
    } else if (isItemCode(code)) {
      onAdd(code);
      setMessage(`Added ${ITEM_NAMES[code]}`);
      setText("");
    } else {
      setMessage(`Unknown code: ${code}`);
    }
  }

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    setText(e.target.value.toUpperCase());
  }

  function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter") {
      addTyped();
    }
  }

  return (
    <div className="counter-pad">
      <label htmlFor="code">Item code</label>
      <input id="code" value={text} onChange={handleChange} onKeyDown={handleKeyDown} />
      <button type="button" onClick={addTyped}>
        Add
      </button>
      <p className="message">{message}</p>
    </div>
  );
}
