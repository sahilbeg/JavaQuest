import { useEffect, useRef, useState } from "react";
import type { ChangeEvent, KeyboardEvent } from "react";

const EMPTY: string[] = ["", "", "", "", "", ""];

/** Six one-digit boxes for a delivery PIN code, like an OTP screen. */
export function PincodeBoxes({ onComplete }: { onComplete: (pincode: string) => void }) {
  const [digits, setDigits] = useState<string[]>(EMPTY);

  // TODO 1: const boxes = useRef<(HTMLInputElement | null)[]>([]); and, in the JSX below, give each input
  //         ref={(el) => { boxes.current[i] = el; }} (with braces, so it returns nothing).
  // TODO 2: in an effect that runs once, put the focus in the first box: boxes.current[0]?.focus().

  function handleChange(index: number, event: ChangeEvent<HTMLInputElement>) {
    const digit = event.currentTarget.value.replace(/\D/g, "").slice(-1);
    const next = digits.map((d, i) => (i === index ? digit : d));
    setDigits(next);
    // TODO 3: if digit isn't empty, move the focus to the next box (if there is one).
    // TODO 4: if every box in next holds a digit, call onComplete(next.join("")).
  }

  function handleKeyDown(index: number, event: KeyboardEvent<HTMLInputElement>) {
    // TODO 5: on "Backspace" in an empty box that isn't the first, call event.preventDefault()
    //         and move the focus to the box before.
  }

  return (
    <fieldset>
      <legend>Delivery PIN code</legend>
      {digits.map((digit, i) => (
        <input
          key={i}
          aria-label={`Digit ${i + 1}`}
          inputMode="numeric"
          size={1}
          value={digit}
          onChange={(event) => handleChange(i, event)}
          onKeyDown={(event) => handleKeyDown(i, event)}
        />
      ))}
    </fieldset>
  );
}
