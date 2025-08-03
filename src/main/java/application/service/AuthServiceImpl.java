package application.service;

import application.model.Rule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final List<Rule> rules = new ArrayList<>();
    private List<String> forAllWPass = new ArrayList<>();
    private List<String> forAllWOPass = new ArrayList<>();

    private static final String PLIST_PATH = "access/plist.txt";

    @PostConstruct
    @Override
    public void init() throws IOException {
        loadRulesFromFile(PLIST_PATH);
    }
    @Override
    public void loadRulesFromFile(String filePath) throws IOException {
        rules.clear();
        forAllWPass.clear();
        forAllWOPass.clear();

        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(filePath)) {
            if (in == null) {
                throw new FileNotFoundException("Файл не найден в classpath: " + filePath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("~");
                    if (parts[0].equals("*")) {
                        if (parts[1].equals("*")) {
                            forAllWPass = Arrays.asList(parts[3].split(","));
                        } else {
                            forAllWOPass = Arrays.asList(parts[3].split(","));
                        }
                    } else {
                        Rule rule = new Rule(parts[0], parts[1], parts[2],
                                parts.length == 4 ? parts[3].split(",") : null);
                        rules.add(rule);
                    }
                }
            }
        }

        rules.sort(Comparator.comparing(Rule::getUser));
    }


    @Override
    public boolean isValidLogin(String login, String password) {
        return rules.stream().anyMatch(r -> r.check(login, password));
    }

    @Override
    public boolean isAllowedWithoutPassword(String uri) {
        return forAllWOPass.stream().anyMatch(p -> uri.equals(p) || uri.startsWith(p.replace("*", "")));
    }
    @Override
    public boolean isAllowedWithPassword(String login, String uri) {
        Optional<Rule> matched = rules.stream().filter(r -> r.getUser().equals(login)).findFirst();
        if (matched.isEmpty()) return false;

        Rule r = matched.get();
        if (forAllWPass.contains(uri) || forAllWPass.stream().anyMatch(uri::startsWith)) return true;
        if (r.getAccess() == null) return false;

        return Arrays.stream(r.getAccess()).anyMatch(a ->
                a.equals("*") || uri.equals(a) || uri.startsWith(a.replace("*", "")));
    }

    @Override
    public Optional<Rule> getRuleForUser(String login) {
        return rules.stream().filter(r -> r.getUser().equals(login)).findFirst();
    }

    @Override
    public boolean hasAccess(String login, String uri) {
        if (isAllowedWithoutPassword(uri)) return true;

        return isAllowedWithPassword(login, uri);
    }
}