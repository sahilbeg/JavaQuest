import type { ReactNode } from "react";

export interface PanelProps {
  title: string;
  children: ReactNode;
}

export function Panel({ title }: PanelProps) {
  return (
    <section className="panel">
      <h2>{title}</h2>
      {/* TODO 1: Take children from the props and show them inside this div. */}
      <div className="panel-body"></div>
    </section>
  );
}
