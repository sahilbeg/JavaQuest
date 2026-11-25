import { useState } from "react";

/** The saved value, or initial when there is none, it is broken JSON, or it has the wrong shape. */
function readSaved<T>(key: string, initial: T, isValid?: (value: unknown) => value is T): T {
  const text = localStorage.getItem(key);
  if (text === null) return initial;
  try {
    const parsed: unknown = JSON.parse(text);
    if (isValid && !isValid(parsed)) return initial;
    return parsed as T;
  } catch {
    return initial;
  }
}

/**
 * Like useState, but the value is also kept in localStorage under key, as JSON, so it survives a refresh.
 * isValid, when given, checks the saved value's shape before it is trusted.
 */
export function useStoredState<T>(
  key: string,
  initial: T,
  isValid?: (value: unknown) => value is T,
): [T, (next: T) => void] {
  const [value, setValue] = useState<T>(() => readSaved(key, initial, isValid));

  function save(next: T) {
    localStorage.setItem(key, JSON.stringify(next));
    setValue(next);
  }

  return [value, save];
}
