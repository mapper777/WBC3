package application.service;

import application.model.Rule;

import java.io.IOException;
import java.util.Optional;

public interface AuthService {
    void init() throws IOException;
    void loadRulesFromFile(String path) throws IOException;
    boolean isValidLogin(String login, String password);
    boolean hasAccess(String login, String uri);
    Optional<Rule> getRuleForUser(String login);
    boolean isAllowedWithPassword(String login, String uri);
    boolean isAllowedWithoutPassword(String uri);


}
