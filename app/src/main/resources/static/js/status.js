export function ticketState(topic) {
  const progress = topic.progress;
  const outcome = topic.latest?.result?.outcome;
  if (progress?.mastered) return { key: "done", label: "Done", chip: "chip-done" };
  if (progress?.testsPassed) return { key: "progress", label: "Checks pass · take the check", chip: "chip-progress" };
  if (outcome && outcome !== "PASSED") return { key: "progress", label: "Checks failing", chip: "chip-failing" };
  if (progress?.quizAttempts || outcome) return { key: "progress", label: "In progress", chip: "chip-progress" };
  return { key: "todo", label: "To do", chip: "chip-todo" };
}

export function nextTicket(topics) {
  return topics.find((t) => ticketState(t).key !== "done") ?? topics.at(-1);
}
