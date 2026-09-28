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
import raf.rs.raf_cloud_backend.dto.ScheduledOperationCreateRequestDTO;
import raf.rs.raf_cloud_backend.dto.ScheduledOperationDTO;
import raf.rs.raf_cloud_backend.service.ScheduledOperationService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/machines")
@CrossOrigin(origins = "*")
public class ScheduledOperationController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledOperationController.class);

    @Autowired
    private ScheduledOperationService scheduledOperationService;

    @PostMapping("/{machineId}/scheduled-operations")
    public ResponseEntity<?> createScheduledOperation(
            @PathVariable Long machineId,
            @Valid @RequestBody ScheduledOperationCreateRequestDTO request,
            HttpServletRequest httpRequest) {

        logger.info("=== POST /machines/" + machineId + "/scheduled-operations ===");
        logger.info("Operacija: " + request.getOperation());
        logger.info("Zakazano vreme: " + request.getScheduledTime());

        try {
            if (!machineId.equals(request.getMachineId())) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse(400, "Machine ID u URL-u ne odgovara Machine ID-u u body-u")
                );
            }

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

            ScheduledOperationDTO created = scheduledOperationService.createScheduledOperation(request, userId);

            logger.info("Zakazana operacija uspešno kreirana sa ID: " + created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (Exception e) {
            logger.error("Greška pri kreiranju zakazane operacije: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(400, e.getMessage())
            );
        }
    }

    @GetMapping("/{machineId}/scheduled-operations")
    @Secured(Permissions.SEARCH_MACHINES)
    public ResponseEntity<List<ScheduledOperationDTO>> getScheduledOperationsByMachine(
            @PathVariable Long machineId) {

        logger.info("=== GET /machines/" + machineId + "/scheduled-operations ===");

        try {
            List<ScheduledOperationDTO> operations =
                    scheduledOperationService.getScheduledOperationsByMachineId(machineId);

            logger.info("Pronađeno " + operations.size() + " zakazanih operacija");
            return ResponseEntity.ok(operations);

        } catch (Exception e) {
            logger.error("Greška pri preuzimanju zakazanih operacija: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{machineId}/scheduled-operations/{opId}")
    @Secured(Permissions.SEARCH_MACHINES)
    public ResponseEntity<ScheduledOperationDTO> getScheduledOperationById(
            @PathVariable Long machineId,
            @PathVariable Long opId) {

        logger.info("=== GET /machines/" + machineId + "/scheduled-operations/" + opId + " ===");

        try {
            return scheduledOperationService.getScheduledOperationById(opId)
                    .map(operation -> {
                        logger.info("Zakazana operacija pronađena");
                        return ResponseEntity.ok(operation);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            logger.error("Greška pri preuzimanju zakazane operacije: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{machineId}/scheduled-operations/{opId}")
    @Secured(Permissions.DESTROY_MACHINE)
    public ResponseEntity<?> cancelScheduledOperation(
            @PathVariable Long machineId,
            @PathVariable Long opId) {

        logger.info("=== DELETE /machines/" + machineId + "/scheduled-operations/" + opId + " ===");

        try {
            scheduledOperationService.cancelScheduledOperation(opId);

            logger.info("Zakazana operacija otkazana");
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Greška pri otkazivanju zakazane operacije: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(400, e.getMessage())
            );
        }
    }

    public static class ErrorResponse {
        private int status;
        private String message;

        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

