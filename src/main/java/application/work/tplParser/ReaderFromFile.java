package application.work.tplParser;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ReaderFromFile {
    private InputStreamReader inputStream = null;
    private int backedC;
    private boolean flag = false;

    public ReaderFromFile(String s) throws FileNotFoundException {

        Resource resource = new ClassPathResource(s);
        if (!resource.exists()) {
            throw new FileNotFoundException("Template not found: " + s);
        }

        if (s == null) return;
        try {
//   inputStream=new FileReader(s);
            inputStream = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            //FileInputStream fis = new FileInputStream(s);
//InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            //inputStream = new InputStreamReader(fis, StandardCharsets.UTF_8);//new FileReader(isr);
        } catch (Exception e) {
            inputStream = null;
        }
    }

    public void back(int bc) {
        if (!flag) {
            flag = true;
            backedC = bc;
        }
    }

    public int read() {
        if (flag) {
            flag = false;
            return backedC;
        }
        try {
            if (inputStream.ready()) return inputStream.read();
            else {
                inputStream.close();
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }
}