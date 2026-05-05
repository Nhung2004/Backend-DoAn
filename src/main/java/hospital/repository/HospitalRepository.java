package hospital.repository;

import hospital.domain.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Hospital entity.
 */
@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long>, JpaSpecificationExecutor<Hospital> {}
