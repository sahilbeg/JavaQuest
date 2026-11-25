import { useEffect, useRef, useState } from "react";
import type { ChangeEvent, KeyboardEvent } from "react";

const EMPTY: string[] = ["", "", "", "", "", ""];

/** Six one-digit boxes for a delivery PIN code, like an OTP screen. */
export function PincodeBoxes({ onComplete }: { onComplete: (pincode: string) => void }) {
  const [digits, setDigits] = useState<string[]>(EMPTY);
  const boxes = useRef<(HTMLInputElement | null)[]>([]);

  useEffect(() => {
    boxes.current[0]?.focus();
  }, []);

  function handleChange(index: number, event: ChangeEvent<HTMLInputElement>) {
    const digit = event.currentTarget.value.replace(/\D/g, "").slice(-1);
    const next = digits.map((d, i) => (i === index ? digit : d));
    setDigits(next);
    if (digit !== "") boxes.current[index + 1]?.focus();
    if (next.every((d) => d !== "")) onComplete(next.join(""));
  }

  function handleKeyDown(index: number, event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === "Backspace" && digits[index] === "" && index > 0) {
      event.preventDefault();
      boxes.current[index - 1]?.focus();
    }
  }

  return (
    <fieldset>
      <legend>Delivery PIN code</legend>
      {digits.map((digit, i) => (
        <input
          key={i}
          ref={(el) => {
            boxes.current[i] = el;
          }}
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
