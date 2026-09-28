package raf.rs.raf_cloud_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import raf.rs.raf_cloud_backend.dto.MachineCreateRequestDTO;
import raf.rs.raf_cloud_backend.dto.MachineDTO;
import raf.rs.raf_cloud_backend.entity.*;
import raf.rs.raf_cloud_backend.exception.InvalidOperationException;
import raf.rs.raf_cloud_backend.exception.ResourceNotFoundException;
import raf.rs.raf_cloud_backend.repository.MachineRepository;
import raf.rs.raf_cloud_backend.repository.ScheduledOperationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MachineService {

    private static final Logger logger = LoggerFactory.getLogger(MachineService.class);

    @Autowired
    private MachineRepository machineRepository;
    @Autowired
    private WebSocketBroadcaster webSocketBroadcaster;

    public List<MachineDTO> getAllMachines(Long userId, boolean isAdmin) {
        List<Machine> machines;
        if (isAdmin) {
            machines = machineRepository.findByActiveTrue();
            logger.info("Admin preuzima sve mašine. Pronađeno: " + machines.size());
        } else {
            machines = machineRepository.findByCreatedByAndActiveTrue(userId);
            logger.info("Korisnik " + userId + " preuzima svoje mašine. Pronađeno: " + machines.size());
        }
        return machines.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public MachineDTO getMachineById(Long id) {
        logger.info("Preuzimam mašinu sa ID: " + id);

        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mašina sa ID " + id + " ne postoji"));

        if (!machine.getActive()) {
            throw new ResourceNotFoundException("Mašina je obrisana");
        }

        return convertToDTO(machine);
    }

    public MachineDTO createMachine(MachineCreateRequestDTO request, Long userId) {
        logger.info("Kreiram novu mašinu: " + request.getName() + " za korisnika: " + userId);

        Machine machine = new Machine();
        machine.setName(request.getName());
        machine.setType(request.getType());
        machine.setDescription(request.getDescription());
        machine.setStatus(MachineStatus.POWERED_OFF);
        machine.setCreatedBy(userId);
        machine.setActive(true);
        machine.setCreatedAt(LocalDateTime.now());
        machine.setIsBusy(false);

        Machine saved = machineRepository.save(machine);
        logger.info("Mašina uspešno kreirana sa ID: " + saved.getId());

        webSocketBroadcaster.notifyMachineCreated(saved.getId(), saved.getName());
        return convertToDTO(saved);
    }

    public void deleteMachine(Long id) {
        logger.info("Brišem mašinu sa ID: " + id);
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mašina sa ID " + id + " ne postoji"));

        if (!machine.getActive()) {
            throw new InvalidOperationException("Mašina je već obrisana");
        }

        machine.setActive(false);
        machineRepository.save(machine);
        logger.info("Mašina sa ID " + id + " je uspešno obrisana");

        webSocketBroadcaster.notifyMachineDeleted(id);
    }


    @Async
    public void turnOn(Long id) {
        logger.info("Pokušavam da upem mašinu sa ID: " + id);
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mašina sa ID " + id + " ne postoji"));

        if (!machine.getActive()) {
            throw new InvalidOperationException("Mašina je obrisana");
        }
        if (machine.getStatus() != MachineStatus.POWERED_OFF) {
            throw new InvalidOperationException("Mašina mora biti gašena da bi se mogla upaliti");
        }
        if (machine.getIsBusy()) {
            throw new InvalidOperationException("Operacija je već u toku");
        }

        MachineStatus previousStatus = machine.getStatus();
        machine.setIsBusy(true);
        machineRepository.save(machine);
        logger.info("Mašina " + id + " počinje sa paljenjem...");

        try {
            Thread.sleep(2000);
            machine.setStatus(MachineStatus.POWERED_ON);
            machine.setIsBusy(false);
            machineRepository.save(machine);

            webSocketBroadcaster.notifyMachineStatusChanged(id, MachineStatus.POWERED_ON, previousStatus);

            logger.info("Mašina " + id + " je uspešno upaljena!");
        } catch (InterruptedException e) {
            logger.error("Greška pri upaljenju mašine: " + e.getMessage());
            machine.setIsBusy(false);
            machineRepository.save(machine);
            Thread.currentThread().interrupt();
            webSocketBroadcaster.notifyMachineError(id, e.getMessage());
        }
    }


    @Async
    public void turnOff(Long id) {
        logger.info("Pokušavam da ugasim mašinu sa ID: " + id);
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mašina sa ID " + id + " ne postoji"));

        if (!machine.getActive()) {
            throw new InvalidOperationException("Mašina je obrisana");
        }
        if (machine.getStatus() != MachineStatus.POWERED_ON) {
            throw new InvalidOperationException("Mašina mora biti upaljena da bi se mogla ugasiti");
        }
        if (machine.getIsBusy()) {
            throw new InvalidOperationException("Operacija je već u toku");
        }

        MachineStatus previousStatus = machine.getStatus();
        machine.setIsBusy(true);
        machineRepository.save(machine);
        logger.info("Mašina " + id + " počinje sa gašenjem...");

        try {
            Thread.sleep(2000);
            machine.setStatus(MachineStatus.POWERED_OFF);
            machine.setIsBusy(false);
            machineRepository.save(machine);

            webSocketBroadcaster.notifyMachineStatusChanged(id, MachineStatus.POWERED_OFF, previousStatus);

            logger.info("Mašina " + id + " je uspešno ugašena!");
        } catch (InterruptedException e) {
            logger.error("Greška pri gašenju mašine: " + e.getMessage());
            machine.setIsBusy(false);
            machineRepository.save(machine);
            Thread.currentThread().interrupt();
        }
    }

    @Async
    public void restart(Long id) {
        logger.info("Pokušavam da restartam mašinu sa ID: " + id);
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mašina sa ID " + id + " ne postoji"));

        if (!machine.getActive()) {
            throw new InvalidOperationException("Mašina je obrisana");
        }
        if (machine.getStatus() != MachineStatus.POWERED_ON) {
            throw new InvalidOperationException("Mašina mora biti upaljena da bi se mogla restartati");
        }
        if (machine.getIsBusy()) {
            throw new InvalidOperationException("Operacija je već u toku");
        }

        MachineStatus previousStatus = machine.getStatus();
        machine.setIsBusy(true);
        machineRepository.save(machine);
        logger.info("Mašina " + id + " počinje sa restartovanjem...");

        try {
            logger.info("Mašina " + id + " se gasi...");
            Thread.sleep(1000);
            machine.setStatus(MachineStatus.POWERED_OFF);
            machineRepository.save(machine);

            webSocketBroadcaster.notifyMachineStatusChanged(id, MachineStatus.POWERED_OFF, previousStatus);

            logger.info("Mašina " + id + " se pali...");
            Thread.sleep(1000);
            machine.setStatus(MachineStatus.POWERED_ON);
            machine.setIsBusy(false);
            machineRepository.save(machine);

            webSocketBroadcaster.notifyMachineStatusChanged(id, MachineStatus.POWERED_ON, MachineStatus.POWERED_OFF);

            logger.info("Mašina " + id + " je uspešno restartovana!");
        } catch (InterruptedException e) {
            logger.error("Greška pri restartu mašine: " + e.getMessage());
            machine.setIsBusy(false);
            machineRepository.save(machine);
            Thread.currentThread().interrupt();
        }
    }
    @Autowired
    private ScheduledOperationRepository scheduledOperationRepository;


    public Object scheduleOperation(Long machineId, String operation, String scheduledTime, Long userId) {
        logger.info("Zakazujem operaciju '" + operation + "' na mašini " + machineId + " za " + scheduledTime);

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Mašina sa ID " + machineId + " ne postoji"));

        if (!machine.getActive()) {
            throw new InvalidOperationException("Mašina je obrisana");
        }

        ScheduledOperation scheduled = new ScheduledOperation();
        scheduled.setMachine(machine);
        scheduled.setOperation(ScheduledOperationType.valueOf(operation.toUpperCase()));
        scheduled.setScheduledTime(LocalDateTime.parse(scheduledTime));
        scheduled.setStatus(ScheduledOperationStatus.valueOf("PENDING"));
        scheduled.setCreatedBy(userId);
        scheduled.setCreatedAt(LocalDateTime.now());

        ScheduledOperation saved = scheduledOperationRepository.save(scheduled);
        logger.info("Operacija uspešno zakazana sa ID: " + saved.getId());

        webSocketBroadcaster.notifyScheduledOperationCreated(machineId, saved.getId(), operation);

        return saved;
    }


    public List<?> getScheduledOperationsByMachine(Long machineId) {
        logger.info("Preuzimam zakazane operacije za mašinu: " + machineId);

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Mašina sa ID " + machineId + " ne postoji"));

        return scheduledOperationRepository.findByMachine_IdAndStatus(machineId, ScheduledOperationStatus.valueOf("PENDING"));
    }


    public void cancelScheduledOperation(Long operationId) {
        logger.info("Otkazujem zakazanu operaciju sa ID: " + operationId);

        ScheduledOperation scheduled = scheduledOperationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operacija sa ID " + operationId + " ne postoji"));

        if (!"PENDING".equals(scheduled.getStatus())) {
            throw new InvalidOperationException("Mogu se otkazati samo PENDING operacije. Trenutni status: " + scheduled.getStatus());
        }

        scheduledOperationRepository.delete(scheduled);
        logger.info("Operacija sa ID " + operationId + " je uspešno otkazana");

        webSocketBroadcaster.notifyScheduledOperationCancelled(scheduled.getMachineId(), operationId);
    }

    private MachineDTO convertToDTO(Machine machine) {
        return new MachineDTO(
                machine.getId(),
                machine.getName(),
                machine.getType(),
                machine.getDescription(),
                machine.getStatus(),
                machine.getCreatedBy(),
                machine.getActive(),
                machine.getCreatedAt(),
                machine.getIsBusy()
        );
    }
}

