package hospital.web.rest;

import hospital.domain.Appointment;
import hospital.domain.Payment;
import hospital.domain.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.PaymentRepository;
import hospital.repository.UserRepository;
import hospital.security.SecurityUtils;
import hospital.service.dto.PageResponseDTO;
import hospital.service.dto.PaginationDTO;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentResource {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public PaymentResource(
        PaymentRepository paymentRepository,
        AppointmentRepository appointmentRepository,
        UserRepository userRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/payments/process")
    public ResponseEntity<Map<String, Object>> process(@RequestBody ProcessPaymentRequest request) {
        User user = currentUser();
        Appointment appointment = appointmentRepository
            .findById(request.appointmentId())
            .orElseThrow(() -> new IllegalStateException("Appointment not found"));
        if (appointment.getUser() == null || !appointment.getUser().getLogin().equalsIgnoreCase(user.getLogin())) {
            throw new IllegalStateException("Unauthorized");
        }
        if (request.amount() == null || request.amount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAppointment(appointment);
        payment.setAmount(request.amount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus("SUCCESS");
        payment.setTransactionId("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        payment.setCreatedAt(Instant.now());
        paymentRepository.save(payment);
        appointment.setPaymentStatus("PAID");
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(toMap(payment));
    }

    @GetMapping("/payments")
    public ResponseEntity<PageResponseDTO<Map<String, Object>>> list(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        String login = currentLogin();
        List<Payment> payments = paymentRepository.findByUserLoginOrderByCreatedAtDesc(login);
        if (status != null && !status.isBlank()) {
            payments = payments.stream().filter(p -> p.getStatus() != null && p.getStatus().equalsIgnoreCase(status)).toList();
        }
        payments = payments
            .stream()
            .sorted((a, b) -> "asc".equalsIgnoreCase(sortOrder) ? compare(a, b, sortBy) : compare(b, a, sortBy))
            .toList();
        Page<Payment> pageData = new PageImpl<>(
            slice(payments, page, limit),
            PageRequest.of(Math.max(page - 1, 0), limit),
            payments.size()
        );
        List<Map<String, Object>> data = pageData.getContent().stream().map(this::toMap).toList();
        return ResponseEntity.ok(
            new PageResponseDTO<>(data, new PaginationDTO(page, limit, pageData.getTotalElements(), pageData.getTotalPages()))
        );
    }

    private User currentUser() {
        return userRepository.findOneByLogin(currentLogin()).orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private String currentLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new IllegalStateException("Unauthorized"));
    }

    private Map<String, Object> toMap(Payment payment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", payment.getId());
        map.put("appointmentId", payment.getAppointment() != null ? payment.getAppointment().getId() : null);
        map.put(
            "doctorName",
            payment.getAppointment() != null && payment.getAppointment().getDoctor() != null
                ? payment.getAppointment().getDoctor().getFullName()
                : null
        );
        map.put("amount", payment.getAmount());
        map.put("paymentMethod", payment.getPaymentMethod());
        map.put("status", payment.getStatus());
        map.put("transactionId", payment.getTransactionId());
        map.put("createdAt", payment.getCreatedAt());
        return map;
    }

    private <T> List<T> slice(List<T> items, int page, int limit) {
        int fromIndex = Math.min(Math.max(page - 1, 0) * limit, items.size());
        int toIndex = Math.min(fromIndex + limit, items.size());
        return fromIndex >= toIndex ? List.of() : items.subList(fromIndex, toIndex);
    }

    private int compare(Payment left, Payment right, String sortBy) {
        if ("amount".equalsIgnoreCase(sortBy)) {
            return Long.compare(left.getAmount() == null ? 0L : left.getAmount(), right.getAmount() == null ? 0L : right.getAmount());
        }
        if ("status".equalsIgnoreCase(sortBy)) {
            return String.valueOf(left.getStatus()).compareToIgnoreCase(String.valueOf(right.getStatus()));
        }
        return left.getCreatedAt() == null || right.getCreatedAt() == null ? 0 : left.getCreatedAt().compareTo(right.getCreatedAt());
    }

    public record ProcessPaymentRequest(
        Long appointmentId,
        Long amount,
        String paymentMethod,
        String cardNumber,
        String cardHolder,
        String expiryDate,
        String cvv
    ) {}
}
