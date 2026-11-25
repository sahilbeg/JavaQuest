/** Whether the menu link to href should be marked as the current page, when the address's path is pathname. */
export function isActive(pathname, href) {
  if (href === "/") return pathname === "/";
  return pathname === href || pathname.startsWith(href + "/");
}
