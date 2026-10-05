-- 1. Remove os registros existentes das tarefas (e históricos para evitar FK errors)
DELETE FROM task_histories;
DELETE FROM tasks;

-- 2. Remove a coluna 'assignee' se ela existir
ALTER TABLE tasks DROP COLUMN IF EXISTS assignee;

-- 3. Caso a coluna 'reporter' antiga exista, renomeie ou drope
ALTER TABLE tasks DROP COLUMN IF EXISTS reporter;

-- 4. Adiciona/Garante a coluna 'reporter_id' como NOT NULL
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS reporter_id VARCHAR(36) NOT NULL;