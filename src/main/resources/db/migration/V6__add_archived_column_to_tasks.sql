-- 1. Adiciona a coluna com valor padrão FALSE
ALTER TABLE IF EXISTS tasks
    ADD COLUMN IF NOT EXISTS archived BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. (Opcional/Garantia) Atualiza registros pré-existentes que possam ter ficado NULL
UPDATE tasks
SET archived = FALSE
WHERE archived IS NULL;