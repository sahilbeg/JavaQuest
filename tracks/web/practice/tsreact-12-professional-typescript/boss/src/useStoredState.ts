import { useEffect, useState } from "react";

/**
 * Round 2. Like useState, but remembered in localStorage under key.
 * Stored data is checked with isValid, every change is saved, and a new key reads that key's value.
 */
export function useStoredState<T>(
  key: string,
  initial: T,
  isValid: (value: unknown) => value is T
): [T, (next: T) => void] {
  // TODO 2: the candidate trusts whatever is stored, with "as T". Parse it as unknown and keep it only if isValid says yes.
  const [value, setValue] = useState<T>(() => {
    const stored = localStorage.getItem(key);
    if (stored === null) return initial;
    try {
      return JSON.parse(stored) as T;
    } catch {
      return initial;
    }
  });

  // TODO 3: save every change to localStorage (as JSON, under key), and read the stored value again whenever key changes (useEffect).
  function save(next: T) {
    setValue(next);
  }

  return [value, save];
}
