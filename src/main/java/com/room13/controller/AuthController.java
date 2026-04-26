package com.room13.controller;

import com.room13.model.Player;
import com.room13.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private PlayerService playerService;

    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("player") != null) {
            return "redirect:/menu";
        }
        return "redirect:/login";
    }

    // ── REGISTER ────────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String showRegister(HttpSession session, Model model) {
        if (session.getAttribute("player") != null) {
            return "redirect:/menu";
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("confirmPassword") String confirmPassword,
                           RedirectAttributes redirectAttrs) {
        if (!password.equals(confirmPassword)) {
            redirectAttrs.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/register";
        }
        try {
            playerService.register(username, password);
            redirectAttrs.addFlashAttribute("success", "Account created. You may now log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    // ── LOGIN ────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String showLogin(HttpSession session) {
        if (session.getAttribute("player") != null) {
            return "redirect:/menu";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session,
                        RedirectAttributes redirectAttrs) {
        Player player = playerService.login(username, password);
        if (player == null) {
            redirectAttrs.addFlashAttribute("error", "Invalid username or password.");
            return "redirect:/login";
        }
        session.setAttribute("player", player);
        return "redirect:/menu";
    }

    // ── LOGOUT ───────────────────────────────────────────────────────────────

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
