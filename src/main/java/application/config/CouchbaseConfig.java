package application.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.couchbase.client.core.error.BucketNotFoundException;
import com.couchbase.client.core.error.UnambiguousTimeoutException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;

import lombok.Getter;


@Configuration
@Getter
public class CouchbaseConfig {

    private static final Logger log = LoggerFactory.getLogger(CouchbaseConfig.class);
    // значение меняется в application.properties
    @Value("#{systemEnvironment['DB_CONN_STR'] ?: 'DB_CONN_STR'}")
    private String host;

    @Value("#{systemEnvironment['DB_USERNAME'] ?: 'DB_USERNAME'}")
    private String username;

    @Value("#{systemEnvironment['DB_PASSWORD'] ?: 'DB_PASSWORD'}")
    private String password;

    @Value("#{systemEnvironment['CB_BUCKET_NAME'] ?: 'WebCompBuilder'}")
    private String bucketName;


    @Bean(destroyMethod = "disconnect")
    Cluster getCouchbaseCluster() {
        try {
            log.debug("Connecting to Couchbase at " + host);
            Cluster cluster = Cluster.connect(host, username, password);
            cluster.waitUntilReady(Duration.ofSeconds(15));

            return cluster;
        } catch (UnambiguousTimeoutException ex) {
            log.error("Connection to Couchbase at " + host + " timed out" );
            throw ex;
        } catch (Exception ex) {
            log.error(ex.getClass().getName());
            log.error("Could not connect to Couchbase at " + host);
            throw ex;
        }
    }


    @Bean
    Bucket getCouchbaseBucket(Cluster cluster) {
        try {
            if (!cluster.buckets().getAllBuckets().containsKey(bucketName)) {
                throw new BucketNotFoundException("Bucket " + bucketName + " does not exist");
            }
            Bucket bucket = cluster.bucket(bucketName);
            bucket.waitUntilReady(Duration.ofSeconds(15));
            return bucket;
        } catch (UnambiguousTimeoutException ex) {
            log.error("Connection to bucket " + bucketName + " timed out");
            throw ex;
        } catch (BucketNotFoundException ex) {
            log.error("Bucket " + bucketName + " does not exist");
            throw ex;
        } catch (Exception ex) {
            log.error(ex.getClass().getName());
            log.error("Could not coonnect to bucket " + bucketName);
            throw ex;
        }
    }
}
