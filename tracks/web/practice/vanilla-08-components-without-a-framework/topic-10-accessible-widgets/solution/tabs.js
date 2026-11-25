// The item page's tabs: Details, Care and Delivery, for mouse, keyboard and screen reader alike.

/** Wires the tab list inside root (the .tabs element). */
export function setupTabs(root) {
  const list = root.querySelector(".tabs__list");
  const tabs = [...list.querySelectorAll("button")];
  const panels = tabs.map((tab) => root.querySelector(`#${tab.dataset.panel}`));

  list.setAttribute("role", "tablist");
  tabs.forEach((tab, i) => {
    tab.setAttribute("role", "tab");
    tab.setAttribute("aria-controls", panels[i].id);
    panels[i].setAttribute("role", "tabpanel");
    panels[i].setAttribute("aria-labelledby", tab.id);
    panels[i].tabIndex = 0;
  });

  function select(index) {
    tabs.forEach((tab, i) => {
      const selected = i === index;
      tab.classList.toggle("is-selected", selected);
      panels[i].hidden = !selected;
      tab.setAttribute("aria-selected", String(selected));
      tab.tabIndex = selected ? 0 : -1;
    });
  }

  list.addEventListener("click", (event) => {
    const tab = event.target.closest("button");
    if (tab !== null) {
      select(tabs.indexOf(tab));
    }
  });

  list.addEventListener("keydown", (event) => {
    const current = tabs.indexOf(event.target.closest("button"));
    if (current < 0) {
      return;
    }
    const moves = {
      ArrowRight: (current + 1) % tabs.length,
      ArrowLeft: (current - 1 + tabs.length) % tabs.length,
      Home: 0,
      End: tabs.length - 1,
    };
    const next = moves[event.key];
    if (next === undefined) {
      return;
    }
    event.preventDefault();
    select(next);
    tabs[next].focus();
  });

  select(0);
}
