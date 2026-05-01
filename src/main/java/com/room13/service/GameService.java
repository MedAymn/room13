package com.room13.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.room13.model.Level;
import com.room13.model.Riddle;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService {

    private Map<Level, List<Riddle>> riddlesByLevel;

    @PostConstruct
    public void loadRiddles() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("riddles.json")) {
            if (is == null) {
                throw new RuntimeException("riddles.json not found on classpath");
            }
            List<Riddle> all = mapper.readValue(is, new TypeReference<List<Riddle>>() {});
            riddlesByLevel = new HashMap<>();
            for (Riddle r : all) {
                Level lvl = Level.valueOf(r.getLevel());
                riddlesByLevel.computeIfAbsent(lvl, k -> new ArrayList<>()).add(r);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load riddles.json: " + e.getMessage(), e);
        }
    }

    /** Return a freshly shuffled subset of riddles for the given level. */
    public List<Riddle> getRiddlesForLevel(Level level) {
        List<Riddle> pool = new ArrayList<>(riddlesByLevel.getOrDefault(level, Collections.emptyList()));
        Collections.shuffle(pool);
        // Only 1 riddle per game now!
        return pool.subList(0, Math.min(1, pool.size()));
    }

    /** Look up specific riddles by their IDs (used when restoring a saved game). */
    public List<Riddle> getRiddlesByIds(List<Integer> ids) {
        Map<Integer, Riddle> index = new HashMap<>();
        for (List<Riddle> riddles : riddlesByLevel.values()) {
            for (Riddle r : riddles) {
                index.put(r.getId(), r);
            }
        }
        return ids.stream()
                .map(index::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** Case-insensitive, trimmed answer check. */
    public boolean checkAnswer(Riddle riddle, String playerAnswer) {
        return playerAnswer != null
                && playerAnswer.trim().toUpperCase().equals(riddle.getAnswer().toUpperCase());
    }

    /** Score = remaining seconds × difficulty multiplier. */
    public int computeScore(Level level, int remainingSeconds) {
        int multiplier;
        switch (level) {
            case MEDIUM: multiplier = 2; break;
            case HARD:   multiplier = 3; break;
            default:     multiplier = 1; break;
        }
        return Math.max(0, remainingSeconds) * multiplier;
    }

    public int getInitialTime(Level level) {
        switch (level) {
            case MEDIUM: return 240;
            case HARD:   return 180;
            default:     return 300;
        }
    }

    /** Subtract 30 s from the session timer (hint penalty). */
    public void applyHintPenalty(HttpSession session) {
        int remaining = getRemaining(session);
        session.setAttribute("remainingSeconds", Math.max(0, remaining - 30));
    }

    /**
     * Subtract 90 s, mark current riddle as auto-solved, and reveal its digit.
     */
    @SuppressWarnings("unchecked")
    public void applySolvePenalty(HttpSession session) {
        int remaining = getRemaining(session);
        session.setAttribute("remainingSeconds", Math.max(0, remaining - 90));
        session.setAttribute("autoSolved", true);
    }

    private int getRemaining(HttpSession session) {
        Object val = session.getAttribute("remainingSeconds");
        return val instanceof Integer ? (Integer) val : 0;
    }
}
