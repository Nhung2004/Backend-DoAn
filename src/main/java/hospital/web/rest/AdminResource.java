package hospital.web.rest;

import hospital.domain.Appointment;
import hospital.domain.Doctor;
import hospital.domain.Hospital;
import hospital.domain.Specialty;
import hospital.repository.AppointmentRepository;
import hospital.repository.DoctorRepository;
import hospital.repository.HospitalRepository;
import hospital.repository.PaymentRepository;
import hospital.repository.SpecialtyRepository;
import hospital.repository.UserRepository;
import hospital.service.dto.PageResponseDTO;
import hospital.service.dto.PaginationDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminResource {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final HospitalRepository hospitalRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;

    public AdminResource(
        UserRepository userRepository,
        DoctorRepository doctorRepository,
        SpecialtyRepository specialtyRepository,
        HospitalRepository hospitalRepository,
        AppointmentRepository appointmentRepository,
        PaymentRepository paymentRepository
    ) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
        this.hospitalRepository = hospitalRepository;
        this.appointmentRepository = appointmentRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Map<String, Object> statistics = new LinkedHashMap<>();
        statistics.put("totalUsers", userRepository.count());
        statistics.put("totalDoctors", doctorRepository.count());
        statistics.put("totalAppointments", appointmentRepository.count());
        statistics.put(
            "totalRevenue",
            paymentRepository
                .findAll()
                .stream()
                .filter(p -> "SUCCESS".equalsIgnoreCase(p.getStatus()))
                .mapToLong(p -> p.getAmount() == null ? 0L : p.getAmount())
                .sum()
        );
        statistics.put("monthlyAppointments", appointmentRepository.count());
        statistics.put("monthlyRevenue", statistics.get("totalRevenue"));

        Map<String, Object> appointmentStatuses = new LinkedHashMap<>();
        appointmentStatuses.put(
            "PENDING",
            appointmentRepository.findAll().stream().filter(a -> a.getStatus() != null && "PENDING".equals(a.getStatus().name())).count()
        );
        appointmentStatuses.put(
            "CONFIRMED",
            appointmentRepository.findAll().stream().filter(a -> a.getStatus() != null && "CONFIRMED".equals(a.getStatus().name())).count()
        );
        appointmentStatuses.put(
            "COMPLETED",
            appointmentRepository.findAll().stream().filter(a -> a.getStatus() != null && "COMPLETED".equals(a.getStatus().name())).count()
        );
        appointmentStatuses.put(
            "CANCELLED",
            appointmentRepository.findAll().stream().filter(a -> a.getStatus() != null && "CANCELLED".equals(a.getStatus().name())).count()
        );
        statistics.put("appointmentStatuses", appointmentStatuses);

        return ResponseEntity.ok(Map.of("statistics", statistics));
    }

    @GetMapping("/doctors")
    public ResponseEntity<PageResponseDTO<Map<String, Object>>> listDoctors(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(required = false) String search
    ) {
        List<Doctor> doctors = doctorRepository.findAll();
        if (search != null && !search.isBlank()) {
            String like = search.toLowerCase();
            doctors = doctors
                .stream()
                .filter(
                    d ->
                        (d.getFullName() != null && d.getFullName().toLowerCase().contains(like)) ||
                        (d.getEmail() != null && d.getEmail().toLowerCase().contains(like))
                )
                .toList();
        }
        Page<Doctor> pageData = new org.springframework.data.domain.PageImpl<>(
            slice(doctors, page, limit),
            PageRequest.of(Math.max(page - 1, 0), limit),
            doctors.size()
        );
        List<Map<String, Object>> data = pageData.getContent().stream().map(this::doctorSummary).toList();
        return ResponseEntity.ok(
            new PageResponseDTO<>(data, new PaginationDTO(page, limit, pageData.getTotalElements(), pageData.getTotalPages()))
        );
    }

    @GetMapping("/doctors/{id}")
    public ResponseEntity<Map<String, Object>> getDoctor(@PathVariable Long id) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new IllegalStateException("Doctor not found"));
        return ResponseEntity.ok(doctorDetail(doctor));
    }

    @PostMapping("/doctors")
    public ResponseEntity<Map<String, Object>> createDoctor(@Valid @RequestBody DoctorRequest request) {
        Doctor doctor = new Doctor();
        applyDoctorRequest(doctor, request);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            Map.of("message", "Bác sĩ đã được thêm mới", "doctor", doctorDetail(savedDoctor))
        );
    }

    @PutMapping("/doctors/{id}")
    public ResponseEntity<Map<String, Object>> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new IllegalStateException("Doctor not found"));
        applyDoctorRequest(doctor, request);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return ResponseEntity.ok(Map.of("message", "Bác sĩ đã được cập nhật", "doctor", doctorDetail(savedDoctor)));
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<Map<String, Object>> deleteDoctor(@PathVariable Long id) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new IllegalStateException("Doctor not found"));
        try {
            doctorRepository.delete(doctor);
            doctorRepository.flush();
            return ResponseEntity.ok(Map.of("id", id, "message", "Bác sĩ đã được xóa"));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("error", "Không thể xóa bác sĩ vì còn dữ liệu liên quan", "code", "DOCTOR_DELETE_CONFLICT")
            );
        }
    }

    @GetMapping("/appointments")
    public ResponseEntity<PageResponseDTO<Map<String, Object>>> listAppointments(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "date") String sortBy
    ) {
        List<Appointment> appointments = appointmentRepository.findAll();
        if (status != null && !status.isBlank()) {
            appointments = appointments
                .stream()
                .filter(a -> a.getStatus() != null && a.getStatus().name().equalsIgnoreCase(status))
                .toList();
        }
        appointments = appointments.stream().sorted((a, b) -> a.getAppointmentDate().compareTo(b.getAppointmentDate())).toList();
        Page<Appointment> pageData = new org.springframework.data.domain.PageImpl<>(
            slice(appointments, page, limit),
            PageRequest.of(Math.max(page - 1, 0), limit),
            appointments.size()
        );
        List<Map<String, Object>> data = pageData.getContent().stream().map(this::appointmentSummary).toList();
        return ResponseEntity.ok(
            new PageResponseDTO<>(data, new PaginationDTO(page, limit, pageData.getTotalElements(), pageData.getTotalPages()))
        );
    }

    @PutMapping("/appointments/{id}")
    public ResponseEntity<Map<String, Object>> updateAppointment(
        @PathVariable Long id,
        @RequestBody UpdateAppointmentStatusRequest request
    ) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new IllegalStateException("Appointment not found"));
        appointment.setStatus(hospital.domain.enumeration.AppointmentStatus.valueOf(request.status()));
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(
            Map.of("id", id, "status", appointment.getStatus().name(), "message", "Trạng thái lịch khám đã được cập nhật")
        );
    }

    private Map<String, Object> doctorSummary(Doctor doctor) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", doctor.getId());
        map.put("fullName", doctor.getFullName());
        map.put("email", doctor.getEmail());
        map.put("specialty", doctor.getSpecialty() != null ? doctor.getSpecialty().getName() : null);
        map.put("hospital", doctor.getHospital() != null ? doctor.getHospital().getName() : null);
        map.put(
            "appointments",
            appointmentRepository
                .findAll()
                .stream()
                .filter(a -> a.getDoctor() != null && a.getDoctor().getId().equals(doctor.getId()))
                .count()
        );
        map.put("rating", doctor.getRating());
        return map;
    }

    private Map<String, Object> doctorDetail(Doctor doctor) {
        Map<String, Object> specialty = new LinkedHashMap<>();
        if (doctor.getSpecialty() != null) {
            specialty.put("id", doctor.getSpecialty().getId());
            specialty.put("name", doctor.getSpecialty().getName());
            specialty.put("vietnamName", doctor.getSpecialty().getVietnamName());
        }

        Map<String, Object> hospital = new LinkedHashMap<>();
        if (doctor.getHospital() != null) {
            hospital.put("id", doctor.getHospital().getId());
            hospital.put("name", doctor.getHospital().getName());
            hospital.put("address", doctor.getHospital().getAddress());
            hospital.put("phone", doctor.getHospital().getPhone());
            hospital.put("email", doctor.getHospital().getEmail());
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", doctor.getId());
        map.put("fullName", doctor.getFullName());
        map.put("email", doctor.getEmail());
        map.put("phoneNumber", doctor.getPhoneNumber());
        map.put("bio", doctor.getBio());
        map.put("avatar", doctor.getAvatar());
        map.put("experience", doctor.getExperience());
        map.put("license", doctor.getLicense());
        map.put("price", doctor.getPrice());
        map.put("rating", doctor.getRating());
        map.put("reviewCount", doctor.getReviewCount());
        map.put("specialty", specialty);
        map.put("hospital", hospital);
        map.put(
            "appointments",
            appointmentRepository
                .findAll()
                .stream()
                .filter(a -> a.getDoctor() != null && a.getDoctor().getId().equals(doctor.getId()))
                .count()
        );
        return map;
    }

    private Map<String, Object> appointmentSummary(Appointment appointment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", appointment.getId());
        map.put("userId", appointment.getUser() != null ? appointment.getUser().getId() : null);
        map.put("userName", appointment.getUser() != null ? appointment.getUser().getFirstName() : null);
        map.put("doctorId", appointment.getDoctor() != null ? appointment.getDoctor().getId() : null);
        map.put("doctorName", appointment.getDoctor() != null ? appointment.getDoctor().getFullName() : null);
        map.put("appointmentDate", appointment.getAppointmentDate());
        map.put("appointmentTime", appointment.getAppointmentTime());
        map.put("status", appointment.getStatus() != null ? appointment.getStatus().name() : null);
        map.put("price", appointment.getPrice());
        return map;
    }

    private void applyDoctorRequest(Doctor doctor, DoctorRequest request) {
        doctor.setFullName(request.fullName().trim());
        doctor.setEmail(request.email());
        doctor.setPhoneNumber(request.phoneNumber());
        doctor.setBio(request.bio());
        doctor.setAvatar(request.avatar());
        doctor.setExperience(request.experience());
        doctor.setLicense(request.license());
        doctor.setPrice(request.price());
        doctor.setRating(request.rating());
        doctor.setReviewCount(request.reviewCount());

        Specialty specialty = specialtyRepository
            .findById(request.specialtyId())
            .orElseThrow(() -> new IllegalStateException("Specialty not found"));
        Hospital hospital = hospitalRepository
            .findById(request.hospitalId())
            .orElseThrow(() -> new IllegalStateException("Hospital not found"));

        doctor.setSpecialty(specialty);
        doctor.setHospital(hospital);
    }

    private <T> List<T> slice(List<T> items, int page, int limit) {
        int fromIndex = Math.min(Math.max(page - 1, 0) * limit, items.size());
        int toIndex = Math.min(fromIndex + limit, items.size());
        return fromIndex >= toIndex ? List.of() : items.subList(fromIndex, toIndex);
    }

    public record UpdateAppointmentStatusRequest(String status) {}

    public record DoctorRequest(
        @NotBlank String fullName,
        String email,
        String phoneNumber,
        String bio,
        String avatar,
        Integer experience,
        String license,
        Long price,
        Double rating,
        Integer reviewCount,
        @NotNull Long specialtyId,
        @NotNull Long hospitalId
    ) {}
}
