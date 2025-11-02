-- COMMENTS
CREATE TABLE comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  issue_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  text TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_comment_issue  FOREIGN KEY (issue_id)  REFERENCES issues(id),
  CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(id),
  KEY ix_comment_issue_created (issue_id, created_at)
);

-- ATTACHMENTS (S3 metadata)
CREATE TABLE attachments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  issue_id BIGINT NOT NULL,
  uploader_id BIGINT NOT NULL,
  s3_key VARCHAR(512) NOT NULL,
  filename VARCHAR(255) NOT NULL,
  size BIGINT NOT NULL,
  content_type VARCHAR(120) NOT NULL,
  uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_att_issue FOREIGN KEY (issue_id) REFERENCES issues(id),
  CONSTRAINT fk_att_user  FOREIGN KEY (uploader_id) REFERENCES users(id),
  KEY ix_att_issue (issue_id),
  KEY ix_att_uploader (uploader_id)
);

-- AUDIT TRAIL (append-only)
CREATE TABLE activity_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entity_type VARCHAR(32) NOT NULL,  -- PROJECT|SPRINT|ISSUE|COMMENT|ATTACHMENT|USER|TEAM
  entity_id BIGINT NOT NULL,
  actor_id BIGINT NOT NULL,
  action VARCHAR(64) NOT NULL,       -- e.g., STATUS_CHANGED
  from_value JSON NULL,
  to_value JSON NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_al_entity (entity_type, entity_id, created_at),
  KEY ix_al_actor (actor_id, created_at),
  CONSTRAINT fk_al_actor FOREIGN KEY (actor_id) REFERENCES users(id)
);

-- AUTH TOKENS
CREATE TABLE refresh_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_rt_user (user_id),
  KEY ix_rt_exp (expires_at),
  CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE password_reset_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  used_at DATETIME NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_prt_user (user_id),
  KEY ix_prt_exp (expires_at),
  CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id)
);