package application.service;

import application.model.AdminInitData;
import application.model.Tune;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public interface AdminService {

    // (в getPList.jsp создаем response и добавляется header)
    Resource getPlist(String filename) throws IOException; // сессию будет принимать контроллер
    void savePlist(Reader reader) throws IOException;
    String listBase() throws IOException; // <%=((errStr=="")?res:errStr)%>

    String dropColls(String names); // возвращает строку вида в dropColls.jsp:
    //                    <%=""+collCnt+"|"+docCnt+"|"+errStr%>

    String getContentOld() throws IOException;
    String getContent() throws IOException; // читает содержимое BaseContent.json и выводит содержимое в виде строки
    String impBase() throws IOException; // возврщает следующее содержимое:
    // errStr=""+collCnt+" "+iCnt+" "+uCnt+" "+eCnt; <%=(errStr=="")?"":errStr%>
    String expBase() throws IOException; // <%=((errStr=="")?"":errStr)%>
    // нужно ли возвращать String, или можно ограничиться чем-то ещё?
    void deleteTranslatorFile(String wUser);
    List<String> getJobs() throws IOException;
    Map<String, String> getUserPasswordMap(); // логин -> пароль
    // получение списка коллекций
    List<String> getCollectionNames();
    Tune getUserTune(String wUser);

    AdminInitData getInitData(String wUser) throws IOException;

}
