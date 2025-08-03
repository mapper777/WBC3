package application.service;

import application.model.InitData;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public interface MainService {
    void clearSession(HttpServletRequest request, HttpServletResponse response);
    File getFile(String filename, ServletRequest request) throws IOException;
    InitData getInitData(String user) throws IOException;

}
