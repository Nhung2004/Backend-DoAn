package hospital.web.rest;

import hospital.domain.User;
import hospital.repository.AuthorityRepository;
import hospital.repository.UserRepository;
import hospital.security.AuthoritiesConstants;
import hospital.security.DomainUserDetailsService.UserWithId;
import hospital.security.SecurityUtils;
import hospital.security.TokenBlacklistService;
import hospital.service.dto.AuthTokenDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.jhipster.security.RandomUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthResource {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds:86400}")
    private long tokenValidityInSeconds;

    public AuthResource(
        JwtEncoder jwtEncoder,
        JwtDecoder jwtDecoder,
        AuthenticationManagerBuilder authenticationManagerBuilder,
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        PasswordEncoder passwordEncoder,
        TokenBlacklistService tokenBlacklistService
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            return ResponseEntity.badRequest().body(error("Passwords do not match", "PASSWORD_MISMATCH"));
        }
        if (!isStrongPassword(request.password())) {
            return ResponseEntity.badRequest()
                .body(
                    error(
                        "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character",
                        "WEAK_PASSWORD"
                    )
                );
        }
        if (userRepository.findOneByEmailIgnoreCase(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body(error("Email already exists", "DUPLICATE_EMAIL"));
        }

        User user = new User();
        user.setLogin(request.email().toLowerCase());
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActivated(true);
        user.setFirstName(extractFirstName(request.fullName()));
        user.setLastName(extractLastName(request.fullName()));
        user.setPhoneNumber(request.phoneNumber());
        Set<hospital.domain.Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        user.setAuthorities(authorities);
        return createUserAndIssueToken(user);
    }

    private ResponseEntity<?> createUserAndIssueToken(User user) {
        userRepository.save(user);
        String token = createTokenFromUser(user);
        AuthTokenDTO response = buildTokenResponse(user, token);
        response.setCreatedAt(Instant.now().toString());
        return ResponseEntity.status(HttpStatus.CREATED).header("Set-Cookie", accessTokenCookie(token).toString()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            );
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userRepository
                .findOneWithAuthoritiesByLogin(request.email().toLowerCase())
                .orElseGet(() -> userRepository.findOneWithAuthoritiesByEmailIgnoreCase(request.email()).orElse(null));
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Invalid credentials", "INVALID_CREDENTIALS"));
            }
            String token = createTokenFromUser(user);
            return ResponseEntity.ok().header("Set-Cookie", accessTokenCookie(token).toString()).body(buildTokenResponse(user, token));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Invalid credentials", "INVALID_CREDENTIALS"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        SecurityUtils.getCurrentUserJWT().ifPresent(token -> tokenBlacklistService.revoke(token, null));
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok()
            .header("Set-Cookie", clearAccessTokenCookie().toString())
            .body(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody(required = false) RefreshRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (authentication != null && authentication.isAuthenticated()) {
            user = userRepository.findOneWithAuthoritiesByLogin(authentication.getName()).orElse(null);
        }
        if (user == null && request != null && request.token() != null && !request.token().isBlank()) {
            try {
                var jwt = jwtDecoder.decode(request.token());
                user = userRepository.findOneWithAuthoritiesByLogin(jwt.getSubject()).orElse(null);
            } catch (Exception ignored) {
                user = null;
            }
        }
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Unauthorized", "UNAUTHORIZED"));
        }
        String token = createTokenFromUser(user);
        return ResponseEntity.ok().header("Set-Cookie", accessTokenCookie(token).toString()).body(buildTokenResponse(user, token));
    }

    private AuthTokenDTO buildTokenResponse(User user, String token) {
        AuthTokenDTO response = new AuthTokenDTO();
        response.setId(String.valueOf(user.getId()));
        response.setEmail(user.getEmail());
        response.setFullName(fullName(user));
        response.setRole(
            user
                .getAuthorities()
                .stream()
                .map(hospital.domain.Authority::getName)
                .findFirst()
                .map(role -> role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role)
                .orElse("USER")
        );
        response.setToken(token);
        response.setExpiresIn(tokenValidityInSeconds);
        return response;
    }

    private String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));
        Instant now = Instant.now();
        Instant validity = now.plus(tokenValidityInSeconds, ChronoUnit.SECONDS);
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(authentication.getName())
            .claim(SecurityUtils.AUTHORITIES_CLAIM, authorities);
        if (authentication.getPrincipal() instanceof UserWithId user) {
            builder.claim(SecurityUtils.USER_ID_CLAIM, user.getId());
        }
        JwsHeader jwsHeader = JwsHeader.with(hospital.security.SecurityUtils.JWT_ALGORITHM).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, builder.build())).getTokenValue();
    }

    private String createTokenFromUser(User user) {
        Instant now = Instant.now();
        Instant validity = now.plus(tokenValidityInSeconds, ChronoUnit.SECONDS);
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder().issuedAt(now).expiresAt(validity).subject(user.getLogin());
        String authorities = user.getAuthorities().stream().map(hospital.domain.Authority::getName).collect(Collectors.joining(" "));
        builder.claim(SecurityUtils.AUTHORITIES_CLAIM, authorities);
        if (user.getId() != null) {
            builder.claim(SecurityUtils.USER_ID_CLAIM, user.getId());
        }
        JwsHeader jwsHeader = JwsHeader.with(hospital.security.SecurityUtils.JWT_ALGORITHM).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, builder.build())).getTokenValue();
    }

    private String fullName(User user) {
        String first = user.getFirstName() == null ? "" : user.getFirstName();
        String last = user.getLastName() == null ? "" : user.getLastName();
        String name = (first + " " + last).trim();
        return name.isBlank() ? user.getLogin() : name;
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        int idx = fullName.trim().lastIndexOf(' ');
        return idx > 0 ? fullName.substring(0, idx).trim() : fullName.trim();
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        int idx = fullName.trim().lastIndexOf(' ');
        return idx > 0 ? fullName.substring(idx + 1).trim() : "";
    }

    private boolean isStrongPassword(String password) {
        return (
            password != null &&
            password.length() >= 8 &&
            password.matches(".*[A-Z].*") &&
            password.matches(".*[a-z].*") &&
            password.matches(".*\\d.*") &&
            password.matches(".*[^A-Za-z0-9].*")
        );
    }

    private static Map<String, Object> error(String message, String code) {
        return Map.of("error", message, "code", code);
    }

    private ResponseCookie accessTokenCookie(String token) {
        return ResponseCookie.from("access_token", token)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(Duration.ofSeconds(tokenValidityInSeconds))
            .sameSite("Lax")
            .build();
    }

    private ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from("access_token", "").httpOnly(true).secure(false).path("/").maxAge(Duration.ZERO).sameSite("Lax").build();
    }

    public record RegisterRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        String phoneNumber,
        @NotBlank String password,
        @NotBlank String confirmPassword
    ) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record RefreshRequest(String token) {}
}
