package raf.rs.raf_cloud_backend.repository;

import raf.rs.raf_cloud_backend.entity.ScheduledOperation;
import raf.rs.raf_cloud_backend.entity.ScheduledOperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledOperationRepository extends JpaRepository<ScheduledOperation, Long> {

    List<ScheduledOperation> findByMachine_Id(Long machineId);

    List<ScheduledOperation> findByCreatedBy(Long createdBy);

    List<ScheduledOperation> findByStatusAndScheduledTimeBeforeOrderByScheduledTimeAsc(
            ScheduledOperationStatus status, LocalDateTime now);

    List<ScheduledOperation> findByMachine_IdAndStatus(Long machineId, ScheduledOperationStatus status);
}
