package raf.rs.raf_cloud_backend.repository;

import raf.rs.raf_cloud_backend.entity.Error;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ErrorRepository extends JpaRepository<Error, Long> {
    List<Error> findByMachineId(Long machineId);
    List<Error> findByUserId(Long userId);
}

