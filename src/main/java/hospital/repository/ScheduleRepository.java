package hospital.repository;

import hospital.domain.Schedule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Schedule entity.
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByDoctorIdOrderByWorkDateDesc(Long doctorId);

    List<Schedule> findByDoctorIdAndWorkDate(Long doctorId, LocalDate workDate);
}
