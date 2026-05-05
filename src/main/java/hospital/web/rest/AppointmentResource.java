package hospital.web.rest;

import hospital.domain.Appointment;
import hospital.domain.Doctor;
import hospital.domain.Hospital;
import hospital.domain.User;
import hospital.domain.enumeration.AppointmentStatus;
import hospital.repository.AppointmentRepository;
import hospital.repository.DoctorRepository;
import hospital.repository.HospitalRepository;
import hospital.repository.UserRepository;
import hospital.security.SecurityUtils;
import hospital.service.dto.AppointmentDTO;
import hospital.service.dto.PageResponseDTO;
import hospital.service.dto.PaginationDTO;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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
public class AppointmentResource {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;

    public AppointmentResource(
        AppointmentRepository appointmentRepository,
        DoctorRepository doctorRepository,
        HospitalRepository hospitalRepository,
        UserRepository userRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentDTO> createAppointment(@RequestBody CreateAppointmentRequest request) {
        User user = currentUser();
        Doctor doctor = doctorRepository.findById(request.doctorId()).orElseThrow(() -> new IllegalStateException("Doctor not found"));
        Hospital hospital = resolveHospital(request.hospitalId(), doctor);
        LocalTime requestedTime = LocalTime.parse(request.appointmentTime());
        ensureSlotAvailable(doctor.getId(), request.appointmentDate(), requestedTime, null);

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setDoctor(doctor);
        appointment.setHospital(hospital);
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setAppointmentTime(requestedTime);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setReason(request.reason());
        appointment.setNotes(request.notes());
        appointment.setPrice(doctor.getPrice());
        appointment.setPaymentStatus("UNPAID");
        appointmentRepository.save(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(appointment));
    }

    @GetMapping("/appointments")
    public ResponseEntity<PageResponseDTO<AppointmentDTO>> listAppointments(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "date") String sortBy,
        @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        String login = currentLogin();
        List<Appointment> appointments = appointmentRepository.findByUserLogin(login);
        if (status != null && !status.isBlank()) {
            appointments = appointments
                .stream()
                .filter(a -> a.getStatus() != null && a.getStatus().name().equalsIgnoreCase(status))
                .toList();
        }
        appointments = appointments.stream().sorted(sorter(sortBy, sortOrder)).toList();
        Page<AppointmentDTO> dtoPage = sliceAppointments(appointments, page, limit).map(this::toDto);
        return ResponseEntity.ok(
            new PageResponseDTO<>(dtoPage.getContent(), new PaginationDTO(page, limit, dtoPage.getTotalElements(), dtoPage.getTotalPages()))
        );
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<AppointmentDTO> getAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new IllegalStateException("Appointment not found"));
        ensureCanAccess(appointment);
        return ResponseEntity.ok(toDetailDto(appointment));
    }

    @PutMapping("/appointments/{id}")
    public ResponseEntity<AppointmentDTO> updateAppointment(@PathVariable Long id, @RequestBody UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new IllegalStateException("Appointment not found"));
        ensureCanAccess(appointment);
        LocalTime requestedTime = LocalTime.parse(request.appointmentTime());
        ensureSlotAvailable(appointment.getDoctor().getId(), request.appointmentDate(), requestedTime, appointment.getId());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setAppointmentTime(requestedTime);
        appointmentRepository.save(appointment);
        AppointmentDTO dto = toDto(appointment);
        dto.setMessage("Lịch khám đã được cập nhật");
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<AppointmentDTO> cancelAppointment(
        @PathVariable Long id,
        @RequestBody(required = false) CancelAppointmentRequest request
    ) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new IllegalStateException("Appointment not found"));
        ensureCanAccess(appointment);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (request != null && request.reason() != null && !request.reason().isBlank()) {
            appointment.setNotes(
                (appointment.getNotes() == null ? "" : appointment.getNotes() + "\n") + "Cancel reason: " + request.reason()
            );
        }
        appointmentRepository.save(appointment);
        AppointmentDTO dto = toDto(appointment);
        dto.setMessage("Lịch khám đã được hủy");
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/appointments/{doctorId}/available-slots")
    public ResponseEntity<AppointmentDTO> availableSlots(
        @PathVariable Long doctorId,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() -> new IllegalStateException("Doctor not found"));
        AppointmentDTO dto = new AppointmentDTO();
        dto.setDoctorId(doctorId);
        dto.setDoctorName(doctor.getFullName());
        List<Map<String, Object>> slots = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", cursor.toString());
            day.put("slots", generateSlots(cursor, doctorId));
            slots.add(day);
            cursor = cursor.plusDays(1);
        }
        dto.setAvailableSlots(slots);
        return ResponseEntity.ok(dto);
    }

    private List<String> generateSlots(LocalDate date, Long doctorId) {
        List<String> baseSlots = List.of("08:00", "08:30", "09:00", "09:30", "10:00", "14:00", "14:30", "15:00", "15:30", "16:00");
        List<String> booked = appointmentRepository
            .findByDoctorIdAndAppointmentDate(doctorId, date)
            .stream()
            .map(a -> a.getAppointmentTime().toString().substring(0, 5))
            .toList();
        return baseSlots.stream().filter(slot -> !booked.contains(slot)).toList();
    }

    private Page<Appointment> sliceAppointments(List<Appointment> appointments, int page, int limit) {
        int fromIndex = Math.min(Math.max(page - 1, 0) * limit, appointments.size());
        int toIndex = Math.min(fromIndex + limit, appointments.size());
        List<Appointment> content = fromIndex >= toIndex ? List.of() : appointments.subList(fromIndex, toIndex);
        return new PageImpl<>(content, PageRequest.of(Math.max(page - 1, 0), limit), appointments.size());
    }

    private java.util.Comparator<Appointment> sorter(String sortBy, String sortOrder) {
        java.util.Comparator<Appointment> comparator;
        if ("status".equalsIgnoreCase(sortBy)) {
            comparator = java.util.Comparator.comparing(a -> a.getStatus() == null ? "" : a.getStatus().name());
        } else {
            comparator = java.util.Comparator.comparing(Appointment::getAppointmentDate).thenComparing(Appointment::getAppointmentTime);
        }
        return "desc".equalsIgnoreCase(sortOrder) ? comparator.reversed() : comparator;
    }

    private AppointmentDTO toDto(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setDoctorId(appointment.getDoctor() != null ? appointment.getDoctor().getId() : null);
        dto.setDoctorName(appointment.getDoctor() != null ? appointment.getDoctor().getFullName() : null);
        dto.setDoctorPhone(appointment.getDoctor() != null ? appointment.getDoctor().getPhoneNumber() : null);
        dto.setHospitalId(appointment.getHospital() != null ? appointment.getHospital().getId() : null);
        dto.setHospitalName(appointment.getHospital() != null ? appointment.getHospital().getName() : null);
        dto.setHospitalAddress(appointment.getHospital() != null ? appointment.getHospital().getAddress() : null);
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus() != null ? appointment.getStatus().name() : null);
        dto.setReason(appointment.getReason());
        dto.setNotes(appointment.getNotes());
        dto.setPrice(appointment.getPrice());
        dto.setPaymentStatus(appointment.getPaymentStatus());
        dto.setCreatedAt(appointment.getCreatedAt());
        return dto;
    }

    private AppointmentDTO toDetailDto(Appointment appointment) {
        AppointmentDTO dto = toDto(appointment);
        dto.setPaymentStatus(appointment.getPaymentStatus());
        return dto;
    }

    private User currentUser() {
        String login = currentLogin();
        return userRepository.findOneByLogin(login).orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private String currentLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new IllegalStateException("Unauthorized"));
    }

    private Hospital resolveHospital(Long hospitalId, Doctor doctor) {
        if (hospitalId != null) {
            return hospitalRepository.findById(hospitalId).orElseThrow(() -> new IllegalStateException("Hospital not found"));
        }
        return doctor.getHospital();
    }

    private void ensureCanAccess(Appointment appointment) {
        String login = currentLogin();
        boolean owner = appointment.getUser() != null && login.equalsIgnoreCase(appointment.getUser().getLogin());
        boolean admin = SecurityUtils.hasCurrentUserAnyOfAuthorities("ROLE_ADMIN");
        if (!owner && !admin) {
            throw new IllegalStateException("Unauthorized");
        }
    }

    private void ensureSlotAvailable(Long doctorId, LocalDate date, LocalTime time, Long excludeAppointmentId) {
        boolean booked = appointmentRepository
            .findByDoctorIdAndAppointmentDate(doctorId, date)
            .stream()
            .filter(a -> excludeAppointmentId == null || !excludeAppointmentId.equals(a.getId()))
            .anyMatch(
                a -> a.getAppointmentTime() != null && a.getAppointmentTime().equals(time) && a.getStatus() != AppointmentStatus.CANCELLED
            );
        if (booked) {
            throw new IllegalArgumentException("Selected appointment slot is not available");
        }
    }

    public record CreateAppointmentRequest(
        Long doctorId,
        Long hospitalId,
        LocalDate appointmentDate,
        String appointmentTime,
        String reason,
        String notes
    ) {}

    public record UpdateAppointmentRequest(LocalDate appointmentDate, String appointmentTime) {}

    public record CancelAppointmentRequest(String reason) {}
}
