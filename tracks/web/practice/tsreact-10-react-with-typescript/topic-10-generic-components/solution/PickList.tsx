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
      {options.map((option) => (
        <button
          key={getKey(option)}
          type="button"
          aria-pressed={selected !== null && getKey(selected) === getKey(option)}
          onClick={() => onPick(option)}
        >
          {renderOption(option)}
        </button>
      ))}
    </fieldset>
  );
}
