package hospital.web.rest;

import hospital.domain.Appointment;
import hospital.domain.Doctor;
import hospital.domain.MedicalRecord;
import hospital.domain.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.DoctorRepository;
import hospital.repository.MedicalRecordRepository;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MedicalRecordResource {

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    public MedicalRecordResource(
        MedicalRecordRepository medicalRecordRepository,
        UserRepository userRepository,
        AppointmentRepository appointmentRepository,
        DoctorRepository doctorRepository
    ) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/medical-records")
    public ResponseEntity<PageResponseDTO<Map<String, Object>>> list(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit
    ) {
        String login = currentLogin();
        List<MedicalRecord> records = medicalRecordRepository.findByUserLoginOrderByCreatedAtDesc(login);
        Page<MedicalRecord> pageData = new PageImpl<>(
            slice(records, page, limit),
            PageRequest.of(Math.max(page - 1, 0), limit),
            records.size()
        );
        List<Map<String, Object>> data = pageData.getContent().stream().map(this::toMap).toList();
        return ResponseEntity.ok(
            new PageResponseDTO<>(data, new PaginationDTO(page, limit, pageData.getTotalElements(), pageData.getTotalPages()))
        );
    }

    @PostMapping("/medical-records")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "','ROLE_DOCTOR')")
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateMedicalRecordRequest request) {
        User user = userRepository
            .findOneByLogin(resolveLogin(request.userId()))
            .orElseThrow(() -> new IllegalStateException("User not found"));
        if (
            !SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN) &&
            !SecurityUtils.hasCurrentUserAnyOfAuthorities("ROLE_DOCTOR")
        ) {
            throw new IllegalStateException("Unauthorized");
        }
        MedicalRecord record = new MedicalRecord();
        record.setUser(user);
        if (request.appointmentId() != null) {
            Appointment appointment = appointmentRepository
                .findById(request.appointmentId())
                .orElseThrow(() -> new IllegalStateException("Appointment not found"));
            record.setAppointment(appointment);
            record.setDoctor(appointment.getDoctor());
        } else if (request.doctorId() != null) {
            Doctor doctor = doctorRepository.findById(request.doctorId()).orElseThrow(() -> new IllegalStateException("Doctor not found"));
            record.setDoctor(doctor);
        }
        record.setDiagnosis(request.diagnosis());
        record.setTreatment(request.treatment());
        record.setNotes(request.notes());
        medicalRecordRepository.save(record);
        Map<String, Object> response = toMap(record);
        response.put("createdAt", Instant.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String currentLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new IllegalStateException("Unauthorized"));
    }

    private String resolveLogin(Long userId) {
        if (userId == null) {
            return currentLogin();
        }
        return userRepository.findById(userId).map(User::getLogin).orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private Map<String, Object> toMap(MedicalRecord record) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", record.getId());
        map.put("appointmentId", record.getAppointment() != null ? record.getAppointment().getId() : null);
        map.put("doctorName", record.getDoctor() != null ? record.getDoctor().getFullName() : null);
        map.put("visitDate", record.getAppointment() != null ? record.getAppointment().getAppointmentDate() : null);
        map.put("diagnosis", record.getDiagnosis());
        map.put("treatment", record.getTreatment());
        map.put("notes", record.getNotes());
        map.put("attachments", List.of());
        return map;
    }

    private <T> List<T> slice(List<T> items, int page, int limit) {
        int fromIndex = Math.min(Math.max(page - 1, 0) * limit, items.size());
        int toIndex = Math.min(fromIndex + limit, items.size());
        return fromIndex >= toIndex ? List.of() : items.subList(fromIndex, toIndex);
    }

    public record CreateMedicalRecordRequest(
        Long userId,
        Long appointmentId,
        Long doctorId,
        String diagnosis,
        String treatment,
        String notes
    ) {}
}
