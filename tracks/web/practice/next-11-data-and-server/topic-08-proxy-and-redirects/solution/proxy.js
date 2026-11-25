/** Runs before the routes in config.matcher. Return a Response to stop the request, or nothing to let it through. */
export function proxy(request) {
  const { pathname } = request.nextUrl;

  if (pathname === "/catalogue" || pathname.startsWith("/catalogue/")) {
    const rest = pathname.slice("/catalogue".length);
    return Response.redirect(new URL(`/items${rest}`, request.url), 308);
  }

  if (pathname === "/account" || pathname.startsWith("/account/")) {
    if (!request.cookies.get("dhaaga_session")?.value) {
      return Response.redirect(new URL(`/login?next=${pathname}`, request.url), 307);
    }
  }
}

export const config = {
  matcher: ["/account/:path*", "/catalogue/:path*"],
};
