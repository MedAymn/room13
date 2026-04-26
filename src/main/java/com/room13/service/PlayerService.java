package com.room13.service;

import com.room13.model.Player;
import com.room13.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * Register a new player. Throws IllegalArgumentException if the username is already taken
     * or if any field is blank.
     */
    public Player register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        String trimmedUsername = username.trim();
        if (trimmedUsername.length() < 3 || trimmedUsername.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters.");
        }
        Player existing = playerRepository.findByUsername(trimmedUsername);
        if (existing != null) {
            throw new IllegalArgumentException("Username '" + trimmedUsername + "' is already taken.");
        }
        Player player = new Player(trimmedUsername, password);
        return playerRepository.save(player);
    }

    /**
     * Authenticate a player. Returns the Player on success, or null if credentials are wrong.
     */
    @Transactional(readOnly = true)
    public Player login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        Player player = playerRepository.findByUsername(username.trim());
        if (player == null) {
            return null;
        }
        if (!player.getPassword().equals(password)) {
            return null;
        }
        return player;
    }

    /**
     * Update the best score only if the new score is higher than the current best.
     */
    public void updateBestScore(Long playerId, int newScore) {
        playerRepository.updateBestScore(playerId, newScore);
    }

    @Transactional(readOnly = true)
    public Player findById(Long id) {
        return playerRepository.findById(id);
    }
}
