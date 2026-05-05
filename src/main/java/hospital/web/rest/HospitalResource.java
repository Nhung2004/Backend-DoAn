package hospital.web.rest;

import hospital.domain.Doctor;
import hospital.domain.Hospital;
import hospital.repository.DoctorRepository;
import hospital.repository.HospitalRepository;
import hospital.service.dto.PageResponseDTO;
import hospital.service.dto.PaginationDTO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HospitalResource {

    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;

    public HospitalResource(HospitalRepository hospitalRepository, DoctorRepository doctorRepository) {
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/hospitals")
    public ResponseEntity<PageResponseDTO<Map<String, Object>>> listHospitals(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "rating") String sortBy
    ) {
        Specification<Hospital> spec = Specification.where(null);
        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                cb.or(cb.like(cb.lower(root.get("name")), like), cb.like(cb.lower(root.get("address")), like))
            );
        }
        Page<Hospital> pageData = hospitalRepository.findAll(
            spec,
            PageRequest.of(Math.max(page - 1, 0), limit, Sort.by(Sort.Direction.DESC, sortBy))
        );
        List<Map<String, Object>> data = pageData.getContent().stream().map(this::toSummary).toList();
        return ResponseEntity.ok(
            new PageResponseDTO<>(data, new PaginationDTO(page, limit, pageData.getTotalElements(), pageData.getTotalPages()))
        );
    }

    @GetMapping("/hospitals/{id}")
    public ResponseEntity<Map<String, Object>> getHospital(@PathVariable Long id) {
        Hospital hospital = hospitalRepository.findById(id).orElseThrow(() -> new IllegalStateException("Hospital not found"));
        return ResponseEntity.ok(toDetail(hospital));
    }

    private Map<String, Object> toSummary(Hospital hospital) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", hospital.getId());
        map.put("name", hospital.getName());
        map.put("address", hospital.getAddress());
        map.put("phone", hospital.getPhone());
        map.put("email", hospital.getEmail());
        map.put("avatar", hospital.getAvatar());
        map.put("rating", hospital.getRating());
        map.put("reviewCount", hospital.getReviewCount());
        map.put("description", hospital.getDescription());
        List<Doctor> doctors = doctorRepository
            .findAll()
            .stream()
            .filter(d -> d.getHospital() != null && d.getHospital().getId().equals(hospital.getId()))
            .toList();
        map.put("doctorCount", doctors.size());
        map.put("serviceCount", 3);
        return map;
    }

    private Map<String, Object> toDetail(Hospital hospital) {
        Map<String, Object> map = toSummary(hospital);
        map.put("services", List.of("Khám tổng quát", "Chụp X-quang", "Siêu âm"));
        List<Map<String, Object>> doctors = doctorRepository
            .findAll()
            .stream()
            .filter(d -> d.getHospital() != null && d.getHospital().getId().equals(hospital.getId()))
            .map(this::doctorCard)
            .toList();
        map.put("doctors", doctors);
        return map;
    }

    private Map<String, Object> doctorCard(Doctor doctor) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", doctor.getId());
        map.put("fullName", doctor.getFullName());
        map.put("specialty", doctor.getSpecialty() != null ? doctor.getSpecialty().getName() : null);
        map.put("avatar", doctor.getAvatar());
        map.put("rating", doctor.getRating());
        return map;
    }
}
