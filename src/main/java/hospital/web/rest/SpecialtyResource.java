package hospital.web.rest;

import hospital.domain.Specialty;
import hospital.repository.DoctorRepository;
import hospital.repository.SpecialtyRepository;
import hospital.service.dto.SpecialtyDTO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SpecialtyResource {

    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;

    public SpecialtyResource(SpecialtyRepository specialtyRepository, DoctorRepository doctorRepository) {
        this.specialtyRepository = specialtyRepository;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/specialties")
    public ResponseEntity<Map<String, Object>> listSpecialties() {
        List<SpecialtyDTO> data = specialtyRepository.findAll().stream().map(this::toDto).toList();
        return ResponseEntity.ok(Map.of("data", data));
    }

    private SpecialtyDTO toDto(Specialty specialty) {
        SpecialtyDTO dto = new SpecialtyDTO();
        dto.setId(specialty.getId());
        dto.setName(specialty.getName());
        dto.setVietnamName(specialty.getVietnamName());
        dto.setIcon(specialty.getIcon());
        dto.setDescription(specialty.getDescription());
        dto.setDoctorCount(
            (int) doctorRepository
                .findAll()
                .stream()
                .filter(d -> d.getSpecialty() != null && d.getSpecialty().getId().equals(specialty.getId()))
                .count()
        );
        return dto;
    }
}
