package application.service;

import application.config.DescriptionProperties;
import application.model.InitData;
import application.model.Option;
import application.model.Tune;
import application.repository.BaseRepository;
import application.repository.TuneRepository;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.core.service.BackupService;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.kv.GetResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class TuneServiceImpl implements TuneService {


    //private final ResourceLoader resourceLoader;
    private TuneRepository tuneRepository;
    //private BaseRepository baseRepository;
    private final Bucket bucket;

    @Autowired
    public TuneServiceImpl(TuneRepository tuneRepository, Bucket bucket) {
        this.tuneRepository = tuneRepository;
        this.bucket = bucket;
        //this.baseRepository = baseRepository;
        //this.resourceLoader = resourceLoader;
    }

    @Override
    public void upsertTune(String userLogin, Tune tune) {
        tuneRepository.upsertTune(userLogin, tune);
    }

    @Override
    public void deleteTune(String userLogin) {
        tuneRepository.deleteTune(userLogin);
    }

    @Override
    public Tune getTune(String userLogin) {
        return tuneRepository.findTune(userLogin);
    }


    @Override
    public Tune getOrCreateTune(String wUser) {
        try {
            GetResult result = bucket.scope("_default").collection(wUser).get(wUser + "::tune");
            return result.contentAs(Tune.class);
        } catch (DocumentNotFoundException e) {
            // создаём новый документ с настройками по умолчанию
            Tune defaultTune = Tune.builder()
                    .language("js")
                    .scanner("lexAsTable")
                    .parser("SyntAsSingleFSM")
                    .saving("+3")
                    .deltat(0)
                    .assist(1)
                    .build();

            bucket.scope("_default").collection(wUser).upsert(wUser + "::tune", defaultTune);
            return defaultTune;
        }
    }

}
