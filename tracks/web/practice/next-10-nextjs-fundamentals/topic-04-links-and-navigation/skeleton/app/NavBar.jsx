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
            {/* TODO 2: use <Link> instead of <a>, so a tap doesn't load the whole page again. */}
            {/* TODO 3: give the current page's link aria-current="page" (use pathname and isActive). */}
            <a href={link.href}>{link.label}</a>
          </li>
        ))}
      </ul>
    </nav>
  );
}
