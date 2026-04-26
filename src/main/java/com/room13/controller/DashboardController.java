package com.room13.controller;

import com.room13.model.Player;
import com.room13.service.PlayerService;
import com.room13.service.SaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @Autowired private PlayerService playerService;
    @Autowired private SaveService   saveService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Player player = (Player) session.getAttribute("player");
        if (player == null) return "redirect:/login";

        // Refresh player so bestScore is up to date
        Player fresh = playerService.findById(player.getId());
        if (fresh != null) {
            session.setAttribute("player", fresh);
            player = fresh;
        }

        model.addAttribute("player",     player);
        model.addAttribute("savedGames", saveService.getSavedGames(player.getId()));
        return "dashboard";
    }
}
