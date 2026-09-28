package raf.rs.raf_cloud_backend.repository;

import raf.rs.raf_cloud_backend.entity.Machine;
import raf.rs.raf_cloud_backend.entity.MachineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
    List<Machine> findByCreatedByAndActiveTrue(Long createdBy);
    List<Machine> findByActiveTrue();
    List<Machine> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    List<Machine> findByStatusAndActiveTrue(MachineStatus status);
}
