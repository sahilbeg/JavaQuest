import { useEffect, useState } from "react";

/** The value stored under key, if it is there, is good JSON and passes the check. Otherwise the initial value. */
function readStored<T>(key: string, initial: T, isValid: (value: unknown) => value is T): T {
  const stored = localStorage.getItem(key);
  if (stored === null) return initial;
  try {
    const parsed: unknown = JSON.parse(stored);
    return isValid(parsed) ? parsed : initial;
  } catch {
    return initial;
  }
}

/**
 * Round 2. Like useState, but remembered in localStorage under key.
 * Stored data is checked with isValid, every change is saved, and a new key reads that key's value.
 */
export function useStoredState<T>(
  key: string,
  initial: T,
  isValid: (value: unknown) => value is T
): [T, (next: T) => void] {
  const [value, setValue] = useState<T>(() => readStored(key, initial, isValid));

  useEffect(() => {
    setValue(readStored(key, initial, isValid));
    // Only a new key means a different stored value; initial and isValid are often new on every render.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key]);

  function save(next: T) {
    localStorage.setItem(key, JSON.stringify(next));
    setValue(next);
  }

  return [value, save];
}
