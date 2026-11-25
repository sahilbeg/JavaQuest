import Link from "next/link";

// TODO 2: set metadata to { metadataBase: new URL("https://dhaaga.in"), title: { default: "Dhaaga & Co.",
// template: "%s | Dhaaga & Co." }, description: "..." }, and give <html> the attribute lang="en-IN".
export const metadata = {
  title: "Dhaaga & Co.",
};

export default function RootLayout({ children }) {
  return (
    <html>
      <body>
        <a href="#main">Skip to content</a>
        <header>
          <nav aria-label="Main">
            <Link href="/">Shop</Link> <Link href="/cart">Cart</Link>
          </nav>
        </header>
        <main id="main">{children}</main>
      </body>
    </html>
  );
}
