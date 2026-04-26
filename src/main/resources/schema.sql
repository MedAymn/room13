-- Room 13 Database Schema
-- Run this against a MySQL 8+ server after creating the database:
--   CREATE DATABASE room13 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE room13;

CREATE TABLE IF NOT EXISTS player (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    best_score  INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS saved_game (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    player_id         BIGINT      NOT NULL,
    level             VARCHAR(10) NOT NULL,
    revealed_digits   TEXT,
    riddle_index      INT DEFAULT 0,
    remaining_seconds INT,
    current_score     INT DEFAULT 0,
    riddle_ids        VARCHAR(200),
    correct_pin       VARCHAR(20),
    saved_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_saved_player FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
