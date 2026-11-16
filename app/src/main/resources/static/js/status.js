export function ticketState(topic) {
  const progress = topic.progress;
  const outcome = topic.latest?.result?.outcome;
  if (progress?.mastered) return { key: "done", label: "Done", chip: "chip-done" };
  if (topic.locked) return { key: "locked", label: "Locked", chip: "chip-locked" };
  if (progress?.testsPassed) return { key: "progress", label: "Checks pass · take the check", chip: "chip-progress" };
  if (outcome && outcome !== "PASSED") return { key: "progress", label: "Checks failing", chip: "chip-failing" };
  if (progress?.quizAttempts || outcome) return { key: "progress", label: "In progress", chip: "chip-progress" };
  return { key: "todo", label: "To do", chip: "chip-todo" };
}

export function topicKind(topic) {
  return topic.id.endsWith("/boss") ? "incident" : topic.id.endsWith("/mini-project") ? "feature" : "ticket";
}

/** The ticket to work on now: the catalog's frontier, or the last ticket when everything is done. */
export function nextTicket(catalog) {
  const { topics, frontierId } = catalog;
  return topics.find((t) => t.id === frontierId) ?? topics.find((t) => ticketState(t).key !== "done") ?? topics.at(-1);
}

/** One emoji per project, for the journey map. */
export const MODULE_EMOJI = ["👋", "🧮", "🚲", "📊", "🧭", "⚡", "🧹", "🚀", "🌐", "🏗️", "🎓"];

export function moduleEmoji(moduleId) {
  const n = Number(moduleId.match(/module-(\d+)/)?.[1] ?? 0);
  return MODULE_EMOJI[n] ?? "⭐";
}
