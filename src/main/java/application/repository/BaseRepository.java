package application.repository;

import application.config.CouchbaseConfig;
import application.controller.RuleDocumentController;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.manager.collection.CollectionSpec;
import com.couchbase.client.java.query.QueryResult;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;

@Repository
public class BaseRepository {
    @Getter
    private final Cluster cluster;
    @Getter
    private final Bucket bucket;
    @Getter
    private final CouchbaseConfig config;
    @Getter
    private final Scope scope;

    private static final Logger log = LoggerFactory.getLogger(BaseRepository.class);

    @PostConstruct
    public void init() {
        log.debug("BaseRepository: cluster={} bucket={} scope={}", cluster, bucket, scope);
    }

    @Autowired
    public BaseRepository(Cluster cluster, Bucket bucket, CouchbaseConfig config) {
        this.cluster = cluster;
        this.bucket = bucket;
        this.config = config;
        this.scope = bucket.scope("_default");
    }

    public List<String> getCollectionNames() {
//        String query = "SELECT name FROM system:keyspaces" +
//                " WHERE bucket_name = $bucket AND scope_name = '_default'";
//        QueryResult result = cluster.query(query,
//                queryOptions().parameters(
//                        JsonObject.create().put("bucket", bucket.name())
//                ));
//
//        return result.rowsAsObject().stream()
//                .map(row -> row.getString("name"))
//                .collect(Collectors.toList());

        return bucket.collections()
                .getAllScopes()
                .stream()
                .filter(scope -> scope.name().equals("_default"))
                .flatMap(scope -> scope.collections().stream())
                .map(CollectionSpec::name)
                .toList();
    }


    public Collection getCollection(String collection) {
        return bucket.collection(collection);
    }

    public List<JsonObject> find(String collectionName) {
        try {
            String queryStr = String.format(
                    "SELECT META().id AS id, `%s`.* FROM `%s`.`%s`.`%s`",
                    collectionName, bucket.name(), scope.name(), collectionName
            );

            QueryResult result = cluster.query(queryStr);
            return result.rowsAsObject().stream().toList();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return Collections.emptyList();
    }
}
