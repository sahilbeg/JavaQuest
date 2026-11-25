import { SiteShell } from "../components/SiteShell.jsx";

// TODO 1: every page is titled "Dhaaga & Co.". Use a title object with default "Dhaaga & Co." and template "%s | Dhaaga & Co.",
// add a description, and set metadataBase to new URL("https://dhaaga.in").
export const metadata = {
  title: "Dhaaga & Co.",
};

export default function RootLayout({ children }) {
  // TODO 1 (also): tell browsers and screen readers the page is in Indian English with lang="en-IN".
  return (
    <html>
      <body>
        <SiteShell>{children}</SiteShell>
      </body>
    </html>
  );
}
