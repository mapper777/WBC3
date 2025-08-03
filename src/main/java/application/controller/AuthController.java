package application.controller;

import application.service.AuthService;
import com.couchbase.client.core.deps.com.google.api.Http;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.io.File;


@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "path", required = false) String path,
                            Model model) {
        if (error != null) model.addAttribute("error", true);
        model.addAttribute("path", path != null ? path : "/main");
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String login,
                        @RequestParam String pass,
                        HttpSession session,
                        HttpServletResponse response) {
        if (authService.isValidLogin(login, pass)) {
            session.setAttribute("WBCuser", login);
            Cookie cookie = new Cookie("WBCuser", login);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);

            if ("admin".equals(login)) {
                return "redirect:/admin";
            }
            return "redirect:/main";
        } else {
            return "redirect:/auth/login?error=true";
        }
    }

}
