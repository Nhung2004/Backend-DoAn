package hospital.web.rest;

import hospital.domain.Appointment;
import hospital.domain.Doctor;
import hospital.domain.Review;
import hospital.domain.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.DoctorRepository;
import hospital.repository.ReviewRepository;
import hospital.repository.UserRepository;
import hospital.security.AuthoritiesConstants;
import hospital.security.SecurityUtils;
import hospital.service.dto.PageResponseDTO;
import hospital.service.dto.PaginationDTO;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReviewResource {

    private final ReviewRepository reviewRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public ReviewResource(
        ReviewRepository reviewRepository,
        DoctorRepository doctorRepository,
        UserRepository userRepository,
        AppointmentRepository appointmentRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @PostMapping("/reviews")
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateReviewRequest request) {
        User user = currentUser();
        Doctor doctor = doctorRepository.findById(request.doctorId()).orElseThrow(() -> new IllegalStateException("Doctor not found"));
        if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (request.appointmentId() != null) {
            Appointment appointment = appointmentRepository
                .findById(request.appointmentId())
                .orElseThrow(() -> new IllegalStateException("Appointment not found"));
            boolean owner = appointment.getUser() != null && appointment.getUser().getLogin().equalsIgnoreCase(user.getLogin());
            boolean admin = SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN);
            if (!owner && !admin) {
                throw new IllegalStateException("Unauthorized");
            }
        }
        Review review = new Review();
        review.setUser(user);
        review.setDoctor(doctor);
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setCreatedAt(Instant.now());
        reviewRepository.save(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(review));
    }

    @GetMapping("/reviews")
    public ResponseEntity<?> list() {
        String login = currentLogin();
        List<Map<String, Object>> data = reviewRepository.findByUserLoginOrderByCreatedAtDesc(login).stream().map(this::toMap).toList();
        return ResponseEntity.ok(Map.of("data", data));
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody UpdateReviewRequest request) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new IllegalStateException("Review not found"));
        ensureCanModify(review);
        if (request.rating() != null && (request.rating() < 1 || request.rating() > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        review.setRating(request.rating());
        review.setComment(request.comment());
        reviewRepository.save(review);
        Map<String, Object> response = toMap(review);
        response.put("updatedAt", Instant.now());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new IllegalStateException("Review not found"));
        ensureCanModify(review);
        reviewRepository.delete(review);
        return ResponseEntity.ok(Map.of("message", "Đánh giá đã được xóa"));
    }

    private User currentUser() {
        String login = currentLogin();
        return userRepository.findOneByLogin(login).orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private String currentLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new IllegalStateException("Unauthorized"));
    }

    private void ensureCanModify(Review review) {
        String login = currentLogin();
        boolean owner = review.getUser() != null && review.getUser().getLogin().equalsIgnoreCase(login);
        boolean admin = SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN);
        if (!owner && !admin) {
            throw new IllegalStateException("Unauthorized");
        }
    }

    private Map<String, Object> toMap(Review review) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", review.getId());
        map.put("doctorId", review.getDoctor() != null ? review.getDoctor().getId() : null);
        map.put("doctorName", review.getDoctor() != null ? review.getDoctor().getFullName() : null);
        map.put("userId", review.getUser() != null ? review.getUser().getId() : null);
        map.put("rating", review.getRating());
        map.put("comment", review.getComment());
        map.put("createdAt", review.getCreatedAt());
        return map;
    }

    public record CreateReviewRequest(Long doctorId, Long appointmentId, Integer rating, String comment) {}

    public record UpdateReviewRequest(Integer rating, String comment) {}
}
