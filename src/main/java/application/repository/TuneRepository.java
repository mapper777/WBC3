package application.repository;

import application.model.Tune;
import com.couchbase.client.java.json.JsonObject;

import java.util.List;
import java.util.Optional;

public interface TuneRepository {
    void upsertTune(String userLogin, Tune tune);
    void deleteTune(String userLogin);
    Tune findTune(String userLogin);

}
