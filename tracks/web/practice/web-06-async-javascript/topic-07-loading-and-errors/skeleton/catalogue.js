// Loads Dhaaga's catalogue from GET /api/catalogue and draws it, with a status line for every state.
// The server answers with a list such as [{ "id": "DC-1042", "name": "Golu the elephant", "paise": 45000 }].

export const LOADING = "Loading the catalogue...";
export const EMPTY = "No items yet. Check back soon!";
export const FAILED = "Couldn't load the catalogue. Check your connection and try again.";

/**
 * list is the <ul> for the items, status is the status line, and retry is the Try again button.
 * Sets status.dataset.state to "loading", then to "ready", "empty" or "error". Never throws.
 */
export async function loadCatalogue({ list, status, retry }) {
  // TODO 1: before asking, hide retry, put LOADING in status and set status.dataset.state to "loading".

  // TODO 2: wrap the request below in try...catch, and throw an Error when response.ok is false.
  const response = await fetch("/api/catalogue");
  const items = await response.json();

  // TODO 3: empty the list first with list.replaceChildren(), so loading twice doesn't double it.
  for (const item of items) {
    const li = document.createElement("li");
    li.textContent = item.name;
    list.append(li);
  }

  // TODO 4: with no items, show EMPTY and set the state to "empty".
  //         Otherwise clear the status text ("") and set the state to "ready".

  // TODO 5: in catch, show FAILED, set the state to "error" and show the retry button (retry.hidden = false).
}
