package application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitData {
    private String wUser;
    private List<String> jobs;
    private String rulesList; // HTML: rulesList
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
}