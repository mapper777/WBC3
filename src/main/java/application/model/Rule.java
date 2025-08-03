package application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rule {
    private String user;
    private String password;
    private String userName;
    private String[] access;

    public boolean check(String login, String pass) {
        return "*".equals(user) || (user.equals(login) && password.equals(pass));
    }

    public boolean allows(String uri) {
        if (access == null) return false;
        for (String a : access) {
            if ("*".equals(a) || uri.equals(a) || uri.startsWith(a.replace("*", ""))) return true;
        }
        return false;
    }
}