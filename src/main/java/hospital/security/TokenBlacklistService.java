package hospital.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String token, Instant expiresAt) {
        if (token == null || token.isBlank()) {
            return;
        }
        revokedTokens.put(token, expiresAt == null ? Instant.now().plusSeconds(86400) : expiresAt);
    }

    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        purgeExpired();
        return revokedTokens.containsKey(token);
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue() != null && entry.getValue().isBefore(now));
    }
}
