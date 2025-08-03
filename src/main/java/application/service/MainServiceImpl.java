package application.service;

import application.config.DescriptionProperties;
import application.model.InitData;
import application.model.Option;
import application.model.Tune;
import application.repository.BaseRepository;
import application.repository.RuleRepository;
import com.couchbase.client.java.manager.collection.CollectionSpec;
import com.couchbase.client.java.manager.collection.ScopeSpec;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MainServiceImpl implements MainService {

    private final ResourceLoader resourceLoader;
    TuneService tuneService;
    RuleRepository ruleRepository;
    BaseRepository baseRepository;
    private DescriptionProperties props;
    InitDataService initDataService;

//    @Autowired
//    ServletContext servletContext;

    public MainServiceImpl(TuneService tuneService, RuleRepository ruleRepository, ResourceLoader resourceLoader,
                           BaseRepository baseRepository, InitDataService initDataService) {
        this.tuneService = tuneService;
        //this.servletContext = servletContext;
        this.ruleRepository = ruleRepository;
        this.resourceLoader = resourceLoader;
        this.baseRepository = baseRepository;
        this.initDataService = initDataService;
    }

    @Override
    public InitData getInitData(String wUser) throws IOException {
        return initDataService.getInitData(wUser);
    }

//    @Override
//    public InitData getInitData(String wUser) throws IOException {
//
//        initUserCollectionIfAbsent(wUser);
//
//
//        List<String> jobs = getJobs(); // help/job.txt
//
//        // Для студента только одна "коллекция": его имя
//        String collsList = buildStudentRulesHtml(wUser); // генерируем HTML
//
//        Tune tune = tuneService.getOrCreateTune(wUser);
//
//        String dTime = "0", autoS = "0", helP = "0", tplName = "";
//        String language = "", scanner = "", parser = "";
//        boolean saveNN = false;
//
//        if (tune != null) {
//            dTime = String.valueOf(tune.getDeltat());
//            language = tune.getLanguage();
//            scanner = tune.getScanner();
//            parser = tune.getParser();
//            if (language != null && scanner != null && parser != null) {
//                tplName = language + "/" + scanner + parser;
//            }
//            saveNN = tune.getSaving().contains("+");
//            autoS = saveNN ? "1" : "0";
//            helP = String.valueOf(tune.getAssist());
//        }
//
//        return InitData.builder()
//                .wUser(wUser)
//                .jobs(jobs)
//                .collsList(collsList)
//                .dTime(dTime)
//                .tplName(tplName)
//                .saveNN(saveNN)
//                .autoS(autoS)
//                .helP(helP)
//                .language(language)
//                .scanner(scanner)
//                .parser(parser)
//                .languages(parse(props.getLangMap(), tune.getLanguage()))
//                .scanners(parse(props.getScanMap(), tune.getScanner()))
//                .parsers(parse(props.getParsMap(), tune.getParser()))
//                .build();
//    }
//
//    private List<Option> parse(Map<String, String> map, String current) throws IOException {
//        return map.entrySet()
//                .stream()
//                .sorted(Map.Entry.comparingByKey())
//                .map(e -> {
//                    String[] parts = e.getValue().split(":", 2);
//                    String label = parts[0], id = parts[1];
//
//
//                    boolean disabled;
//                    try {
//                        File dir = new ClassPathResource("templates/" + id).getFile();
//                        disabled = !dir.exists();
//                    } catch (IOException ex) {
//                        disabled = true;
//                    }
//
//                    boolean selected = id.equals(current);
//                    return new Option(id, label, disabled, selected);
//                })
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<String> getJobs() throws IOException {
//        String mainDir = new File(".").getCanonicalPath(); // или config.getAppDir()
//        File jobFile = new File(mainDir + "/help/job.txt");
//
//        List<String> jobs = new ArrayList<>();
//        try (BufferedReader br = new BufferedReader(
//                new InputStreamReader(new FileInputStream(jobFile), "UTF-8")
//        )) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                if (!line.trim().isEmpty()) {
//                    jobs.add(line.trim()); // добавляем без обёртки в кавычки
//                }
//            }
//        } catch (Exception e) {
//            e.getMessage();
//        }
//
//        return jobs;
//    }
//
//    @Override
//    public String buildStudentRulesHtml(String wUser) {
//        StringBuilder sb = new StringBuilder();
//
//        List<String> sampleRules = ruleRepository.findSampleRuleNames();
//        if (!sampleRules.isEmpty()) {
//            sb.append("<tr class='coll'><td><span>Samples:</span>");
//            for (String rule : sampleRules) {
//                sb.append("<tr><td>&nbsp;<span>").append(rule).append("</span>");
//            }
//        }
//
//        List<String> userRules = ruleRepository.findUserRuleNames(wUser);
//        sb.append("<tr class='coll'><td><span>").append(wUser).append(":</span>");
//        for (String rule : userRules) {
//            sb.append("<tr><td>&nbsp;<span>");
//            if ("noName".equals(rule)) {
//                sb.append("&nbsp;");
//            } else {
//                sb.append(rule);
//            }
//            sb.append("</span>");
//        }
//
//        return sb.toString();
//    }

//    @Override
//    public File getFile(String filename) throws IOException {
//
//        Resource resource = resourceLoader.getResource("classpath:users/" + filename);
//        //String basePath = servletContext.getRealPath("/").replace("\\", "/");
//        File userDir = new File(basePath, "users");
//        File requested = new File(userDir, filename).getCanonicalFile();
//
//        if (!requested.getPath().startsWith(userDir.getCanonicalPath()) || !requested.exists()) {
//            throw new FileNotFoundException("Недопустимое имя файла: " + filename);
//        }
//
//        return requested;
//    }

    @Override
    public File getFile(String filename, ServletRequest request) throws IOException {

        String mainDir = request.getServletContext().getRealPath("/").replaceAll("\\\\", "/");
        //Resource resource = resourceLoader.getResource("classpath:users/" + filename);
        ClassPathResource resource = new ClassPathResource("users/" + filename);
        //Resource resource = resourceLoader.getResource(mainDir + "users/" + filename);
        File file = resource.getFile();

        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("Файл не найден: " + filename);
        }

        return file;
    }

    @Override
    public void clearSession(HttpServletRequest request, HttpServletResponse response) {
        // Удалить куку
        Cookie cookie = new Cookie("WBCuser", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        // Удалить файл пользователя
        // String basePath = servletContext.getRealPath("/").replace("\\", "/");
        HttpSession session = request.getSession(false);
        if (session != null) {
            String wUser = (String) session.getAttribute("WBCuser");
            if (wUser != null) {
                File file = new File("users/" + wUser + "translator.txt");
                if (file.exists()) {
                    file.delete();
                }
            }
            session.invalidate(); // завершить сессию
        }
    }

    public void initUserCollectionIfAbsent(String user) {
        try {
            ScopeSpec scope = baseRepository.getBucket().collections().getScope("_default");
            boolean exists = scope.collections().stream()
                    .anyMatch(c -> c.name().equals(user));

            if (!exists) {
                baseRepository.getBucket().collections().createCollection(CollectionSpec.create(user, "_default"));
                Thread.sleep(500);
                baseRepository.getCluster().query("CREATE PRIMARY INDEX ON `" + baseRepository.getBucket().name() + "`.`_default`.`" + user + "`");
                //log.info("Создана коллекция и индекс для пользователя: {}", user);
            }
        } catch (Exception e) {
            //log.warn("Ошибка при инициализации коллекции пользователя: " + user, e);
        }
    }

}
