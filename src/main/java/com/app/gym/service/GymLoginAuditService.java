package com.app.gym.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.app.gym.audit.GymAuditEventEntity;
import com.app.gym.audit.repository.GymAuditEventRepository;

@Service
public class GymLoginAuditService {

    private final GymAuditEventRepository auditRepository;

    public GymLoginAuditService(
            GymAuditEventRepository auditRepository) {

        this.auditRepository = auditRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginSuccess(Long userId) {

        GymAuditEventEntity event = GymAuditEventEntity.success(
                userId,
                "LOGIN_SUCCESS",
                "USER",
                userId,
                null,
                null,
                "Credentials verified and authenticated session established"
        );

        auditRepository.save(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginFailure() {

        GymAuditEventEntity event =
                GymAuditEventEntity.loginFailure();

        auditRepository.save(event);
 
  }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLogoutSuccess(Long userId) {

        GymAuditEventEntity event = GymAuditEventEntity.success(
                userId,
                "LOGOUT_SUCCESS",
                "USER",
                userId,
                null,
                null,
                "User logged out of the current session"
        );

        auditRepository.save(event);
    }
    
}