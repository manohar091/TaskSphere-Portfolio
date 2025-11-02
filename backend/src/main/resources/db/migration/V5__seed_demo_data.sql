-- Demo users (BCrypt hash can be updated later)
INSERT INTO users(name,email,password_hash)
VALUES
 ('Admin One','admin@ts.com','$2a$10$dummyhash'),
 ('Manager One','manager@ts.com','$2a$10$dummyhash'),
 ('Dev One','dev@ts.com','$2a$10$dummyhash');

-- Link Manager & Dev to project via team_members
INSERT INTO projects(`key`, name, owner_id)
VALUES ('TS','TaskSphere',1);

INSERT INTO team_members(project_id,user_id,role_id)
SELECT p.id,u.id,r.id FROM projects p, users u, roles r
WHERE p.`key`='TS' AND u.email='manager@ts.com' AND r.name='MANAGER';

INSERT INTO team_members(project_id,user_id,role_id)
SELECT p.id,u.id,r.id FROM projects p, users u, roles r
WHERE p.`key`='TS' AND u.email='dev@ts.com' AND r.name='DEV';

-- Create one active sprint
INSERT INTO sprints(project_id,name,state,start_date,end_date)
SELECT id,'Sprint-1','ACTIVE','2025-11-01','2025-11-15' FROM projects WHERE `key`='TS';

-- Create sample issues
INSERT INTO issues(project_id,sprint_id,type,status,priority,reporter_id,summary)
SELECT p.id,s.id,'TASK','IN_PROGRESS','HIGH',2,'Setup Spring Boot + MySQL'
FROM projects p JOIN sprints s ON s.project_id=p.id;

INSERT INTO issues(project_id,sprint_id,type,status,priority,reporter_id,summary)
SELECT p.id,s.id,'BUG','TODO','MEDIUM',2,'Fix login API'
FROM projects p JOIN sprints s ON s.project_id=p.id;