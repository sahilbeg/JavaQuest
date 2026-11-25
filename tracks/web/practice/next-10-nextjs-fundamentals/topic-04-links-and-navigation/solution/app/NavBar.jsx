"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { isActive } from "../lib/nav.js";

const LINKS = [
  { href: "/", label: "Home" },
  { href: "/items", label: "Shop" },
  { href: "/help", label: "Help" },
];

/** The main menu at the top of every page. */
export function NavBar() {
  const pathname = usePathname();
  return (
    <nav aria-label="Main">
      <ul>
        {LINKS.map((link) => (
          <li key={link.href}>
            <Link href={link.href} aria-current={isActive(pathname, link.href) ? "page" : undefined}>
              {link.label}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}
