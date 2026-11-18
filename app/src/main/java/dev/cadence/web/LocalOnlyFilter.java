package dev.cadence.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Blocks requests that did not come from the Cadence page itself. The Host check stops DNS rebinding.
 * The custom header on writes forces a CORS preflight, which other sites cannot pass.
 */
@Component
public class LocalOnlyFilter extends OncePerRequestFilter {

    public static final String WRITE_HEADER = "X-Cadence";
    private static final Set<String> ALLOWED_HOSTS = Set.of("localhost", "127.0.0.1");
    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!ALLOWED_HOSTS.contains(request.getServerName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Cadence only answers on localhost");
            return;
        }
        if (!READ_METHODS.contains(request.getMethod()) && request.getHeader(WRITE_HEADER) == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing " + WRITE_HEADER + " header");
            return;
        }
        chain.doFilter(request, response);
    }
}
