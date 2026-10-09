-- 1. Tabela de salas de chat
CREATE TABLE chat_rooms (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100),
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- 2. Tabela de participantes
CREATE TABLE chat_participants (
    id VARCHAR(36) PRIMARY KEY,
    user_id UUID NOT NULL,
    role VARCHAR(20),
    joined_at TIMESTAMP,
    room_id VARCHAR(36) NOT NULL,

    CONSTRAINT fk_chat_participants_room
        FOREIGN KEY (room_id)
        REFERENCES chat_rooms (id)
        ON DELETE CASCADE
);

-- 3. Tabela de mensagens
CREATE TABLE messages (
    id VARCHAR(36) PRIMARY KEY,
    sender_id UUID NOT NULL,
    content TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    room_id VARCHAR(36) NOT NULL,

    CONSTRAINT fk_messages_room
        FOREIGN KEY (room_id)
        REFERENCES chat_rooms (id)
        ON DELETE CASCADE
);

-- 4. Índices para performance
CREATE INDEX idx_chat_participants_room_id ON chat_participants(room_id);
CREATE INDEX idx_chat_participants_user_id ON chat_participants(user_id);

-- Índice composto otimizado para carregar o histórico de conversas da sala em ordem cronológica
CREATE INDEX idx_messages_room_timestamp ON messages(room_id, timestamp DESC);