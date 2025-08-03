package application.repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import application.config.CouchbaseConfig;
import application.model.RuleDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;

@Repository
public class RuleRepositoryImpl implements RuleRepository {

    private final Bucket bucket;
    private final Cluster cluster;

    @Autowired
    public RuleRepositoryImpl(Cluster cluster, Bucket bucket) {
        this.bucket = bucket;
        this.cluster = cluster;
        //this.bucket = cluster.bucket(bucket.name());
        this.bucket.waitUntilReady(Duration.ofSeconds(5));
    }

    @Override
    // данный метод получает только заголовки, НЕ содержимое
    public List<String> findSampleRuleNames() {
        String q = String.format(
                        "SELECT r.rules AS ruleName " +
                        "FROM `%s`._default.Samples r " +
                        "WHERE r.rules IS VALUED", bucket.name()
        );

        return cluster.query(q)
                .rowsAsObject()
                .stream()
                .map(row -> row.getString("ruleName"))
                .collect(Collectors.toList());
    }


    @Override
    public List<String> findUserRuleNames(String userLogin) {
        String q = String.format(
                "SELECT r.rules AS ruleName " +
                        "FROM `%s`._default.`%s` r " +
                        "WHERE r.rules IS VALUED", bucket.name(), userLogin
        );

        return cluster.query(q)
                .rowsAsObject()
                .stream()
                .map(row -> row.getString("ruleName"))
                .collect(Collectors.toList());
    }


    @Override
    public Optional<JsonObject> findUserTune(String userLogin) {
        String q = String.format(
                "SELECT r.tune AS tuneObj " +
                        "FROM `%s`._default.`%s` r " +
                        "WHERE r.tune IS VALUED LIMIT 1", bucket.name(), userLogin
        );

        List<JsonObject> rows = cluster.query(q)
                .rowsAsObject()
                .stream()
                .map(row -> row.getObject("tuneObj"))
                .toList();

        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    @Override
    public RuleDocument findRule(String id, String userLogin) {
            GetResult res = bucket
                    .scope("_default")
                    .collection(userLogin)
                    .get(userLogin + "::" + id);
            return res.contentAs(RuleDocument.class);
    }

    @Override
    // userLogin == collName
    public void upsertRule(String id, String userLogin, RuleDocument ruleDocument) {
        bucket.scope("_default")
                .collection(userLogin)
                .upsert(id, ruleDocument);
    }

    @Override
    public void deleteRule(String id, String userLogin) {
        bucket.scope("_default")
                .collection(userLogin)
                .remove(id);
    }
}
