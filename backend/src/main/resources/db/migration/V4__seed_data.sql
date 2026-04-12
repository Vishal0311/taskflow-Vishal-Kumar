-- password is: password123  (bcrypt hashed)
INSERT INTO users (id, name, email, password) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Test User',
    'test@example.com',
    '$2a$12$egtdtQyeyY.98BI./BHZ7O6nsrtJJg/WCoffK.U4W2tvMH2OoGzvW'
);

INSERT INTO projects (id, name, description, owner_id) VALUES (
    '00000000-0000-0000-0000-000000000002',
    'Demo Project',
    'A sample project for testing',
    '00000000-0000-0000-0000-000000000001'
);

INSERT INTO tasks (id, title, description, status, priority, project_id, assignee_id) VALUES
(
    '00000000-0000-0000-0000-000000000003',
    'Set up database',
    'Configure PostgreSQL and run migrations',
    'done',
    'high',
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001'
),
(
    '00000000-0000-0000-0000-000000000004',
    'Build REST API',
    'Implement all endpoints',
    'in_progress',
    'high',
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001'
),
(
    '00000000-0000-0000-0000-000000000005',
    'Write documentation',
    'Add README and API docs',
    'todo',
    'medium',
    '00000000-0000-0000-0000-000000000002',
    NULL
);

--UPDATE users
--SET password = '$2a$12$egtdtQyeyY.98BI./BHZ7O6nsrtJJg/WCoffK.U4W2tvMH2OoGzvW'
--WHERE email = 'test@example.com';