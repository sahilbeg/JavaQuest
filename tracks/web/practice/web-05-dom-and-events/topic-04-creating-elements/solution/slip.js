// The counter's order slip. An item looks like { code: "KEY", name: "Keychain", qty: 2 }.

/** A new <li class="line" data-code="..."> with a .name span, a .qty span and a Remove button. Not on the page yet. */
export function slipLine(item) {
  const li = document.createElement("li");
  li.className = "line";
  li.dataset.code = item.code;

  const name = document.createElement("span");
  name.className = "name";
  name.textContent = item.name;

  const qty = document.createElement("span");
  qty.className = "qty";
  qty.textContent = String(item.qty);

  const remove = document.createElement("button");
  remove.type = "button";
  remove.className = "remove";
  remove.textContent = "Remove";
  remove.setAttribute("aria-label", `Remove ${item.name}`);

  li.append(name, " × ", qty, " ", remove);
  return li;
}

/** Adds a line for the item at the end of the slip, and returns that line. */
export function addLine(slip, item) {
  const line = slipLine(item);
  slip.append(line);
  return line;
}

/** Removes the slip's line with this data-code: true if one was removed, false if there was none. */
export function removeLine(slip, code) {
  const line = slip.querySelector(`[data-code="${code}"]`);
  if (line === null) {
    return false;
  }
  line.remove();
  return true;
}

/** Removes every line. The slip itself stays on the page. */
export function clearSlip(slip) {
  slip.replaceChildren();
}
