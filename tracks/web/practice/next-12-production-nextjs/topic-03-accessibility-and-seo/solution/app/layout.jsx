import { SiteShell } from "../components/SiteShell.jsx";

export const metadata = {
  metadataBase: new URL("https://dhaaga.in"),
  title: { default: "Dhaaga & Co.", template: "%s | Dhaaga & Co." },
  description: "Handmade crochet from Pune: amigurumi, coasters, keychains, tote bags and baby booties.",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en-IN">
      <body>
        <SiteShell>{children}</SiteShell>
      </body>
    </html>
  );
}
