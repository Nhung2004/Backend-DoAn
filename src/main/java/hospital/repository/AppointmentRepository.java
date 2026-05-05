package hospital.repository;

import hospital.domain.Appointment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Appointment entity.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {
    List<Appointment> findByUserLogin(String login);
    List<Appointment> findByDoctorIdAndAppointmentDateBetween(Long doctorId, java.time.LocalDate startDate, java.time.LocalDate endDate);
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, java.time.LocalDate appointmentDate);
}
