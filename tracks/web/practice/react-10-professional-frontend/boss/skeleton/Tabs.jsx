import { useRef, useState } from "react";

/**
 * Round 2. A keyboard-accessible tab list.
 * tabs is [{ id, label, content }]. label names the whole tab list for screen readers.
 *
 * This is the candidate's answer. It works with a mouse, and with nothing else.
 */
export function Tabs({ label, tabs }) {
  const [selected, setSelected] = useState(0);
  const tabRefs = useRef([]);

  // TODO 2: wrap the tabs in a role="tablist" with aria-label={label}. Each tab is a <button role="tab"> with an id,
  // aria-selected, aria-controls and a roving tabIndex (0 on the selected tab, -1 on the others).
  // TODO 3: render one role="tabpanel" per tab, with a matching id, aria-labelledby, tabIndex={0}, and hidden unless selected.
  // TODO 4: on a tab's keydown, ArrowRight and ArrowLeft move to the next and previous tab (wrapping round),
  // Home and End to the first and last. Moving selects the tab AND focuses it (keep each button in tabRefs).
  return (
    <div className="tabs">
      <div className="tab-row">
        {tabs.map((tab, index) => (
          <div key={tab.id} className={index === selected ? "tab active" : "tab"} onClick={() => setSelected(index)}>
            {tab.label}
          </div>
        ))}
      </div>
      <div className="panel">{tabs[selected].content}</div>
    </div>
  );
}
