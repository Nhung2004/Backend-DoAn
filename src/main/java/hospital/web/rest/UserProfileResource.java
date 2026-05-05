package hospital.web.rest;

import hospital.domain.User;
import hospital.repository.UserRepository;
import hospital.security.SecurityUtils;
import hospital.service.dto.UserProfileDTO;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserProfileResource {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResource(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser() {
        User user = currentUserOrThrow();
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(@RequestBody UpdateProfileRequest request) {
        User user = currentUserOrThrow();
        user.setFirstName(extractFirstName(request.fullName()));
        user.setLastName(extractLastName(request.fullName()));
        user.setPhoneNumber(request.phoneNumber());
        user.setDateOfBirth(request.dateOfBirth());
        user.setGender(request.gender());
        user.setAddress(request.address());
        user.setHealthInsurance(request.healthInsurance());
        userRepository.save(user);
        UserProfileDTO response = toDto(user);
        return ResponseEntity.ok(
            Map.of(
                "id",
                response.getId(),
                "email",
                response.getEmail(),
                "fullName",
                response.getFullName(),
                "message",
                "Thông tin cá nhân đã được cập nhật"
            )
        );
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody PasswordRequest request) {
        User user = currentUserOrThrow();
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid password", "code", "INVALID_PASSWORD"));
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("error", "Password confirmation does not match", "code", "PASSWORD_MISMATCH")
            );
        }
        if (!isStrongPassword(request.newPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                    "error",
                    "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character",
                    "code",
                    "WEAK_PASSWORD"
                )
            );
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Mật khẩu đã được cập nhật"));
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAvatar(@RequestParam("file") MultipartFile file) {
        User user = currentUserOrThrow();
        validateAvatarFile(file);
        String originalName = file.getOriginalFilename() == null
            ? "avatar"
            : Paths.get(file.getOriginalFilename()).getFileName().toString();
        Path avatarDir = Path.of("uploads", "avatars", String.valueOf(user.getId()));
        try {
            Files.createDirectories(avatarDir);
            Files.write(avatarDir.resolve(originalName), file.getBytes());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save avatar");
        }
        String avatarUrl = "/uploads/avatars/" + user.getId() + "/" + originalName;
        user.setImageUrl(avatarUrl);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("id", user.getId(), "avatar", avatarUrl, "message", "Ảnh đại diện đã được cập nhật"));
    }

    private User currentUserOrThrow() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new IllegalStateException("Current user not found"));
        return userRepository.findOneByLogin(login).orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private UserProfileDTO toDto(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(fullName(user));
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAvatar(user.getImageUrl());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setAddress(user.getAddress());
        dto.setHealthInsurance(user.getHealthInsurance());
        dto.setCreatedAt(user.getCreatedDate());
        return dto;
    }

    private String fullName(User user) {
        String first = user.getFirstName() == null ? "" : user.getFirstName();
        String last = user.getLastName() == null ? "" : user.getLastName();
        String fullName = (first + " " + last).trim();
        return fullName.isBlank() ? user.getLogin() : fullName;
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

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be <= 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    public record UpdateProfileRequest(
        String fullName,
        String phoneNumber,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String healthInsurance
    ) {}

    public record PasswordRequest(String oldPassword, String newPassword, String confirmPassword) {}
}
