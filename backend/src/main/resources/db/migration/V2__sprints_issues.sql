CREATE TABLE sprints (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  start_date DATE NULL,
  end_date DATE NULL,
  state VARCHAR(16) NOT NULL,     -- PLANNED|ACTIVE|CLOSED
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sprint_project FOREIGN KEY (project_id) REFERENCES projects(id),
  KEY ix_sprint_project_state (project_id, state)
);

CREATE TABLE issues (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  sprint_id BIGINT NULL,
  type VARCHAR(16) NOT NULL,        -- STORY|TASK|BUG|EPIC
  status VARCHAR(20) NOT NULL,      -- BACKLOG|TODO|IN_PROGRESS|QA_READY|DONE
  priority VARCHAR(16) NOT NULL,    -- LOW|MEDIUM|HIGH|CRITICAL
  assignee_id BIGINT NULL,
  reporter_id BIGINT NOT NULL,
  summary VARCHAR(255) NOT NULL,
  description TEXT NULL,
  story_points INT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_issue_project  FOREIGN KEY (project_id) REFERENCES projects(id),
  CONSTRAINT fk_issue_sprint   FOREIGN KEY (sprint_id)  REFERENCES sprints(id),
  CONSTRAINT fk_issue_assignee FOREIGN KEY (assignee_id) REFERENCES users(id),
  CONSTRAINT fk_issue_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
  KEY ix_issue_proj_status (project_id, status),
  KEY ix_issue_proj_assignee_status (project_id, assignee_id, status),
  KEY ix_issue_sprint_status (sprint_id, status)
);