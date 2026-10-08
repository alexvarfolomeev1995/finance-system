INSERT INTO users (email, password_hash, full_name, enabled)
VALUES (
           'admin@finance.local',
           '$2a$10$GprhJMuZokgHSsiTLpecWeL8o9u5jhTdNMWaVRS1iIcpri74..eZW',
           'System Administrator',
           TRUE
       );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@finance.local'
  AND r.name = 'ADMIN';