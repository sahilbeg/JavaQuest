# Children and composition

Build Sana's matching boxes.

**`Panel.tsx`**

1. **TODO 1:** Take `children` from the props and show them inside `<div className="panel-body">`, under the `<h2>` title.

**`PageLayout.tsx`**

2. **TODO 2:** Put the `header` slot inside `<header className="page-header">`.
3. **TODO 3:** Put the `children` inside `<main className="page-main">`, after the header.

**`HoursPanel.tsx`**

4. **TODO 4:** Build it from `Panel`: the title is `Opening hours`, and inside are two paragraphs:
   - `Stall: Sat and Sun, 10:00 to 20:00`
   - `Aundh shop: Tue to Sun, 11:00 to 21:00`

The props interfaces are already written, with `children` typed as `ReactNode`. Keep `<section className="panel">` and `<div className="page">`. The preview shows the whole page. Press **Submit** to run the checks.
