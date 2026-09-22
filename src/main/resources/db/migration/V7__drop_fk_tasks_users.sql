-- Remove a restrição de chave estrangeira da tabela tasks
ALTER TABLE tasks DROP CONSTRAINT IF EXISTS fk_tasks_users;