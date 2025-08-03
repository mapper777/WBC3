package application.repository;

import com.couchbase.client.java.json.JsonObject;
import application.model.RuleDocument;
import java.util.List;
import java.util.Optional;

public interface RuleRepository {

    List<String> findSampleRuleNames();
    List<String> findUserRuleNames(String userLogin);
    Optional<JsonObject> findUserTune(String userLogin);
    RuleDocument findRule(String id, String userLogin);
    void upsertRule(String id, String userLogin, RuleDocument ruleDocument);
    void deleteRule(String id, String userLogin);

}
