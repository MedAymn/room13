package com.room13.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "saved_game")
public class SavedGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "level", nullable = false, length = 10)
    private String level;

    @Column(name = "revealed_digits", columnDefinition = "TEXT")
    private String revealedDigits;

    @Column(name = "riddle_index")
    private int riddleIndex = 0;

    @Column(name = "remaining_seconds")
    private int remainingSeconds;

    @Column(name = "current_score")
    private int currentScore = 0;

    @Column(name = "riddle_ids", length = 200)
    private String riddleIds;

    @Column(name = "correct_pin", length = 20)
    private String correctPin;

    @Column(name = "saved_at")
    private Timestamp savedAt;

    public SavedGame() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getRevealedDigits() { return revealedDigits; }
    public void setRevealedDigits(String revealedDigits) { this.revealedDigits = revealedDigits; }

    public int getRiddleIndex() { return riddleIndex; }
    public void setRiddleIndex(int riddleIndex) { this.riddleIndex = riddleIndex; }

    public int getRemainingSeconds() { return remainingSeconds; }
    public void setRemainingSeconds(int remainingSeconds) { this.remainingSeconds = remainingSeconds; }

    public int getCurrentScore() { return currentScore; }
    public void setCurrentScore(int currentScore) { this.currentScore = currentScore; }

    public String getRiddleIds() { return riddleIds; }
    public void setRiddleIds(String riddleIds) { this.riddleIds = riddleIds; }

    public String getCorrectPin() { return correctPin; }
    public void setCorrectPin(String correctPin) { this.correctPin = correctPin; }

    public Timestamp getSavedAt() { return savedAt; }
    public void setSavedAt(Timestamp savedAt) { this.savedAt = savedAt; }
}
