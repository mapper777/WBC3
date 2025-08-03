package application.repository;

import application.config.CouchbaseConfig;
import application.model.Tune;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.kv.GetResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class TuneRepositoryImpl implements TuneRepository {

    private final Bucket bucket;
    private final Cluster cluster;

    @Autowired
    public TuneRepositoryImpl(Cluster cluster, Bucket bucket) {
        this.cluster = cluster;
        this.bucket = bucket;
        bucket.waitUntilReady(Duration.ofSeconds(5));
    }

    @Override
    public Tune findTune(String userLogin) {
        GetResult res = bucket.scope("_default")
                .collection(userLogin)
                .get(userLogin + "::tune");
        return res.contentAs(Tune.class);
    }

    @Override
    public void upsertTune(String userLogin, Tune tune) {
        bucket.scope("_default")
                .collection(userLogin)
                .upsert(userLogin + "::tune",
                        tune);
    }

    @Override
    public void deleteTune(String userLogin) {
        bucket.scope("_default")
                .collection(userLogin)
                .remove(userLogin + "::tune");
    }

}
