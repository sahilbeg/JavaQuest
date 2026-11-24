import { useRef, useState } from "react";

/**
 * Round 2. A keyboard-accessible tab list.
 * tabs is [{ id, label, content }]. label names the whole tab list for screen readers.
 */
export function Tabs({ label, tabs }) {
  const [selected, setSelected] = useState(0);
  const tabRefs = useRef([]);

  function onKeyDown(event) {
    const last = tabs.length - 1;
    const moves = {
      ArrowRight: selected === last ? 0 : selected + 1,
      ArrowLeft: selected === 0 ? last : selected - 1,
      Home: 0,
      End: last,
    };
    if (event.key in moves) {
      event.preventDefault();
      setSelected(moves[event.key]);
      tabRefs.current[moves[event.key]].focus();
    }
  }

  return (
    <div className="tabs">
      <div role="tablist" aria-label={label}>
        {tabs.map((tab, index) => (
          <button
            key={tab.id}
            ref={(element) => {
              tabRefs.current[index] = element;
            }}
            type="button"
            role="tab"
            id={`tab-${tab.id}`}
            aria-selected={index === selected}
            aria-controls={`panel-${tab.id}`}
            tabIndex={index === selected ? 0 : -1}
            onClick={() => setSelected(index)}
            onKeyDown={onKeyDown}
          >
            {tab.label}
          </button>
        ))}
      </div>
      {tabs.map((tab, index) => (
        <div
          key={tab.id}
          role="tabpanel"
          id={`panel-${tab.id}`}
          aria-labelledby={`tab-${tab.id}`}
          hidden={index !== selected}
          tabIndex={0}
        >
          {tab.content}
        </div>
      ))}
    </div>
  );
}
