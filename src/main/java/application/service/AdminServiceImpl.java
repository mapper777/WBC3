package application.service;

import application.model.AdminInitData;
import application.model.RuleDocument;
import application.model.Tune;
import application.repository.BaseRepository;
import application.repository.RuleRepository;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.manager.collection.AsyncCollectionManager;
import com.couchbase.client.java.manager.collection.CollectionManager;
import com.couchbase.client.java.manager.collection.CollectionSpec;
import com.couchbase.client.java.query.QueryResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.core.util.Json;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;
import static java.util.stream.Collectors.toMap;

@Service
public class AdminServiceImpl implements AdminService {

    private final TuneService tuneService;
    private final BaseRepository baseRepository;
    private final HttpServletRequest httpServletRequest;
    private final RuleRepository ruleRepository;

    public AdminServiceImpl(TuneService tuneService, BaseRepository baseRepository, HttpServletRequest httpServletRequest, RuleRepository ruleRepository) {
        this.tuneService = tuneService;
        this.baseRepository = baseRepository;
        this.httpServletRequest = httpServletRequest;
        this.ruleRepository = ruleRepository;
    }

    @Override
    public Resource getPlist(String filename) throws IOException {

        Resource resource = new ClassPathResource("access/plist.txt");
        if (!resource.exists()) {
            throw new FileNotFoundException("Не найден файл");
        }
        return resource;
    } // сессию будет принимать контроллер
    @Override
    public void savePlist(Reader reader) throws IOException {
        String content = new BufferedReader(reader)
                .lines()
                .collect(Collectors.joining("\n"));

        if (content.isBlank()) {
            throw new IllegalArgumentException("Nothing to save");
        }

        Path accessDir = Paths.get(".").toRealPath().resolve("access");
        Files.createDirectories(accessDir);

        Path current = accessDir.resolve("plist.txt");
        Path backup  = accessDir.resolve("plistOld.txt");

        if (Files.exists(current)) {
            Files.deleteIfExists(backup);
            Files.move(current, backup);
        }


        Files.writeString(current, content + "\n", StandardCharsets.UTF_8);

    }



    @Override
    public String listBase() throws IOException {

        StringBuilder sb = new StringBuilder();
        StringBuilder errStr = new StringBuilder();

        try {
            Path plist = Paths.get("access", "plist.txt").toRealPath();
            Map<String, String> roles = Files.lines(plist, StandardCharsets.UTF_8)
                    .skip(2)
                    .map(l -> l.split("~"))
                    .filter(p -> p.length >= 3)
                    .collect(toMap(p -> p[0], p -> p[2], (a,b) -> a));


            for (String coll : baseRepository.getCollectionNames()) {
                String role = roles.getOrDefault(coll, "???");
                sb.append(role)
                        .append(':')
                        .append(coll)
                        .append(':');

                List<String> rules = baseRepository.find(coll).stream()
                        .map(d -> d.getString("rules"))
                        .filter(Objects::nonNull)
                        .toList();

                if (!rules.isEmpty()) {
                    sb.append(String.join(",", rules));
                }

                sb.append(";\n");
            }
        } catch (Exception e) {
            errStr.append("Error").append(e.getMessage());
        }

        return errStr.isEmpty() ? sb.toString() : errStr.toString();
    }

