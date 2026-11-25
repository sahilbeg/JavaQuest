import { useState } from "react";

/**
 * Like useState, but the value is also kept in localStorage under key, as JSON, so it survives a refresh.
 * isValid, when given, checks the saved value's shape before it is trusted.
 */
export function useStoredState<T>(
  key: string,
  initial: T,
  isValid?: (value: unknown) => value is T,
): [T, (next: T) => void] {
  // TODO 2: start from the saved value with a lazy initializer: useState<T>(() => ...), so storage is read once.
  //         Use initial when nothing is saved under key, when JSON.parse throws, or when isValid says no.
  // TODO 3: return [value, save], where save(next) writes JSON.stringify(next) under key and then sets the state.
  const [value] = useState<T>(initial);
  return [value, () => {}];
}
