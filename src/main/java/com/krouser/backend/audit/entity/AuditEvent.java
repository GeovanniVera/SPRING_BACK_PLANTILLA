package com.krouser.backend.audit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_event", indexes = {
        @Index(name = "idx_audit_event_time", columnList = "event_time_utc"),
        @Index(name = "idx_audit_request_id", columnList = "request_id"),
        @Index(name = "idx_audit_actor_time", columnList = "actor_user_id_public, event_time_utc"),
        @Index(name = "idx_audit_entity_type_id", columnList = "entity_type, entity_id_public"),
        @Index(name = "idx_audit_action_time", columnList = "action, event_time_utc")
})
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_time_utc", nullable = false)
    private LocalDateTime eventTimeUtc;

    @Column(name = "request_id", length = 64)
    private String requestId;

    @Column(name = "actor_user_id_public", length = 36)
    private String actorUserIdPublic;

    @Column(name = "actor_username", length = 80)
    private String actorUsername;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(name = "entity_type", length = 60)
    private String entityType;

    @Column(name = "entity_id_public", length = 36)
    private String entityIdPublic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditOutcome outcome;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "error_code", length = 80)
    private String errorCode;

    @Column(length = 255)
    private String message;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(length = 200)
    private String path;

    @Column(length = 45)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum AuditOutcome {
        SUCCESS, FAIL
    }

    public AuditEvent() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getEventTimeUtc() {
        return eventTimeUtc;
    }

    public void setEventTimeUtc(LocalDateTime eventTimeUtc) {
        this.eventTimeUtc = eventTimeUtc;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getActorUserIdPublic() {
        return actorUserIdPublic;
    }

    public void setActorUserIdPublic(String actorUserIdPublic) {
        this.actorUserIdPublic = actorUserIdPublic;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityIdPublic() {
        return entityIdPublic;
    }

    public void setEntityIdPublic(String entityIdPublic) {
        this.entityIdPublic = entityIdPublic;
    }

    public AuditOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(AuditOutcome outcome) {
        this.outcome = outcome;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
