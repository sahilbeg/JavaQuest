import type { ReactNode } from "react";

export interface PageLayoutProps {
  header: ReactNode;
  children: ReactNode;
}

export function PageLayout({ header, children }: PageLayoutProps) {
  return (
    <div className="page">
      {/* TODO 2: Put the header slot inside <header className="page-header">. */}
      {/* TODO 3: Put the children inside <main className="page-main">, after the header. */}
    </div>
  );
}
