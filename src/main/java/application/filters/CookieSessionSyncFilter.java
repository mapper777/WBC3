package application.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class CookieSessionSyncFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String sessionLogin = (session != null) ? (String) session.getAttribute("WBCuser") : null;

        String cookieLogin = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("WBCuser".equals(cookie.getName())) {
                    cookieLogin = cookie.getValue();
                    break;
                }
            }
        }
        // если в сессии нет логина, но есть в куке -- положим в сессию
        if (sessionLogin == null && cookieLogin != null) {
            request.getSession(true).setAttribute("WBCuser", cookieLogin);
        }
        // если в сессии есть, а куки нет -- восстановим куку
        if (sessionLogin != null && cookieLogin == null) {
            Cookie newCookie = new Cookie("WBCuser", sessionLogin);
            newCookie.setPath("/");
            newCookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(newCookie);
        }

        filterChain.doFilter(request, response);
    }
}
