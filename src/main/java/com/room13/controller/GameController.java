package com.room13.controller;

import com.room13.model.Level;
import com.room13.model.Player;
import com.room13.model.Riddle;
import com.room13.service.GameService;
import com.room13.service.PlayerService;
import com.room13.service.SaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class GameController {

    @Autowired private GameService gameService;
    @Autowired private PlayerService playerService;
    @Autowired private SaveService saveService;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Player requirePlayer(HttpSession session) {
        return (Player) session.getAttribute("player");
    }

    /**
     * Compute elapsed time since last action and subtract it from remainingSeconds.
     * Returns the updated value.
     */
    private int updateTimer(HttpSession session) {
        Object lastObj = session.getAttribute("lastActionTime");
        Object remObj  = session.getAttribute("remainingSeconds");
        if (lastObj == null || remObj == null) return 0;

        long lastActionTime  = (Long) lastObj;
        int  remainingSeconds = (Integer) remObj;
        long now     = System.currentTimeMillis();
        int  elapsed = (int) ((now - lastActionTime) / 1000L);
        int  updated = Math.max(0, remainingSeconds - elapsed);
        session.setAttribute("remainingSeconds", updated);
        session.setAttribute("lastActionTime", now);
        return updated;
    }

    /** Build the List<String> shown in PIN display: digit or "?". */
    private List<String> buildDisplay(int[] revealedDigits) {
        List<String> display = new ArrayList<>();
        for (int d : revealedDigits) {
            display.add(d == -1 ? "?" : String.valueOf(d));
        }
        return display;
    }

    /** True when every position in revealedDigits is filled (≠ -1). */
    private boolean allRevealed(int[] arr) {
        for (int v : arr) if (v == -1) return false;
        return true;
    }

    // ── MENU ─────────────────────────────────────────────────────────────────

    @GetMapping("/menu")
    public String menu(HttpSession session, Model model) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        model.addAttribute("player", player);
        model.addAttribute("savedGames", saveService.getSavedGames(player.getId()));
        return "menu";
    }

    // ── START GAME ────────────────────────────────────────────────────────────

    @PostMapping("/game/start")
    public String startGame(@RequestParam("level") String level, HttpSession session) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        Level lvl;
        try {
            lvl = Level.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "redirect:/menu";
        }

        List<Riddle> riddles = gameService.getRiddlesForLevel(lvl);
        int[] revealedDigits = gameService.initRevealedDigits(lvl);

        // Compute correct PIN from riddle digits
        String correctPin = riddles.stream()
                .map(r -> String.valueOf(r.getDigit()))
                .collect(Collectors.joining());

        session.setAttribute("level", lvl.name());
        session.setAttribute("riddles", riddles);
        session.setAttribute("riddleIndex", 0);
        session.setAttribute("revealedDigits", revealedDigits);
        session.setAttribute("correctPin", correctPin);
        session.setAttribute("remainingSeconds", gameService.getInitialTime(lvl));
        session.setAttribute("lastActionTime", System.currentTimeMillis());
        session.setAttribute("currentScore", 0);
        session.setAttribute("gameOver", false);
        session.setAttribute("escaped", false);
        session.setAttribute("hintUsed", false);
        session.setAttribute("hintText", null);
        session.setAttribute("autoSolved", false);
        session.setAttribute("errorMessage", null);
        session.setAttribute("lastSavedAt", null);

        return "redirect:/game";
    }

    // ── GAME PAGE ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    @GetMapping("/game")
    public String game(HttpSession session, Model model) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        List<Riddle> riddles = (List<Riddle>) session.getAttribute("riddles");
        if (riddles == null) return "redirect:/menu";

        int remainingSeconds = updateTimer(session);
        if (remainingSeconds <= 0) {
            session.setAttribute("gameOver", true);
            return "redirect:/gameover";
        }

        int riddleIndex = (Integer) session.getAttribute("riddleIndex");

        // All riddles solved — go to PIN entry
        if (riddleIndex >= riddles.size()) {
            return "redirect:/game/enter-pin";
        }

        int[]  revealedDigits = (int[]) session.getAttribute("revealedDigits");
        String levelStr       = (String)  session.getAttribute("level");
        int    currentScore   = asInt(session.getAttribute("currentScore"));
        String hintText       = (String)  session.getAttribute("hintText");
        boolean hintUsed      = asBool(session.getAttribute("hintUsed"));
        boolean autoSolved    = asBool(session.getAttribute("autoSolved"));
        String errorMessage   = (String)  session.getAttribute("errorMessage");
        String lastSavedAt    = (String)  session.getAttribute("lastSavedAt");

        // Clear one-shot message
        session.removeAttribute("errorMessage");

        Riddle currentRiddle = riddles.get(riddleIndex);

        model.addAttribute("player",         player);
        model.addAttribute("level",          levelStr);
        model.addAttribute("riddle",         currentRiddle);
        model.addAttribute("riddleNumber",   riddleIndex + 1);
        model.addAttribute("riddleCount",    riddles.size());
        model.addAttribute("revealedDigitsDisplay", buildDisplay(revealedDigits));
        model.addAttribute("remainingSeconds", remainingSeconds);
        model.addAttribute("currentScore",   currentScore);
        model.addAttribute("hint",           hintText);
        model.addAttribute("hintUsed",       hintUsed);
        model.addAttribute("autoSolved",     autoSolved);
        model.addAttribute("errorMessage",   errorMessage);
        model.addAttribute("lastSavedAt",    lastSavedAt);

        return "game";
    }

    // ── SUBMIT ANSWER ─────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    @PostMapping("/game/answer")
    public String submitAnswer(@RequestParam("answer") String answer,
                               HttpSession session,
                               RedirectAttributes redirectAttrs) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        List<Riddle> riddles = (List<Riddle>) session.getAttribute("riddles");
        if (riddles == null) return "redirect:/menu";

        int remainingSeconds = updateTimer(session);
        if (remainingSeconds <= 0) {
            session.setAttribute("gameOver", true);
            return "redirect:/gameover";
        }

        int    riddleIndex    = (Integer) session.getAttribute("riddleIndex");
        int[]  revealedDigits = (int[]) session.getAttribute("revealedDigits");
        Riddle current        = riddles.get(riddleIndex);

        if (gameService.checkAnswer(current, answer)) {
            // Correct — reveal digit
            revealedDigits[riddleIndex] = current.getDigit();
            session.setAttribute("revealedDigits", revealedDigits);
            session.setAttribute("riddleIndex", riddleIndex + 1);
            // Reset per-riddle state
            session.setAttribute("hintUsed", false);
            session.setAttribute("hintText", null);
            session.setAttribute("autoSolved", false);

            if (riddleIndex + 1 >= riddles.size()) {
                return "redirect:/game/enter-pin";
            }
            return "redirect:/game";
        } else {
            session.setAttribute("errorMessage", "Wrong answer. Try again!");
            return "redirect:/game";
        }
    }

    // ── HINT ─────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    @PostMapping("/game/hint")
    public String useHint(HttpSession session) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        boolean hintUsed = asBool(session.getAttribute("hintUsed"));
        if (hintUsed) return "redirect:/game";

        updateTimer(session);
        gameService.applyHintPenalty(session);

        List<Riddle> riddles    = (List<Riddle>) session.getAttribute("riddles");
        int          riddleIndex = (Integer)        session.getAttribute("riddleIndex");
        if (riddles != null && riddleIndex < riddles.size()) {
            session.setAttribute("hintText", riddles.get(riddleIndex).getHint());
        }
        session.setAttribute("hintUsed", true);
        return "redirect:/game";
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/game/solve")
    public String autoSolve(HttpSession session) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        List<Riddle> riddles     = (List<Riddle>) session.getAttribute("riddles");
        int          riddleIndex = (Integer)       session.getAttribute("riddleIndex");
        if (riddles == null || riddleIndex >= riddles.size()) return "redirect:/game";

        if (!asBool(session.getAttribute("autoSolved"))) {
            updateTimer(session);
            gameService.applySolvePenalty(session);

            int[] revealedDigits = (int[]) session.getAttribute("revealedDigits");
            revealedDigits[riddleIndex] = riddles.get(riddleIndex).getDigit();
            session.setAttribute("revealedDigits", revealedDigits);

            session.setAttribute("autoSolved", true);
        }

        return "redirect:/game";
    }

    @PostMapping("/game/next")
    public String nextRiddle(HttpSession session) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        List<Riddle> riddles = (List<Riddle>) session.getAttribute("riddles");
        int riddleIndex = (Integer) session.getAttribute("riddleIndex");
        if (riddles == null) return "redirect:/menu";

        // Advance to next riddle
        session.setAttribute("riddleIndex", riddleIndex + 1);
        session.setAttribute("hintUsed", false);
        session.setAttribute("hintText", null);
        session.setAttribute("autoSolved", false);

        if (riddleIndex + 1 >= riddles.size()) {
            return "redirect:/game/enter-pin";
        }
        return "redirect:/game";
    }

    // ── SAVE ─────────────────────────────────────────────────────────────────

    @PostMapping("/game/save")
    public String saveGame(HttpSession session) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        updateTimer(session);
        saveService.saveGame(player, session);
        return "redirect:/game";
    }

    // ── LOAD ─────────────────────────────────────────────────────────────────

    @GetMapping("/game/load/{id}")
    public String loadGame(@PathVariable Long id, HttpSession session) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        saveService.loadGame(id, session);
        return "redirect:/game";
    }

    // ── DELETE SAVED GAME ────────────────────────────────────────────────────

    @PostMapping("/game/delete/{id}")
    public String deleteSavedGame(@PathVariable Long id, HttpSession session) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        saveService.deleteSavedGame(id);
        return "redirect:/menu";
    }

    // ── ENTER PIN PAGE ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    @GetMapping("/game/enter-pin")
    public String enterPin(HttpSession session, Model model) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        List<Riddle> riddles = (List<Riddle>) session.getAttribute("riddles");
        if (riddles == null) return "redirect:/menu";

        int remainingSeconds = updateTimer(session);
        if (remainingSeconds <= 0) {
            session.setAttribute("gameOver", true);
            return "redirect:/gameover";
        }

        int[]  revealedDigits = (int[]) session.getAttribute("revealedDigits");
        String levelStr       = (String) session.getAttribute("level");
        String pinError       = (String) session.getAttribute("pinError");
        session.removeAttribute("pinError");

        model.addAttribute("player",               player);
        model.addAttribute("level",                levelStr);
        model.addAttribute("revealedDigitsDisplay", buildDisplay(revealedDigits));
        model.addAttribute("remainingSeconds",     remainingSeconds);
        model.addAttribute("pinLength",            revealedDigits.length);
        model.addAttribute("pinError",             pinError);

        return "enter-pin";
    }

    // ── SUBMIT PIN ────────────────────────────────────────────────────────────

    @PostMapping("/game/submit-pin")
    public String submitPin(@RequestParam("pin") String pin, HttpSession session) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        int remainingSeconds = updateTimer(session);
        if (remainingSeconds <= 0) {
            session.setAttribute("gameOver", true);
            return "redirect:/gameover";
        }

        String correctPin = (String) session.getAttribute("correctPin");
        if (correctPin != null && correctPin.equals(pin.trim())) {
            String levelStr = (String) session.getAttribute("level");
            Level  lvl      = Level.valueOf(levelStr);
            int    score    = gameService.computeScore(lvl, remainingSeconds);
            playerService.updateBestScore(player.getId(), score);
            // Refresh player in session with updated best score
            Player updated = playerService.findById(player.getId());
            session.setAttribute("player", updated);

            session.setAttribute("escaped", true);
            session.setAttribute("finalScore", score);
            session.setAttribute("finalLevel", levelStr);
            session.setAttribute("finalRemainingSeconds", remainingSeconds);
            return "redirect:/win";
        } else {
            session.setAttribute("pinError", "Incorrect PIN. Try again.");
            return "redirect:/game/enter-pin";
        }
    }

    // ── TIMEOUT ──────────────────────────────────────────────────────────────

    @PostMapping("/game/timeout")
    public String timeout(HttpSession session) {
        session.setAttribute("gameOver", true);
        session.setAttribute("escaped", false);
        return "redirect:/gameover";
    }

    // ── WIN PAGE ──────────────────────────────────────────────────────────────

    @GetMapping("/win")
    public String win(HttpSession session, Model model) {
        Player player = requirePlayer(session);
        if (player == null) return "redirect:/login";

        if (!asBool(session.getAttribute("escaped"))) {
            return "redirect:/menu";
        }

        model.addAttribute("player",                 player);
        model.addAttribute("finalScore",             session.getAttribute("finalScore"));
        model.addAttribute("finalLevel",             session.getAttribute("finalLevel"));
        model.addAttribute("finalRemainingSeconds",  session.getAttribute("finalRemainingSeconds"));
        return "win";
    }

    // ── GAME OVER PAGE ────────────────────────────────────────────────────────

    @GetMapping("/gameover")
    public String gameover(HttpSession session, Model model) {
        Player player = requirePlayer(session);
        model.addAttribute("player", player);
        model.addAttribute("level",  session.getAttribute("level"));
        return "gameover";
    }

    // ── Utility ──────────────────────────────────────────────────────────────

    private int asInt(Object o) {
        return o instanceof Integer ? (Integer) o : 0;
    }

    private boolean asBool(Object o) {
        return Boolean.TRUE.equals(o);
    }
}
