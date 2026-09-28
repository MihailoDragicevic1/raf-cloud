package raf.rs.raf_cloud_backend.entity;


import java.time.LocalDateTime;
public class WebSocketEvent {

    private String type;
    private Long entityId;
    private String entityType;
    private LocalDateTime timestamp;
    private Object data;
    private String severity;

    public WebSocketEvent() {
    }

    public WebSocketEvent(String type, Long entityId, String entityType,
                          LocalDateTime timestamp, Object data, String severity) {
        this.type = type;
        this.entityId = entityId;
        this.entityType = entityType;
        this.timestamp = timestamp;
        this.data = data;
        this.severity = severity;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public Long getEntityId() {
        return entityId;
    }
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }

    public String getSeverity() {
        return severity;
    }
    public void setSeverity(String severity) {
        this.severity = severity;
    }

}
