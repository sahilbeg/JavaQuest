// The item page's tabs: Details, Care and Delivery. Right now they only work with a mouse.

/** Wires the tab list inside root (the .tabs element). */
export function setupTabs(root) {
  const list = root.querySelector(".tabs__list");
  const tabs = [...list.querySelectorAll("button")];
  const panels = tabs.map((tab) => root.querySelector(`#${tab.dataset.panel}`));

  // TODO 1: role="tablist" on list; role="tab" and aria-controls on each tab;
  //         role="tabpanel", aria-labelledby and tabindex="0" on each panel

  function select(index) {
    tabs.forEach((tab, i) => {
      tab.classList.toggle("is-selected", i === index);
      panels[i].hidden = i !== index;
      // TODO 2: aria-selected "true" or "false", and a roving tabindex: 0 on the selected tab, -1 on the others
    });
  }

  list.addEventListener("click", (event) => {
    const tab = event.target.closest("button");
    if (tab !== null) {
      select(tabs.indexOf(tab));
    }
  });

  // TODO 3: a keydown listener on list: ArrowRight and ArrowLeft move to the next or previous tab (wrapping with %),
  //         Home to the first and End to the last
  // TODO 4: for those keys only, select the new tab, focus it and call event.preventDefault(); other keys do nothing

  select(0);
}
