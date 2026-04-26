package com.room13.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.room13.model.Player;
import com.room13.model.Riddle;
import com.room13.model.SavedGame;
import com.room13.repository.SavedGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SaveService {

    @Autowired
    private SavedGameRepository savedGameRepository;

    @Autowired
    private GameService gameService;

    private final ObjectMapper mapper = new ObjectMapper();

    /** Serialize current session state into a SavedGame row. */
    @SuppressWarnings("unchecked")
    public SavedGame saveGame(Player player, HttpSession session) {
        try {
            String level = (String) session.getAttribute("level");
            int riddleIndex = asInt(session.getAttribute("riddleIndex"));
            int remainingSeconds = asInt(session.getAttribute("remainingSeconds"));
            int currentScore = asInt(session.getAttribute("currentScore"));
            int[] revealedDigits = (int[]) session.getAttribute("revealedDigits");
            List<Riddle> riddles = (List<Riddle>) session.getAttribute("riddles");
            String correctPin = (String) session.getAttribute("correctPin");

            String revealedDigitsJson = mapper.writeValueAsString(revealedDigits);
            String riddleIds = riddles.stream()
                    .map(r -> String.valueOf(r.getId()))
                    .collect(Collectors.joining(","));

            SavedGame sg = new SavedGame();
            sg.setPlayer(player);
            sg.setLevel(level);
            sg.setRevealedDigits(revealedDigitsJson);
            sg.setRiddleIndex(riddleIndex);
            sg.setRemainingSeconds(remainingSeconds);
            sg.setCurrentScore(currentScore);
            sg.setRiddleIds(riddleIds);
            sg.setCorrectPin(correctPin);
            sg.setSavedAt(Timestamp.from(Instant.now()));

            savedGameRepository.save(sg);
            session.setAttribute("lastSavedAt", sg.getSavedAt().toString());
            return sg;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save game: " + e.getMessage(), e);
        }
    }

    /** Restore session state from a persisted SavedGame. */
    public void loadGame(Long savedGameId, HttpSession session) {
        try {
            SavedGame sg = savedGameRepository.findById(savedGameId);
            if (sg == null) {
                throw new RuntimeException("Saved game not found: " + savedGameId);
            }

            List<Integer> ids = Arrays.stream(sg.getRiddleIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            List<Riddle> riddles = gameService.getRiddlesByIds(ids);
            int[] revealedDigits = mapper.readValue(sg.getRevealedDigits(), int[].class);

            session.setAttribute("level", sg.getLevel());
            session.setAttribute("riddles", riddles);
            session.setAttribute("riddleIndex", sg.getRiddleIndex());
            session.setAttribute("revealedDigits", revealedDigits);
            session.setAttribute("remainingSeconds", sg.getRemainingSeconds());
            session.setAttribute("currentScore", sg.getCurrentScore());
            session.setAttribute("correctPin", sg.getCorrectPin());
            session.setAttribute("lastActionTime", System.currentTimeMillis());
            session.setAttribute("gameOver", false);
            session.setAttribute("escaped", false);
            session.setAttribute("hintUsed", false);
            session.setAttribute("hintText", null);
            session.setAttribute("autoSolved", false);
            session.setAttribute("errorMessage", null);
            session.setAttribute("lastSavedAt", sg.getSavedAt().toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load game: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<SavedGame> getSavedGames(Long playerId) {
        return savedGameRepository.findByPlayerId(playerId);
    }

    public void deleteSavedGame(Long id) {
        savedGameRepository.deleteById(id);
    }

    private int asInt(Object o) {
        return o instanceof Integer ? (Integer) o : 0;
    }
}
