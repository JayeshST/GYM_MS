package com.app.gym.audit.repository;

import org.springframework.data.repository.Repository;

import com.app.gym.audit.GymAuditEventEntity;


public interface GymAuditEventRepository
        extends Repository<GymAuditEventEntity, Long> {

    GymAuditEventEntity save(GymAuditEventEntity event);
}