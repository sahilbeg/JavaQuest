import { useState } from "react";

/** A yes/no value and a function that flips it: const [open, toggle] = useToggle(); */
export function useToggle(initial: boolean = false): readonly [boolean, () => void] {
  // TODO 1: keep the value in useState<boolean>(initial). Make toggle flip it with the updater form,
  //         then return [on, toggle] as const.
  return [initial, () => {}] as const;
}
