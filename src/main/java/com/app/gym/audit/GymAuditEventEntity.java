package com.app.gym.audit;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "GYM_AUDIT_EVENT")
public class GymAuditEventEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "gymAuditSequence"
    )
    @SequenceGenerator(
        name = "gymAuditSequence",
        sequenceName = "GYM_ENTITY_SEQ",
        allocationSize = 1
    )
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @Column(name = "ACTOR_USER_ID", updatable = false)
    private Long actorUserId;

    @Column(
        name = "ACTION_CODE",
        nullable = false,
        length = 80,
        updatable = false
    )
    private String actionCode;

    @Column(
        name = "ENTITY_TYPE",
        nullable = false,
        length = 40,
        updatable = false
    )
    private String entityType;

    @Column(name = "ENTITY_ID", updatable = false)
    private Long entityId;

    @Column(
        name = "OUTCOME",
        nullable = false,
        length = 20,
        updatable = false
    )
    private String outcome;

    @Column(
        name = "EVENT_TIME",
        nullable = false,
        updatable = false
    )
    private LocalDateTime eventTime;

    @Lob
    @Column(name = "BEFORE_DATA", updatable = false)
    private String beforeData;

    @Lob
    @Column(name = "AFTER_DATA", updatable = false)
    private String afterData;

    @Column(name = "REASON", length = 500, updatable = false)
    private String reason;

    protected GymAuditEventEntity() {
    }

    public static GymAuditEventEntity success(
            Long actorUserId,
            String actionCode,
            String entityType,
            Long entityId,
            String beforeData,
            String afterData,
            String reason) {

        GymAuditEventEntity event = new GymAuditEventEntity();

        event.actorUserId = actorUserId;
        event.actionCode = actionCode;
        event.entityType = entityType;
        event.entityId = entityId;
        event.outcome = "SUCCESS";
        event.beforeData = beforeData;
        event.afterData = afterData;
        event.reason = reason;

        return event;
    }

    @PrePersist
    protected void beforeInsert() {
        this.eventTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getActionCode() {
        return actionCode;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getOutcome() {
        return outcome;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public String getBeforeData() {
        return beforeData;
    }

    public String getAfterData() {
        return afterData;
    }

    public String getReason() {
        return reason;
    }
    public static GymAuditEventEntity loginFailure() {

        GymAuditEventEntity event = success(
                null,
                "LOGIN_FAILED",
                "USER",
                null,
                null,
                null,
                "Authentication rejected"
        );

        event.outcome = "FAILURE";

        return event;
    }
}