package hospital.repository;

import hospital.domain.TimeSlot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TimeSlot entity.
 */
@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByScheduleIdOrderBySlotTimeAsc(Long scheduleId);

    List<TimeSlot> findByScheduleId(Long scheduleId);

    long countByScheduleId(Long scheduleId);
}
