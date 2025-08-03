package application.service;

import application.model.InitData;
import application.model.Tune;

import java.io.IOException;
import java.util.List;

public interface TuneService {
    void upsertTune(String userLogin, Tune tune);
    void deleteTune(String userLogin);
    Tune getTune(String userLogin);
    Tune getOrCreateTune(String wUser);
}