    @Override
    public String dropColls(String names) {
        int collCnt = 0;
        int docCnt = 0;
        String errStr = "";

        try {
            CollectionManager cm = baseRepository.getBucket().collections();

            if ("!".equals(names)) {
                List<String> collections = getCollectionNames(); // метод из BaseRepository
                for (String collName : collections) {
                    if (!collName.isEmpty() && Character.isDigit(collName.charAt(0))) {
                        try {
                            cm.dropCollection(CollectionSpec.create(collName, "_default"));
                            collCnt++;
                        } catch (Exception e) {
                            errStr += "[drop:" + collName + "=>" + e.getMessage() + "]";
                        }
                    }
                }
            } else {
                String[] collList = names.split(";");
                for (String entry : collList) {
                    if (entry.isBlank()) continue;

                    int idx = entry.indexOf(":");
                    if (idx == -1) continue;

                    String collName = entry.substring(0, idx);
                    String docListStr = entry.substring(idx + 1);

                    if (docListStr.isBlank()) {
                        if (!collName.isEmpty() && Character.isDigit(collName.charAt(0))) {
                            try {
                                cm.dropCollection(CollectionSpec.create(collName, "_default"));
                                collCnt++;
                            } catch (Exception e) {
                                errStr += "[drop:" + collName + "=>" + e.getMessage() + "]";
                            }
                        }
                    } else {
                        String[] docKeys = docListStr.split(",");
                        for (String ruleKey : docKeys) {
                            try {
                                baseRepository.getCluster().query(
                                        "DELETE FROM `" + baseRepository.getBucket().name() + "`.`_default`.`"
                                                + collName + "` WHERE rules = $rule",
                                        queryOptions().parameters(JsonObject.create().put("rule", ruleKey))
                                );
                                docCnt++;
                            } catch (Exception e) {
                                errStr += "[del:" + ruleKey + "=>" + e.getMessage() + "]";
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            errStr = "Error: " + e.getMessage();
        }

        return collCnt + "|" + docCnt + "|" + errStr;
    } // возвращает строку вида в dropColls.jsp:
    //                    <%=""+collCnt+"|"+docCnt+"|"+errStr%>
    @Override
    public String getContent() throws IOException {
        Path jsonPath = Paths.get("users", "BaseContent.json");
        if (!Files.exists(jsonPath)) {
            throw new FileNotFoundException("Не найден файл: " + jsonPath);
        }

        ObjectMapper mapper = new ObjectMapper();
        List<ObjectNode> items;
        try {
            items = mapper.readValue(
                    jsonPath.toFile(),
                    new TypeReference<List<ObjectNode>>() {}
            );
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }

        StringBuilder sb = new StringBuilder();
        for (ObjectNode node : items) {
            String fio = node.path("FIO").asText("???");
            String coll = node.path("Collection").asText("?");
            sb.append(fio).append(":").append(coll).append(":");

            if (node.has("tune")) {
                sb.append("tune=").append(node.get("tune").asText()).append(",");
            }

            ArrayNode rulesArr = (ArrayNode) node.path("rules");
            for (int i = 0; i < rulesArr.size(); i++) {
                sb.append(rulesArr.get(i).asText());
                if (i < rulesArr.size() - 1) {
                    sb.append(",");
                }
            }

            sb.append(";\n");
        }

        return sb.toString();
    }
    public String getContentOld() throws IOException {
        String errStr = "";
        StringBuilder json = new StringBuilder();

        Resource resource = new ClassPathResource("users/BaseContent.json");
        if (!resource.exists()) {
            throw new FileNotFoundException("Не найден файл");
        }


        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                int ind = line.indexOf(":");
                if (ind > 0) {
                    int nd = line.indexOf(",");
                    json.append(line, ind + 2, nd - 1).append(":");

                    ind = line.indexOf(",{");
                    if (ind > nd + 17) {
                        json.append(line, nd + 15, ind - 1).append(":");
                    } else {
                        json.append("???:");
                    }

                    ind = line.indexOf(", \"rules\"");
                    while (ind >= 0) {
                        line = line.substring(ind + 12);
                        nd = line.indexOf('"');
                        if (nd > 1) {
                            json.append(line, 0, nd).append(",");
                        }
                        ind = line.indexOf(", \"rules\"");
                    }

                    json.append(";\n");
                }
            }

        } catch (FileNotFoundException e) {
            errStr = "нет такого файла";
        } catch (IOException e) {
            errStr = e.getMessage();
        }

        return errStr.isEmpty() ? json.toString() : errStr;
    } // читает содержимое BaseContent.json и выводит содержимое в виде строки


    @Override
    public String impBase() throws IOException {
        StringBuilder sb = new StringBuilder();
        try(BufferedReader reader = httpServletRequest.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String payload = sb.toString();

        int mode = Integer.parseInt(payload.substring(0, 1));
        String prefix = payload.substring(2, payload.indexOf(';'));
        String listPart = payload.substring(payload.indexOf(';') + 1);

        Map<String, Set<String>> selection = new HashMap<>();
        if (!listPart.equals("!")) {
            String[] parts = listPart.split(";");

            for (String part : parts) {
                if (part.isBlank()) continue;

                String[] kv = part.split(":", 2);
                String coll = kv[0];

                Set<String> rules = new HashSet<>();
                if (kv.length > 1 && !kv[1].isBlank()) {
                    rules.addAll(Arrays.asList(kv[1].split(",")));
                }
                selection.put(coll, rules);
            }
        } else {
            baseRepository.getCollectionNames().forEach(c -> selection.put(c, Collections.emptySet()));
        }

        Path jsonPath = Paths.get("users", "BaseContent.json");

        if (!Files.exists(jsonPath)) {
            throw new FileNotFoundException("Не найден файл: " + jsonPath);
        }

        ObjectMapper mapper = new ObjectMapper();

        List<ObjectNode> items = mapper.readValue(
                jsonPath.toFile(),
                new TypeReference<List<ObjectNode>>() {}
        );

        Set<String> existingColls = new HashSet<>(baseRepository.getCollectionNames());

        int collCnt = 0, docCnt = 0;

        for (ObjectNode node : items) {
            String origColl = node.get("Collection").asText();

            if (!selection.containsKey(origColl)) continue;

            Set<String> allowedRules = selection.get(origColl);

            boolean exists = existingColls.contains(origColl);

            if (exists && mode == 1) continue;
            String targetColl = origColl;
            if (exists && mode == 4) {
                targetColl = origColl + prefix;
                if (existingColls.contains(targetColl)) continue;
            }

            collCnt++;
            Collection cbColl = baseRepository.getCollection(targetColl);

            ArrayNode docs = (ArrayNode) node.get("docs");
            if (docs == null) continue;

            for (JsonNode docNode : docs) {
                String ruleName = docNode.get("rules").asText(null);
                if (ruleName == null) continue;

                if (!allowedRules.isEmpty() && !allowedRules.contains(ruleName)) continue;

                String id = targetColl + "::" + ruleName;

                boolean docExists = true;
                try {
                    cbColl.get(id);
                } catch (DocumentNotFoundException ex) {
                    docExists = false;
                }
                if (docExists && mode == 2) continue;

                ObjectNode body = mapper.createObjectNode();
                body.set("rules", docNode.get("rules"));
                body.set("data", docNode.get("data"));

                cbColl.upsert(id, JsonObject.fromJson(body.toString()));
                docCnt++;
            }
        }
        return collCnt + " " + docCnt;
    }

    // mongo -> couchbase
    public String impBaseOld() throws IOException {
        int collCnt = 0, iCnt = 0, uCnt = 0, eCnt = 0;

        StringBuilder sb = new StringBuilder();
        try(BufferedReader reader = httpServletRequest.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String payload = sb.toString();

        String[] parts = payload.split(";", -1);
        int mode = Integer.parseInt(parts[0].substring(0, 1));
        String prefix = parts[0].substring(2);

        Map<String, Set<String>> selection = new HashMap<>();

        if (parts.length == 1) {
            for (String c : baseRepository.getCollectionNames()) {
                selection.put(c, Collections.emptySet());
            }
        } else {
            for (int i = 1; i < parts.length; i++) {
                String p = parts[i];
                if (p.isBlank()) continue;
                String[] kv = parts[i].split(":", 2);
                String coll = kv[0];
                Set<String> rules = new HashSet<>();
                if (kv.length > 1 && !kv[1].isBlank()) {
                    rules.addAll(Arrays.asList(kv[1].split(",")));
                }

                selection.put(coll, rules);

                Set<String> existingColls = new HashSet<>(baseRepository.getCollectionNames());

                Path jsonPath = Paths.get("user", "BaseContent.json");
                if (!Files.exists(jsonPath)) {
                    throw new FileNotFoundException("Не найден файл экспорта: " + jsonPath);
                }
                List<String> lines = Files.readAllLines(jsonPath, StandardCharsets.UTF_8);

                for (String json : lines) {
                    int cIdx = json.indexOf("\"Collection\"", 8);
                    if (cIdx < 0) continue;
                    int brace = json.indexOf(",{", cIdx);
                    if (brace < 0) continue;
                    String origColl = json.substring(cIdx + 14, brace - 1);

                    if (!selection.containsKey(origColl)) continue;
                    Set<String> allowed = selection.get(origColl);

                    boolean exists = existingColls.contains(origColl);

                    if (exists && mode == 1) continue;

                    String targetColl = origColl;
                    if (exists && mode == 4) {
                        targetColl = origColl + prefix;
                        if (existingColls.contains(targetColl)) continue;
                    }

                    collCnt++;
                    Collection couchColl = baseRepository.getCollection(targetColl);

                    String[] fragments = json.split("oid\": ");
                    List<JsonObject> docList = exists
                            ? baseRepository.find(origColl)
                            : Collections.emptyList();

                    boolean firstInColl = true;
                    for (String frag : fragments) {
                        int rIdx = frag.indexOf("\"rules\":");
                        if (rIdx < 0) continue;
                        int rEnd = frag.indexOf("\",", rIdx);
                        String ruleName = frag.substring(rIdx + 10, rEnd);

                        if (!allowed.isEmpty() && !allowed.contains(ruleName)) {
                            continue;
                        }


                        JsonObject found = null;
                        int existingCount = 0;
                        for (JsonObject d : docList) {
                            if (d.containsKey("rules") && ruleName.equals(d.getString("rules"))) {
                                found = d;
                            }
                            existingCount++;
                        }
                        if (found != null && mode == 2) continue;

                        String docJson = "{" + frag.substring(rIdx, frag.length() - 1);
                        JsonObject newDoc = JsonObject.fromJson(docJson);

                        if (firstInColl) {
                            firstInColl = false;
                        }

                        String id = targetColl + "::" + ruleName;
                        if (found != null) {
                            couchColl.upsert(id, newDoc);
                            uCnt++;
                        } else {
                            if (existingCount < 8) {
                                couchColl.upsert(id, newDoc);
                                iCnt++;
                            } else {
                                eCnt++;
                            }
                        }
                    }
                }

            }
        }


        return ""+collCnt+" "+iCnt+" "+uCnt+" "+eCnt;
    }

    @Override
    public String expBase() throws IOException {
        String errStr = "";
        Map<String, String> roles = new LinkedHashMap<>();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("access/plist.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                String[] p = line.split("~");
                if (p.length >= 3) {
                    roles.put(p[0], p[2]);
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            List<ObjectNode> items = new ArrayList<>();

            for (String coll : baseRepository.getCollectionNames()) {

                ObjectNode node = mapper.createObjectNode();

                String role = roles.getOrDefault(coll, "???");
                node.put("FIO", role);
                node.put("Collection", coll);

                ArrayNode docsArr = node.putArray("docs");

                for (JsonObject doc : baseRepository.find(coll)) {
                    JsonNode docNode = mapper.readTree(doc.toString());
                    docsArr.add(docNode);
                }

                if (docsArr.size() > 0) {
                    items.add(node);
                }
            }

            Path out = Paths.get("users", "BaseContent.json");
            Files.createDirectories(out.getParent());

            try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(w, items);
            }
        } catch (Exception e) {
            errStr = e.getMessage();
        }

        return errStr;
    }

    @Override
    public void deleteTranslatorFile(String wUser) {
        System.out.println("nothing here yet");
    }
    @Override
    public List<String> getJobs() throws IOException {
        String mainDir = new File(".").getCanonicalPath(); // или config.getAppDir()
        File jobFile = new File(mainDir + "/help/job.txt");

        List<String> jobs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(jobFile), StandardCharsets.UTF_8)
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
    @Override
    public Map<String, String> getUserPasswordMap() {
        return null;
    } // логин -> пароль
    @Override
    public List<String> getCollectionNames() {
        return baseRepository.getCollectionNames();
    }


    @Override
    public Tune getUserTune(String wUser) {
        return tuneService.getTune(wUser);
    }

    @Override
    public AdminInitData getInitData(String wUser) throws IOException {
        List<String> jobs = getJobs(); // читаем job.txt
        List<String> collsList = baseRepository.getCollectionNames(); // список коллекций
        Tune tune = tuneService.getTune(wUser); // получаем объект tune из couchbase


        String dTime = "0", autoS = "0", helP = "0", tplName = "";
        boolean saveNN = false;

        if (tune != null) {
            dTime = String.valueOf(tune.getDeltat());

            // tplName = fold + "/" + scan + pars
            String fold = tune.getLanguage();
            String scan = tune.getScanner();
            String pars = tune.getParser();
            if (fold != null && scan != null && pars != null) {
                tplName = fold + "/" + scan + pars;
            }

            // autoS и saveNN
            boolean saving = tune.getSaving().contains("+");
            //String saving = tune.getSaving(); // например, "+2"

            saveNN = saving;
            autoS = (saving) ? "1" : "0";

            helP = String.valueOf(tune.getAssist());

        }

        return AdminInitData.builder()
                .wUser(wUser)
                .jobs(jobs)
                .collsList(collsList)
                .dTime(dTime)
                .tplName(tplName)
                .saveNN(saveNN)
                .autoS(autoS)
                .helP(helP)
                .build();
    }
}
