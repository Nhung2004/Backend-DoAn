package hospital.web.rest;

import hospital.domain.Schedule;
import hospital.domain.TimeSlot;
import hospital.repository.ScheduleRepository;
import hospital.repository.TimeSlotRepository;
import hospital.service.dto.PageResponseDTO;
import hospital.service.dto.PaginationDTO;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TimeSlotResource {

    private final TimeSlotRepository timeSlotRepository;
    private final ScheduleRepository scheduleRepository;

    public TimeSlotResource(TimeSlotRepository timeSlotRepository, ScheduleRepository scheduleRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @GetMapping("/time-slots")
    public ResponseEntity<PageResponseDTO<Map<String, Object>>> listTimeSlots(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) Long scheduleId
    ) {
        List<TimeSlot> timeSlots = scheduleId == null ? timeSlotRepository.findAll() : timeSlotRepository.findByScheduleId(scheduleId);
        timeSlots = timeSlots
            .stream()
            .sorted((left, right) -> {
                if (left.getSchedule() != null && right.getSchedule() != null) {
                    int dateCompare = left.getSchedule().getWorkDate().compareTo(right.getSchedule().getWorkDate());
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                }
                return left.getSlotTime().compareTo(right.getSlotTime());
            })
            .toList();

        Page<TimeSlot> pageData = new PageImpl<>(
            slice(timeSlots, page, limit),
            PageRequest.of(Math.max(page - 1, 0), limit),
            timeSlots.size()
        );
        List<Map<String, Object>> data = pageData.getContent().stream().map(this::toMap).toList();
        return ResponseEntity.ok(
            new PageResponseDTO<>(data, new PaginationDTO(page, limit, pageData.getTotalElements(), pageData.getTotalPages()))
        );
    }

    @GetMapping("/time-slots/{id}")
    public ResponseEntity<Map<String, Object>> getTimeSlot(@PathVariable Long id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id).orElseThrow(() -> new IllegalStateException("TimeSlot not found"));
        return ResponseEntity.ok(toMap(timeSlot));
    }

    @PostMapping("/time-slots")
    public ResponseEntity<Map<String, Object>> createTimeSlot(@RequestBody TimeSlotRequest request) {
        TimeSlot timeSlot = new TimeSlot();
        applyRequest(timeSlot, request);
        timeSlotRepository.save(timeSlot);
        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(timeSlot));
    }

    @PutMapping("/time-slots/{id}")
    public ResponseEntity<Map<String, Object>> updateTimeSlot(@PathVariable Long id, @RequestBody TimeSlotRequest request) {
        TimeSlot timeSlot = timeSlotRepository.findById(id).orElseThrow(() -> new IllegalStateException("TimeSlot not found"));
        applyRequest(timeSlot, request);
        timeSlotRepository.save(timeSlot);
        return ResponseEntity.ok(toMap(timeSlot));
    }

    @DeleteMapping("/time-slots/{id}")
    public ResponseEntity<Map<String, Object>> deleteTimeSlot(@PathVariable Long id) {
        timeSlotRepository.findById(id).orElseThrow(() -> new IllegalStateException("TimeSlot not found"));
        timeSlotRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("id", id, "message", "Time slot đã được xóa"));
    }

    private void applyRequest(TimeSlot timeSlot, TimeSlotRequest request) {
        if (request.scheduleId() != null) {
            Schedule schedule = scheduleRepository
                .findById(request.scheduleId())
                .orElseThrow(() -> new IllegalStateException("Schedule not found"));
            timeSlot.setSchedule(schedule);
        } else if (timeSlot.getId() == null) {
            throw new IllegalArgumentException("scheduleId is required");
        }
        timeSlot.setSlotTime(request.slotTime());
        timeSlot.setBooked(Optional.ofNullable(request.booked()).orElse(false));
    }

    private Map<String, Object> toMap(TimeSlot timeSlot) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", timeSlot.getId());
        map.put("scheduleId", timeSlot.getSchedule() != null ? timeSlot.getSchedule().getId() : null);
        map.put(
            "doctorId",
            timeSlot.getSchedule() != null && timeSlot.getSchedule().getDoctor() != null ? timeSlot.getSchedule().getDoctor().getId() : null
        );
        map.put("workDate", timeSlot.getSchedule() != null ? timeSlot.getSchedule().getWorkDate() : null);
        map.put("slotTime", timeSlot.getSlotTime());
        map.put("booked", timeSlot.getBooked());
        return map;
    }

    private <T> List<T> slice(List<T> items, int page, int limit) {
        int fromIndex = Math.min(Math.max(page - 1, 0) * limit, items.size());
        int toIndex = Math.min(fromIndex + limit, items.size());
        return fromIndex >= toIndex ? List.of() : items.subList(fromIndex, toIndex);
    }

    public record TimeSlotRequest(Long scheduleId, LocalTime slotTime, Boolean booked) {}
}
