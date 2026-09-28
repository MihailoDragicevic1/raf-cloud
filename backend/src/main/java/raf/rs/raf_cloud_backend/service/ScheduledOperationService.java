package raf.rs.raf_cloud_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import raf.rs.raf_cloud_backend.dto.ScheduledOperationCreateRequestDTO;
import raf.rs.raf_cloud_backend.dto.ScheduledOperationDTO;
import raf.rs.raf_cloud_backend.entity.Machine;
import raf.rs.raf_cloud_backend.entity.ScheduledOperation;
import raf.rs.raf_cloud_backend.entity.ScheduledOperationStatus;
import raf.rs.raf_cloud_backend.repository.MachineRepository;
import raf.rs.raf_cloud_backend.repository.ScheduledOperationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScheduledOperationService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledOperationService.class);

    @Autowired
    private ScheduledOperationRepository scheduledOperationRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private MachineService machineService;

    public ScheduledOperationDTO createScheduledOperation(
            ScheduledOperationCreateRequestDTO request,
            Long userId) throws Exception {

        logger.info("Kreiram zakazanu operaciju za mašinu " + request.getMachineId());

        Optional<Machine> machineOpt = machineRepository.findById(request.getMachineId());
        if (machineOpt.isEmpty()) {
            throw new Exception("Mašina sa ID " + request.getMachineId() + " ne postoji");
        }

        Machine machine = machineOpt.get();
        if (!machine.getActive()) {
            throw new Exception("Mašina je obrisana");
        }

        if (request.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new Exception("Zakazano vreme mora biti u budućnosti");
        }

        ScheduledOperation operation = new ScheduledOperation();
        operation.setMachine(machine);
        operation.setOperation(request.getOperation());
        operation.setScheduledTime(request.getScheduledTime());
        operation.setStatus(ScheduledOperationStatus.PENDING);
        operation.setCreatedBy(userId);
        operation.setCreatedAt(LocalDateTime.now());

        ScheduledOperation saved = scheduledOperationRepository.save(operation);
        logger.info("Zakazana operacija kreirana sa ID: " + saved.getId());

        return convertToDTO(saved);
    }


    public List<ScheduledOperationDTO> getScheduledOperationsByMachineId(Long machineId) {
        logger.info("Preuzimam zakazane operacije za mašinu: " + machineId);

        List<ScheduledOperation> operations = scheduledOperationRepository.findByMachine_Id(machineId);

        return operations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public List<ScheduledOperationDTO> getScheduledOperationsByCreatedBy(Long userId) {
        logger.info("Preuzimam zakazane operacije za korisnika: " + userId);

        List<ScheduledOperation> operations = scheduledOperationRepository.findByCreatedBy(userId);

        return operations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public Optional<ScheduledOperationDTO> getScheduledOperationById(Long id) {
        logger.info("Preuzimam zakazanu operaciju sa ID: " + id);

        return scheduledOperationRepository.findById(id)
                .map(this::convertToDTO);
    }

    public void cancelScheduledOperation(Long id) throws Exception {
        logger.info("Otkazujem zakazanu operaciju sa ID: " + id);

        Optional<ScheduledOperation> operationOpt = scheduledOperationRepository.findById(id);
        if (operationOpt.isEmpty()) {
            throw new Exception("Zakazana operacija sa ID " + id + " ne postoji");
        }

        ScheduledOperation operation = operationOpt.get();

        if (operation.getStatus() != ScheduledOperationStatus.PENDING) {
            throw new Exception("Može se otkazati samo PENDING operacija (trenutni status: " + operation.getStatus() + ")");
        }

        scheduledOperationRepository.delete(operation);
        logger.info("Zakazana operacija sa ID " + id + " je otkazana");
    }

    @Scheduled(fixedDelay = 30000)
    public void executeScheduledOperations() {
        logger.info(" SCHEDULER: Proveravamo zakazane operacije ");

        LocalDateTime now = LocalDateTime.now();

        List<ScheduledOperation> pendingOperations =
                scheduledOperationRepository.findByStatusAndScheduledTimeBeforeOrderByScheduledTimeAsc(
                        ScheduledOperationStatus.PENDING,
                        now
                );

        if (pendingOperations.isEmpty()) {
            logger.info("Nema zakazanih operacija za izvršavanje");
            return;
        }

        logger.info("Pronađeno " + pendingOperations.size() + " operacija za izvršavanje");

        for (ScheduledOperation operation : pendingOperations) {
            executeOperationAsync(operation);
        }
    }

    @Async
    public void executeOperationAsync(ScheduledOperation operation) {
        logger.info(" ASYNC: Započinjam izvršavanje zakazane operacije ID: " + operation.getId());

        try {
            operation.setStatus(ScheduledOperationStatus.RUNNING);
            scheduledOperationRepository.save(operation);
            logger.info("Operacija " + operation.getId() + " je sada RUNNING");

            switch (operation.getOperation()) {

                case START:
                    logger.info("Pokušavam da upem mašinu ID: " + operation.getMachineId());
                    machineService.turnOn(operation.getMachineId());
                    break;

                case STOP:
                    logger.info("Pokušavam da ugasim mašinu ID: " + operation.getMachineId());
                    machineService.turnOff(operation.getMachineId());
                    break;

                case RESTART:
                    logger.info("Pokušavam da restartam mašinu ID: " + operation.getMachineId());
                    machineService.restart(operation.getMachineId());
                    break;

                default:
                    throw new Exception("Nepoznata operacija: " + operation.getOperation());
            }

            operation.setStatus(ScheduledOperationStatus.COMPLETED);
            operation.setExecutedAt(LocalDateTime.now());
            scheduledOperationRepository.save(operation);
            logger.info("Zakazana operacija ID " + operation.getId() + " je uspešno COMPLETED!");

        } catch (Exception e) {
            logger.error("Greška pri izvršavanju zakazane operacije ID " + operation.getId() + ": " + e.getMessage());

            operation.setStatus(ScheduledOperationStatus.FAILED);
            operation.setErrorMessage(e.getMessage());
            operation.setExecutedAt(LocalDateTime.now());
            scheduledOperationRepository.save(operation);

            logger.error("Zakazana operacija ID " + operation.getId() + " je FAILED - čuvan error message");
        }
    }

    private ScheduledOperationDTO convertToDTO(ScheduledOperation operation) {
        return new ScheduledOperationDTO(
                operation.getId(),
                operation.getMachineId(),
                operation.getOperation(),
                operation.getScheduledTime(),
                operation.getStatus(),
                operation.getCreatedBy(),
                operation.getErrorMessage(),
                operation.getCreatedAt(),
                operation.getExecutedAt()
        );
    }
}

