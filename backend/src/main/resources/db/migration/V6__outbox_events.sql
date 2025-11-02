CREATE TABLE outbox_events (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_id CHAR(36) NOT NULL,     -- UUID for idempotency
  type VARCHAR(64) NOT NULL,      -- ISSUE_CREATED, STATUS_CHANGED, ...
  channel VARCHAR(128) NOT NULL,  -- project.12 / issue.101
  payload JSON NOT NULL,          -- compact JSON (ids + changed fields)
  published TINYINT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_event_id (event_id),
  KEY ix_published_created (published, created_at)
);