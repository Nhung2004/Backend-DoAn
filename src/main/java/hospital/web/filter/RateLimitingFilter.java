package hospital.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int GENERAL_LIMIT = 100;
    private static final int AUTH_LIMIT = 5;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        int limit = isAuthEndpoint(request.getRequestURI()) ? AUTH_LIMIT : GENERAL_LIMIT;
        String key = clientKey(request);
        WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter());
        if (counter.isExpired()) {
            counter.reset();
        }

        int current = counter.increment();
        if (current > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response
                .getWriter()
                .write("{\"error\":\"Too Many Requests\",\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Rate limit exceeded\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthEndpoint(String uri) {
        return "/api/auth/login".equals(uri) || "/api/auth/register".equals(uri) || "/api/authenticate".equals(uri);
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        return ip + ":" + request.getRequestURI();
    }

    private static final class WindowCounter {

        private final AtomicInteger count = new AtomicInteger(0);
        private volatile Instant windowStart = Instant.now();

        int increment() {
            return count.incrementAndGet();
        }

        boolean isExpired() {
            return Instant.now().isAfter(windowStart.plusSeconds(RateLimitingFilter.WINDOW_SECONDS));
        }

        void reset() {
            count.set(0);
            windowStart = Instant.now();
        }
    }
}
