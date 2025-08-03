package application.service;

import application.model.RuleDocument;
import application.repository.RuleRepository;
import com.couchbase.client.java.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleServiceImpl implements RuleService {


    private final RuleRepository ruleRepository;

    @Autowired
    public RuleServiceImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public void upsertRule(String id, String userLogin, RuleDocument ruleDocument) {
        if (ruleRepository.findUserRuleNames(userLogin).size() < 8) {
            ruleRepository.upsertRule(id, userLogin, ruleDocument);
        } else {
            System.err.println("Слишком много сохраненных правил");
        }
    }
    public void deleteRule(String id, String userLogin) {
        ruleRepository.deleteRule(id, userLogin);
    }
    public List<String> getSampleDocuments() {
        return ruleRepository.findSampleRuleNames();
    }
    public List<String> getUserDocuments(String userLogin) {
        return ruleRepository.findUserRuleNames(userLogin);
    }
    public RuleDocument getRule(String id, String userLogin) {
        return ruleRepository.findRule(id, userLogin);
    }


}
