-- Deletes specific users and their role links (Many-to-Many join table).
-- Safe to run multiple times.

BEGIN;

DELETE FROM user_roles
WHERE user_id IN (
  SELECT id FROM users WHERE username IN ('jamal', 'test4711')
);

DELETE FROM users
WHERE username IN ('jamal', 'test4711');

COMMIT;

