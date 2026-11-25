import type { ReactNode } from "react";

export interface PickListProps<T> {
  label: string;
  options: readonly T[];
  selected: T | null;
  getKey: (option: T) => string;
  renderOption: (option: T) => ReactNode;
  onPick: (option: T) => void;
}

export function PickList<T>({ label, options, selected, getKey, renderOption, onPick }: PickListProps<T>) {
  return (
    <fieldset className="pick-list">
      <legend>{label}</legend>
      {/* TODO 1: One <button type="button"> per option, with key={getKey(option)}. */}
      {/* TODO 2: Inside each button, show renderOption(option). */}
      {/* TODO 3: On click, call onPick(option). */}
      {/* TODO 4: aria-pressed={selected !== null && getKey(selected) === getKey(option)} */}
    </fieldset>
  );
}
