import { useState } from "react";

/** A yes/no value and a function that flips it: const [open, toggle] = useToggle(); */
export function useToggle(initial: boolean = false): readonly [boolean, () => void] {
  const [on, setOn] = useState<boolean>(initial);
  const toggle = () => setOn((v) => !v);
  return [on, toggle] as const;
}
