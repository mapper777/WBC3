package application.service;

import application.config.DescriptionProperties;
import application.model.AdminInitData;
import application.model.InitData;
import application.model.Option;
import application.model.Tune;
import application.repository.BaseRepository;
import application.repository.RuleRepository;
import com.couchbase.client.java.manager.collection.CollectionSpec;
import com.couchbase.client.java.manager.collection.ScopeSpec;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class InitDataService {

    private final HttpServletRequest httpServletRequest;
    private final BaseRepository baseRepository;
    private final RuleRepository ruleRepository;
    private final DescriptionProperties props;
    private final TuneService tuneService;
    @Value("classpath:access/plist.txt")
    private Resource plistResource;

    public InitDataService(BaseRepository baseRepository, RuleRepository ruleRepository, DescriptionProperties props, TuneService tuneService, HttpServletRequest httpServletRequest) {
        this.baseRepository = baseRepository;
        this.ruleRepository = ruleRepository;
        this.props = props;
        this.tuneService = tuneService;
        this.httpServletRequest = httpServletRequest;
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

    public InitData getInitData(String wUser) throws IOException {

        initUserCollectionIfAbsent(wUser);

        List<String> jobs = getJobs(); // help/job.txt

        // Для студента только одна "коллекция": его имя
        String rulesList = buildStudentRulesHtml(wUser); // генерируем HTML

        Tune tune = tuneService.getOrCreateTune(wUser);

        String dTime = "0", autoS = "0", helP = "0", tplName = "";
        String language = "", scanner = "", parser = "";
        boolean saveNN = false;

        if (tune != null) {
            dTime = String.valueOf(tune.getDeltat());
            language = tune.getLanguage();
            scanner = tune.getScanner();
            parser = tune.getParser();
            if (language != null && scanner != null && parser != null) {
                tplName = language + "/" + scanner + parser;
            }
            String saving = tune.getSaving();
            saveNN = tune.getSaving().startsWith("+");
            if (saveNN && saving.length() > 1) {
                autoS = saving.substring(1);
            } else {
                autoS = "0";
            }

            helP = String.valueOf(tune.getAssist());
        }

        return InitData.builder()
                .wUser(wUser)
                .jobs(jobs)
                .rulesList(rulesList)
                .dTime(dTime)
                .tplName(tplName)
                .saveNN(saveNN)
                .autoS(autoS)
                .helP(helP)
                .language(language)
                .scanner(scanner)
                .parser(parser)
                .languages(parse(props.getLangMap(), tune.getLanguage()))
                .scanners(parse(props.getScanMap(), tune.getScanner()))
                .parsers(parse(props.getParsMap(), tune.getParser()))
                .build();
    }


    public AdminInitData getAdminInitData(String wUser) throws IOException {
        initUserCollectionIfAbsent(wUser);

        List<String> jobs = getJobs();
        String pw = readAccessList(); // access/plist.txt
        List<String> sampleRules = ruleRepository.findSampleRuleNames();
        List<String> userRules = ruleRepository.findUserRuleNames(wUser);
        String rulesList = buildStudentRulesHtml(wUser);
        Tune tune = tuneService.getOrCreateTune(wUser);
        String dTime = "0", autoS = "0", helP = "0", tplName = "";
        String language = "", scanner = "", parser = "";
        boolean saveNN = false;

        List<String> collsList = new ArrayList<>(baseRepository.getCollectionNames());
        Collections.sort(collsList);

        if (tune != null) {
            dTime = String.valueOf(tune.getDeltat());
            language = tune.getLanguage();
            scanner = tune.getScanner();
            parser = tune.getParser();
            if (language != null && scanner != null && parser != null) {
                tplName = language + "/" + scanner + parser;
            }
            String saving = tune.getSaving();
            saveNN = tune.getSaving().startsWith("+");
            if (saveNN && saving.length() > 1) {
                autoS = saving.substring(1);
            } else {
                autoS = "0";
            }

            helP = String.valueOf(tune.getAssist());
        }

        return AdminInitData.builder()
                .wUser(wUser)
                .pw(pw)
                .jobs(jobs)
                .sampleRules(sampleRules)
                .userRules(userRules)
                .rulesList(rulesList) // убираем
                .collsList(collsList)
                .collsPreview(collsList.stream().limit(10).toList())
                .hasMore(collsList.size() > 10)
                .dTime(dTime)
                .tplName(tplName)
                .saveNN(saveNN)
                .autoS(autoS)
                .helP(helP)
                .language(language)
                .scanner(scanner)
                .parser(parser)
                .languages(parse(props.getLangMap(), tune.getLanguage()))
                .scanners(parse(props.getScanMap(), tune.getScanner()))
                .parsers(parse(props.getParsMap(), tune.getParser()))
                .build();
    }

    private String readAccessList() throws IOException {
//        String mainDir = httpServletRequest
//                .getServletContext()
//                .getRealPath("/")
//                .replace('\\', '/');
//        Path plist = Paths.get(mainDir, "access", "plist.txt");
//
        StringBuilder pw = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(plistResource.getInputStream(), StandardCharsets.UTF_8))) {
            br.readLine();
            br.readLine();

            String line;
            while ((line = br.readLine()) != null && !line.isEmpty()) {
                String[] parts = line.split("~");
                if (parts.length >= 3) {
                    pw.append(parts[0])
                            .append(':')
                            .append(parts[2])
                            .append('#');
                }
            }
        }

        return pw.toString();
    }


    private List<Option> parse(Map<String, String> map, String current) throws IOException {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    String[] parts = e.getValue().split(":", 2);
                    String id = parts[0], label = parts[1];
//                    String id    = parts[0];
//                    String label = parts.length > 1 ? parts[1] : parts[0];


                    Resource res = new ClassPathResource("templates/" + id);
                    boolean disabled = !res.exists();

                    boolean selected = id.equals(current);
                    return new Option(id, label, disabled, selected);
                })
                .collect(Collectors.toList());
    }

    public String buildStudentRulesHtml(String wUser) {
        StringBuilder sb = new StringBuilder();

        List<String> sampleRules = ruleRepository.findSampleRuleNames();
        if (!sampleRules.isEmpty()) {
            sb.append("<tr class='coll'><td><span>Samples:</span>");
            for (String rule : sampleRules) {
                sb.append("<tr><td>&nbsp;<span>").append(rule).append("</span>");
            }
        }

        List<String> userRules = ruleRepository.findUserRuleNames(wUser);
        sb.append("<tr class='coll'><td><span>").append(wUser).append(":</span>");
        for (String rule : userRules) {
            sb.append("<tr><td>&nbsp;<span>");
            if ("noName".equals(rule)) {
                sb.append("&nbsp;");
            } else {
                sb.append(rule);
            }
            sb.append("</span>");
        }

        return sb.toString();
    }

    public List<String> getJobs() throws IOException {
        String mainDir = new File(".").getCanonicalPath(); // или config.getAppDir()
        File jobFile = new File(mainDir + "/help/job.txt");

        List<String> jobs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(jobFile), "UTF-8")
        )) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    jobs.add(line.trim()); // добавляем без обёртки в кавычки
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }

        return jobs;
    }
}
