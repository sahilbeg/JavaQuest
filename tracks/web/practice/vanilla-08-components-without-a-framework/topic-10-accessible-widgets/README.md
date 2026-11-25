# Accessible widgets: a tab list with ARIA

Make the tabs on Golu's item page work for screen readers and keyboards, in `src/tabs.js`. `setupTabs(root)` gets the `.tabs` element below. Each tab button's `data-panel` is the id of its panel.

```html
<div class="tabs">
  <div class="tabs__list" aria-label="About Golu the elephant">
    <button type="button" id="tab-details" data-panel="panel-details">Details</button>
    <button type="button" id="tab-care" data-panel="panel-care">Care</button>
    <button type="button" id="tab-delivery" data-panel="panel-delivery">Delivery</button>
  </div>
  <section id="panel-details">...</section>
  <section id="panel-care">...</section>
  <section id="panel-delivery">...</section>
</div>
```

1. **TODO 1:** `.tabs__list` gets `role="tablist"`. Each button gets `role="tab"` and `aria-controls` (its panel's id). Each panel gets `role="tabpanel"`, `aria-labelledby` (its tab's id) and `tabindex="0"`.
2. **TODO 2:** selecting a tab sets `aria-selected="true"` on it and `"false"` on the others, shows only its panel (the others get `hidden`), and gives it `tabindex="0"` and the others `tabindex="-1"`. The first tab starts selected, and a click selects a tab.
3. **TODO 3:** on `keydown` inside the list: Right arrow moves to the next tab (from the last, to the first), Left arrow to the previous one (from the first, to the last), `Home` to the first and `End` to the last.
4. **TODO 4:** for those four keys, the new tab is selected **and focused**, and `preventDefault()` is called. Every other key does nothing.

Open the preview, click into the tabs, and use only the keyboard.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
