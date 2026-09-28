package raf.rs.raf_cloud_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import raf.rs.raf_cloud_backend.entity.MachineStatus;
import raf.rs.raf_cloud_backend.entity.WebSocketEvent;
import raf.rs.raf_cloud_backend.handler.GeneralWebSocketHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketBroadcaster {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketBroadcaster.class);

    @Autowired
    private GeneralWebSocketHandler generalWebSocketHandler;

    private final ObjectMapper objectMapper;


    public WebSocketBroadcaster(GeneralWebSocketHandler generalWebSocketHandler) {
        this.generalWebSocketHandler = generalWebSocketHandler;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void broadcast(String eventType, Long entityId, String entityType, Object data, String severity) {
        logger.info("Broadcasting event: [" + eventType + "] | Entity: " + entityType + "#" + entityId +
                " | Severity: " + severity);

        try {
            WebSocketEvent event = new WebSocketEvent();
            event.setType(eventType);
            event.setEntityId(entityId);
            event.setEntityType(entityType);
            event.setTimestamp(LocalDateTime.now());
            event.setData(data);
            event.setSeverity(severity);

            String jsonMessage = objectMapper.writeValueAsString(event);
            TextMessage textMessage = new TextMessage(jsonMessage);

            generalWebSocketHandler.broadcast(textMessage);

            logger.info("Event poslat svim klijentima (" +
                    generalWebSocketHandler.getActiveSessionCount() + " sesije)");
        } catch (Exception e) {
            logger.error("Greška pri broadcastovanju: " + e.getMessage(), e);
        }
    }


    public void notifyMachineStatusChanged(Long machineId, MachineStatus newStatus, MachineStatus previousStatus) {
        Map<String, Object> data = new HashMap<>();
        data.put("machineId", machineId);
        data.put("status", newStatus.toString());
        data.put("previousStatus", previousStatus != null ? previousStatus.toString() : null);

        broadcast("MACHINE_STATUS_CHANGED", machineId, "MACHINE", data, "INFO");
    }

    public void notifyMachineError(Long machineId, String errorMessage) {
        Map<String, Object> data = new HashMap<>();
        data.put("machineId", machineId);
        data.put("errorMessage", errorMessage);

        broadcast("MACHINE_ERROR", machineId, "MACHINE", data, "ERROR");
    }

    public void notifyMachineCreated(Long machineId, String machineName) {
        Map<String, Object> data = new HashMap<>();
        data.put("machineId", machineId);
        data.put("machineName", machineName);
        data.put("action", "CREATED");

        broadcast("MACHINE_CHANGED", machineId, "MACHINE", data, "INFO");
    }

    public void notifyMachineDeleted(Long machineId) {
        Map<String, Object> data = new HashMap<>();
        data.put("machineId", machineId);
        data.put("action", "DELETED");

        broadcast("MACHINE_CHANGED", machineId, "MACHINE", data, "WARNING");
    }

    public void notifyUserLoggedIn(Long userId, String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", username);
        data.put("action", "LOGIN");
        data.put("loginTime", LocalDateTime.now());

        broadcast("USER_ACTION", userId, "USER", data, "INFO");
    }

    public void notifyUserLoggedOut(Long userId, String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", username);
        data.put("action", "LOGOUT");
        data.put("logoutTime", LocalDateTime.now());

        broadcast("USER_ACTION", userId, "USER", data, "INFO");
    }

    public void notifyUserProfileUpdated(Long userId, String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", username);
        data.put("action", "PROFILE_UPDATED");

        broadcast("USER_ACTION", userId, "USER", data, "INFO");
    }

    public void notifyUserCreated(Long userId, String username, String email) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", username);
        data.put("email", email);
        data.put("action", "CREATED");

        broadcast("USER_ACTION", userId, "USER", data, "INFO");
    }

    public void notifyUserDeleted(Long userId, String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", username);
        data.put("action", "DELETED");

        broadcast("USER_ACTION", userId, "USER", data, "WARNING");
    }

    public void notifyUserRoleChanged(Long userId, String username, String newRole) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", username);
        data.put("newRole", newRole);
        data.put("action", "ROLE_CHANGED");

        broadcast("USER_ACTION", userId, "USER", data, "WARNING");
    }

    public void notifyError(String errorCode, String errorMessage, Long affectedUserId) {
        Map<String, Object> data = new HashMap<>();
        data.put("errorCode", errorCode);
        data.put("errorMessage", errorMessage);
        data.put("affectedUserId", affectedUserId);

        broadcast("ERROR_OCCURRED", affectedUserId, "SYSTEM", data, "ERROR");
    }

    public void notifyWarning(String warningMessage, Long affectedUserId) {
        Map<String, Object> data = new HashMap<>();
        data.put("warningMessage", warningMessage);
        data.put("affectedUserId", affectedUserId);

        broadcast("WARNING_OCCURRED", affectedUserId, "SYSTEM", data, "WARNING");
    }


    public void notifyAuthenticationFailed(String username, String reason) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("reason", reason);

        broadcast("AUTH_FAILED", null, "SYSTEM", data, "ERROR");
    }

    public void notifyUnauthorizedAccess(Long userId, String resource) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("resource", resource);

        broadcast("UNAUTHORIZED_ACCESS", userId, "SYSTEM", data, "ERROR");
    }


    public void notifyScheduledOperationCreated(Long machineId, Long operationId, String operation) {
        Map<String, Object> data = new HashMap<>();
        data.put("machineId", machineId);
        data.put("operationId", operationId);
        data.put("operation", operation);
        data.put("action", "CREATED");

        broadcast("SCHEDULED_OPERATION_CREATED", machineId, "SCHEDULED_OPERATION", data, "INFO");
    }

    public void notifyScheduledOperationCancelled(Long machineId, Long operationId) {
        Map<String, Object> data = new HashMap<>();
        data.put("machineId", machineId);
        data.put("operationId", operationId);
        data.put("action", "CANCELLED");

        broadcast("SCHEDULED_OPERATION_CANCELLED", machineId, "SCHEDULED_OPERATION", data, "INFO");
    }

}
