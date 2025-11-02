-- ROLES
CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(32) NOT NULL UNIQUE,
  description VARCHAR(255)
);

-- USERS
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
);

-- OPTIONAL global mapping (we'll prefer project-scoped roles)
CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- PROJECTS
CREATE TABLE projects (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  `key` VARCHAR(16) NOT NULL UNIQUE,
  name VARCHAR(200) NOT NULL,
  owner_id BIGINT NOT NULL,
  description TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_proj_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- PROJECT-SCOPED ROLES (recommended)
CREATE TABLE team_members (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uq_team_member (project_id, user_id),
  KEY ix_team_role (project_id, role_id),
  CONSTRAINT fk_tm_project FOREIGN KEY (project_id) REFERENCES projects(id),
  CONSTRAINT fk_tm_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_tm_role FOREIGN KEY (role_id) REFERENCES roles(id)
);