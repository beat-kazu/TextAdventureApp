CREATE TABLE player_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) DEFAULT 'USER',
    username VARCHAR(255) UNIQUE NOT NULL,
    nickname VARCHAR(255),
    favorite VARCHAR(255),
    player_flags VARCHAR(2000)
);

CREATE TABLE save_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    current_scene_id  VARCHAR(255),
    previous_scene_id VARCHAR(255),
    items VARCHAR(2000),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    flags VARCHAR(2000)
);
