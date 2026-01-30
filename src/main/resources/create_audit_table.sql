-- Tabla Audit Event (Sin JSON, compatible con MySQL 5.7/8.0 estándar)
CREATE TABLE IF NOT EXISTS audit_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_time_utc DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    
    request_id VARCHAR(64) NULL,
    
    actor_user_id_public CHAR(36) NULL COMMENT 'UUID público del actor/usuario',
    actor_username VARCHAR(80) NULL,
    
    action VARCHAR(80) NOT NULL COMMENT 'Código de acción (e.g. USER_REGISTER)',
    category VARCHAR(40) NOT NULL COMMENT 'Categoría (e.g. AUTH, SYSTEM)',
    
    entity_type VARCHAR(60) NULL,
    entity_id_public CHAR(36) NULL COMMENT 'UUID de la entidad afectada',
    
    outcome VARCHAR(20) NOT NULL COMMENT 'SUCCESS o FAIL',
    http_status SMALLINT NULL,
    error_code VARCHAR(80) NULL,
    message VARCHAR(255) NULL,
    
    http_method VARCHAR(10) NULL,
    path VARCHAR(200) NULL,
    ip VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    
    details TEXT NULL COMMENT 'Detalles key=value parseables'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices Recomendados para Búsqueda
CREATE INDEX idx_audit_event_time ON audit_event(event_time_utc);
CREATE INDEX idx_audit_request_id ON audit_event(request_id);
CREATE INDEX idx_audit_actor_time ON audit_event(actor_user_id_public, event_time_utc);
CREATE INDEX idx_audit_entity_type_id ON audit_event(entity_type, entity_id_public);
CREATE INDEX idx_audit_action_time ON audit_event(action, event_time_utc);

-- Ejemplo de insert manual de prueba
-- INSERT INTO audit_event (event_time_utc, action, category, outcome, message) VALUES (UTC_TIMESTAMP(), 'TEST_EVENT', 'SYSTEM', 'SUCCESS', 'Test audit log');
