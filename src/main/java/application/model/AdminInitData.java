package application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AdminInitData {
    private String wUser;
    private String pw;
    private List<String> jobs;
    private List<String> sampleRules;
    private List<String> userRules;
    // убираем на sampleRules и userRules
    private String rulesList; // HTML: rulesList
    private List<String> collsList;
    private List<String> collsPreview;
    private boolean hasMore;
    private String dTime;
    private String tplName;
    private boolean saveNN;
    private String autoS;
    private String helP;
    private String language;
    private String scanner;
    private String parser;

    private List<Option> languages;
    private List<Option> scanners;
    private List<Option> parsers;

    private String error;

}