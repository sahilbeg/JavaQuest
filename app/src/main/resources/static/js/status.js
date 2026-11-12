export function ticketState(topic) {
  const outcome = topic.latest?.result?.outcome;
  if (outcome === "PASSED") return { key: "done", label: "Done", chip: "chip-done" };
  if (outcome) return { key: "progress", label: "Checks failing", chip: "chip-failing" };
  return { key: "todo", label: "To do", chip: "chip-todo" };
}

export function nextTicket(topics) {
  return topics.find((t) => ticketState(t).key !== "done") ?? topics.at(-1);
}
