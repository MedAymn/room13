package com.room13.repository;

import com.room13.model.SavedGame;
import java.util.List;

public interface SavedGameRepository {
    SavedGame save(SavedGame savedGame);
    SavedGame update(SavedGame savedGame);
    SavedGame findById(Long id);
    List<SavedGame> findByPlayerId(Long playerId);
    void deleteById(Long id);
}
