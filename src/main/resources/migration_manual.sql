-- MIGRATION SCRIPT (MANUAL)
-- Use this if you have existing data and need to migrate from UUID PK to BIGINT PK + UUID Public ID.
-- WARNING: Backup your database first.

-- 1. Rename existing UUID PK to id_public (preserving the UUIDs as public IDs)
-- Assuming existing 'id' was BINARY(16) or CHAR(36) holding UUIDs.
ALTER TABLE users CHANGE COLUMN id id_public BINARY(16) NOT NULL;

-- 2. Drop the primary key constraint (since we renamed the column, the PK constraint might still be on it or need adjustment)
ALTER TABLE users DROP PRIMARY KEY;

-- 3. Make id_public UNIQUE
ALTER TABLE users ADD CONSTRAINT uk_users_id_public UNIQUE (id_public);

-- 4. Add the new internal ID column (Long Auto Increment)
ALTER TABLE users ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST;

-- 5. Handle Foreign Keys (users_roles context)
-- We need to update foreign keys to point to the new BIGINT id instead of the UUID (now id_public).

-- Add temporary column to join table
ALTER TABLE users_roles ADD COLUMN user_id_new BIGINT;

-- Disable safe updates if needed
SET SQL_SAFE_UPDATES = 0;

-- Update the link: join users_roles with users on the OLD key (which is now id_public)
-- Note: users_roles.user_id still holds the UUID value.
UPDATE users_roles ur 
JOIN users u ON ur.user_id = u.id_public 
SET ur.user_id_new = u.id;

-- Drop old FK and column
-- You need to know the constraint name. Check with: SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE ...
-- Assuming generic name or use a tool to find it. Here we assume we drop the column and the FK goes with it or explicitly drop FK first.
-- ALTER TABLE users_roles DROP FOREIGN KEY fk_users_roles_user_id; 
ALTER TABLE users_roles DROP COLUMN user_id;

-- Rename new column to user_id
ALTER TABLE users_roles CHANGE COLUMN user_id_new user_id BIGINT NOT NULL;

-- Add new FK constraint
ALTER TABLE users_roles ADD CONSTRAINT fk_users_roles_user_id FOREIGN KEY (user_id) REFERENCES users(id);

-- 6. Add other new columns if missing
-- ALTER TABLE users ADD COLUMN tag VARCHAR(255);
-- ALTER TABLE users ADD COLUMN alias VARCHAR(255);
-- etc. (Assuming they might not exist or need backfilling)

-- 7. Backfill Tag if null (using logic app-side or simple SQL if possible)
-- UPDATE users SET tag = CONCAT(alias, '#', HEX(LEFT(id_public, 3))) WHERE tag IS NULL;

-- 8. Verify Constraints
-- ALTER TABLE users ADD CONSTRAINT uk_users_tag UNIQUE (tag);
-- ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);

SET SQL_SAFE_UPDATES = 1;
