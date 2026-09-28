package raf.rs.raf_cloud_backend.controller;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import raf.rs.raf_cloud_backend.annotation.Permissions;
import raf.rs.raf_cloud_backend.annotation.Secured;
import raf.rs.raf_cloud_backend.dto.MachineCreateRequestDTO;
import raf.rs.raf_cloud_backend.dto.MachineDTO;
import raf.rs.raf_cloud_backend.service.MachineService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/machines")
@CrossOrigin(origins = "*")
public class MachineController {

    private static final Logger logger = LoggerFactory.getLogger(MachineController.class);

    @Autowired
    private MachineService machineService;

    @GetMapping
    @Secured(Permissions.SEARCH_MACHINES)
    public ResponseEntity<List<MachineDTO>> getAllMachines(HttpServletRequest request) {
        logger.info("GET /machines");

        Object userIdObj = request.getAttribute("userId");
        Long userId = null;

        if (userIdObj instanceof Integer) {
            userId = ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            userId = (Long) userIdObj;
        }

        boolean isAdmin = false;
        Object permissionsObj = request.getAttribute("permissions");
        if (permissionsObj != null) {
            Set<String> userPermissions = new HashSet<>();
            if (permissionsObj instanceof Collection) {
                userPermissions.addAll((Collection<? extends String>) permissionsObj);
            }
            isAdmin = userPermissions.contains(Permissions.DESTROY_MACHINE);
        }

        List<MachineDTO> machines = machineService.getAllMachines(userId, isAdmin);
        return ResponseEntity.ok(machines);
    }


    @GetMapping("/{id}")
    @Secured(Permissions.SEARCH_MACHINES)
    public ResponseEntity<MachineDTO> getMachineById(@PathVariable Long id) {
        logger.info(" GET /machines/" + id + " ");

        MachineDTO machine = machineService.getMachineById(id);
        return ResponseEntity.ok(machine);
    }


    @PostMapping
    @Secured(Permissions.CREATE_MACHINE)
    public ResponseEntity<?> createMachine(
            @Valid @RequestBody MachineCreateRequestDTO request,
            HttpServletRequest httpRequest) {
        logger.info(" POST /machines");
        try {
            Object userIdObj = httpRequest.getAttribute("userId");
            Long userId = null;

            if (userIdObj instanceof Integer) {
                userId = ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                userId = (Long) userIdObj;
            }

            if (userId == null) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse(400, "Korisnik nije pronađen")
                );
            }

            MachineDTO created = machineService.createMachine(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Greška: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }


    @DeleteMapping("/{id}")
    @Secured(Permissions.DESTROY_MACHINE)
    public ResponseEntity<?> deleteMachine(@PathVariable Long id) {
        logger.info(" DELETE /machines/" + id + " ");
        try {
            machineService.deleteMachine(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Greška: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PostMapping("/{id}/start")
    @Secured(Permissions.POWER_ON_MACHINE)
    public ResponseEntity<?> startMachine(@PathVariable Long id) {
        logger.info("POST /machines/" + id + "/start");
        try {
            machineService.turnOn(id);
            return ResponseEntity.ok(new OperationResponse("Mašina se pali... (10 sekundi)"));
        } catch (Exception e) {
            logger.error("Greška: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PostMapping("/{id}/stop")
    @Secured(Permissions.POWER_OFF_MACHINE)
    public ResponseEntity<?> stopMachine(@PathVariable Long id) {
        logger.info(" POST /machines/" + id + "/stop ");
        try {
            machineService.turnOff(id);
            return ResponseEntity.ok(new OperationResponse("Mašina se gasi... (10 sekundi)"));
        } catch (Exception e) {
            logger.error("Greška: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PostMapping("/{id}/restart")
    @Secured(Permissions.RESTART_MACHINE)
    public ResponseEntity<?> restartMachine(@PathVariable Long id) {
        logger.info(" POST /machines/" + id + "/restart ");
        try {
            machineService.restart(id);
            return ResponseEntity.ok(new OperationResponse("Mašina se restartuje... (10 sekundi)"));
        } catch (Exception e) {
            logger.error("Greška: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    public static class OperationResponse {
        public String message;
        public OperationResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    public static class ErrorResponse {
        public int status;
        public String message;
        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }
        public int getStatus() { return status; }
        public String getMessage() { return message; }
    }
}
