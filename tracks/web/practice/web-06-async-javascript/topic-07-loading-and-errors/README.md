# Loading and error states

The catalogue is blank while it loads and stays blank when the request fails. Give it a status line for every state. Change `src/catalogue.js`.

`loadCatalogue({ list, status, retry })` gets the `<ul>` for the items, the status line, and the **Try again** button. The texts are already in the file as `LOADING`, `EMPTY` and `FAILED`.

1. **TODO 1:** before asking the server, hide `retry`, put `LOADING` in `status.textContent` and set `status.dataset.state = "loading"`.
2. **TODO 2:** wrap the request in `try...catch`. When `response.ok` is false, throw an `Error`, so a 500 is handled like a dropped connection.
3. **TODO 3:** call `list.replaceChildren()` before drawing, so loading twice doesn't double the list.
4. **TODO 4:** with no items, show `EMPTY` and set the state to `"empty"`. Otherwise set the status text to `""` and the state to `"ready"`.
5. **TODO 5:** in `catch`, show `FAILED`, set the state to `"error"` and show the button with `retry.hidden = false`. `loadCatalogue` must never throw.

Press **Submit** in Cadence, or save the file in your editor. The checks use a pretend server that can be slow, broken or offline.
