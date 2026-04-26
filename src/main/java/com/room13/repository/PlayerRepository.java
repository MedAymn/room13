package com.room13.repository;

import com.room13.model.Player;

public interface PlayerRepository {
    Player save(Player player);
    Player findByUsername(String username);
    Player findById(Long id);
    void updateBestScore(Long playerId, int score);
}
