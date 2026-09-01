CREATE TABLE IF NOT EXISTS task_histories(
    id VARCHAR(36) NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    current_status VARCHAR(20) NOT NULL,
    moved_at TIMESTAMP NOT NULL ,
    task_id VARCHAR(36) NOT NULL,

    CONSTRAINT pk_task_histories PRIMARY KEY (id),
    CONSTRAINT fk_task_histories FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE INDEX idx_task_histories_task_id ON task_histories(task_id);