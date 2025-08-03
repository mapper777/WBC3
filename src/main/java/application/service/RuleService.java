package application.service;

import application.model.RuleDocument;
import com.couchbase.client.java.json.JsonObject;

import java.util.List;


public interface RuleService {
    void upsertRule(String id, String userLogin, RuleDocument ruleDocument);
    void deleteRule(String id, String userLogin);
    List<String> getSampleDocuments();
    List<String> getUserDocuments(String userLogin);
    RuleDocument getRule(String id, String userLogin);
}
