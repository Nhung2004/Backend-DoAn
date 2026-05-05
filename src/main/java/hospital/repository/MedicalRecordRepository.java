package hospital.repository;

import hospital.domain.MedicalRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long>, JpaSpecificationExecutor<MedicalRecord> {
    List<MedicalRecord> findByUserLoginOrderByCreatedAtDesc(String login);
}
