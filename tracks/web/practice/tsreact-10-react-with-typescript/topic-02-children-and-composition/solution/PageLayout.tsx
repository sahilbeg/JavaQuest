import type { ReactNode } from "react";

export interface PageLayoutProps {
  header: ReactNode;
  children: ReactNode;
}

export function PageLayout({ header, children }: PageLayoutProps) {
  return (
    <div className="page">
      <header className="page-header">{header}</header>
      <main className="page-main">{children}</main>
    </div>
  );
}
