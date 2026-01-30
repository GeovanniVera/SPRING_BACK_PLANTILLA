-- ============================================================================
-- RBAC Administrative Module - MySQL Database Schema
-- ============================================================================
-- This script creates the complete database schema for the RBAC module
-- including privileges, roles, users, and audit tables.
-- 
-- Database: MySQL 8.0+
-- Character Set: UTF-8
-- Engine: InnoDB
-- ============================================================================

-- Drop existing tables if they exist (for clean setup)
-- Uncomment the following lines if you want to recreate the schema
-- DROP TABLE IF EXISTS audit_event;
-- DROP TABLE IF EXISTS users_roles;
-- DROP TABLE IF EXISTS roles_privileges;
-- DROP TABLE IF EXISTS users;
-- DROP TABLE IF EXISTS roles;
-- DROP TABLE IF EXISTS privileges;

-- ============================================================================
-- PRIVILEGES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS privileges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_privilege_name (name),
    INDEX idx_privilege_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- ROLES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_role_name (name),
    INDEX idx_role_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_public BINARY(16) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    alias VARCHAR(100) NOT NULL,
    tag VARCHAR(120) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    INDEX idx_user_id_public (id_public),
    INDEX idx_user_username (username),
    INDEX idx_user_tag (tag),
    INDEX idx_user_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- ROLES_PRIVILEGES JOIN TABLE (Many-to-Many)
-- ============================================================================
CREATE TABLE IF NOT EXISTS roles_privileges (
    role_id BIGINT NOT NULL,
    privilege_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, privilege_id),
    CONSTRAINT fk_roles_privileges_role 
        FOREIGN KEY (role_id) REFERENCES roles(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_roles_privileges_privilege 
        FOREIGN KEY (privilege_id) REFERENCES privileges(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_rp_role (role_id),
    INDEX idx_rp_privilege (privilege_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- USERS_ROLES JOIN TABLE (Many-to-Many)
-- ============================================================================
CREATE TABLE IF NOT EXISTS users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users_roles_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_users_roles_role 
        FOREIGN KEY (role_id) REFERENCES roles(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_ur_user (user_id),
    INDEX idx_ur_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- AUDIT_EVENT TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS audit_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_time_utc DATETIME(6) NOT NULL,
    request_id VARCHAR(64),
    actor_user_id_public VARCHAR(36),
    actor_username VARCHAR(80),
    action VARCHAR(80) NOT NULL,
    category VARCHAR(40) NOT NULL,
    entity_type VARCHAR(60),
    entity_id_public VARCHAR(36),
    outcome VARCHAR(20) NOT NULL,
    http_status INT,
    error_code VARCHAR(80),
    message VARCHAR(255),
    http_method VARCHAR(10),
    path VARCHAR(200),
    ip VARCHAR(45),
    user_agent VARCHAR(255),
    details TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_audit_event_time (event_time_utc),
    INDEX idx_audit_request_id (request_id),
    INDEX idx_audit_actor_time (actor_user_id_public, event_time_utc),
    INDEX idx_audit_entity_type_id (entity_type, entity_id_public),
    INDEX idx_audit_action_time (action, event_time_utc),
    INDEX idx_audit_category (category),
    INDEX idx_audit_outcome (outcome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- SEED DATA - BASIC PRIVILEGES
-- ============================================================================
-- Insert core privileges if they don't exist
INSERT IGNORE INTO privileges (name, description, active) VALUES
-- RBAC Privileges
('RBAC_ROLE_CREATE', 'Create new roles', TRUE),
('RBAC_ROLE_READ', 'Read role information', TRUE),
('RBAC_ROLE_UPDATE', 'Update existing roles', TRUE),
('RBAC_ROLE_DELETE', 'Delete or deactivate roles', TRUE),
('RBAC_PRIV_CREATE', 'Create new privileges', TRUE),
('RBAC_PRIV_READ', 'Read privilege information', TRUE),
('RBAC_PRIV_UPDATE', 'Update existing privileges', TRUE),
('RBAC_PRIV_DELETE', 'Delete or deactivate privileges', TRUE),

-- User Management Privileges
('USERS_CREATE', 'Create new users', TRUE),
('USERS_READ_ALL', 'Read all user information', TRUE),
('USERS_READ_SELF', 'Read own user information', TRUE),
('USERS_UPDATE', 'Update user information', TRUE),
('USERS_DELETE', 'Delete or deactivate users', TRUE);

-- ============================================================================
-- SEED DATA - BASIC ROLES
-- ============================================================================
-- Insert ADMIN role if it doesn't exist
INSERT IGNORE INTO roles (name, description, active) VALUES
('ADMIN', 'System administrator with full access', TRUE),
('USER', 'Standard user with basic access', TRUE);

-- ============================================================================
-- SEED DATA - ASSIGN ALL PRIVILEGES TO ADMIN ROLE
-- ============================================================================
-- Assign all privileges to ADMIN role
INSERT IGNORE INTO roles_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN privileges p
WHERE r.name = 'ADMIN';

-- Assign basic privileges to USER role
INSERT IGNORE INTO roles_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r
INNER JOIN privileges p ON p.name = 'USERS_READ_SELF'
WHERE r.name = 'USER';

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================
-- Uncomment to verify the setup

-- SELECT 'Privileges Count' AS Info, COUNT(*) AS Count FROM privileges;
-- SELECT 'Roles Count' AS Info, COUNT(*) AS Count FROM roles;
-- SELECT 'Users Count' AS Info, COUNT(*) AS Count FROM users;
-- SELECT 'Role-Privilege Mappings' AS Info, COUNT(*) AS Count FROM roles_privileges;
-- SELECT 'User-Role Mappings' AS Info, COUNT(*) AS Count FROM users_roles;

-- Show ADMIN privileges
-- SELECT r.name AS role_name, p.name AS privilege_name
-- FROM roles r
-- JOIN roles_privileges rp ON r.id = rp.role_id
-- JOIN privileges p ON rp.privilege_id = p.id
-- WHERE r.name = 'ADMIN'
-- ORDER BY p.name;

-- ============================================================================
-- END OF MIGRATION SCRIPT
-- ============================================================================
