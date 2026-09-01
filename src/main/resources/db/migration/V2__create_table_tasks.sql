CREATE TABLE IF NOT EXISTS tasks(
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    code VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reporter VARCHAR(100) NOT NULL,
    assignee VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id VARCHAR(36),

    CONSTRAINT fk_tasks_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);