package com.jasamedika.hrmis.service;

import com.jasamedika.hrmis.entity.AuditLog;
import com.jasamedika.hrmis.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(String userEmail, String action, String entityType, 
                         String entityId, String oldValue, String newValue,
                         HttpServletRequest request) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserEmail(userEmail);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
        
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> getAuditLogs(String userEmail, String action, String entityType,
                                      LocalDateTime startDate, LocalDateTime endDate,
                                      int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        if (userEmail != null && !userEmail.isEmpty()) {
            return auditLogRepository.findByUserEmailOrderByCreatedAtDesc(userEmail, pageable);
        }
        
        if (action != null && !action.isEmpty()) {
            return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
        }
        
        if (entityType != null && !entityType.isEmpty()) {
            return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable);
        }
        
        if (startDate != null && endDate != null) {
            return auditLogRepository.findByDateRange(startDate, endDate, pageable);
        }
        
        return auditLogRepository.findAll(pageable);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

