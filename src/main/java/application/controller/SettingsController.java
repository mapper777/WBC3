package application.controller;

import application.model.InitData;
import application.model.Tune;
import application.service.InitDataService;
import application.service.TuneService;
import com.couchbase.client.core.error.DocumentExistsException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;


@RestController
public class SettingsController {

    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    private final TuneService tuneService;
    private final InitDataService initDataService;

    public SettingsController(TuneService tuneService, InitDataService initDataService) {
        this.tuneService = tuneService;
        this.initDataService = initDataService;
    }

    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String DOCUMENT_NOT_FOUND = "Document Not Found";

    @GetMapping("/settings")
    public String settingsPage(HttpSession session, Model model) throws IOException {

        String wUser = (String) session.getAttribute("WBCuser");
        if (wUser == null || wUser.isBlank()) {
            return "redirect:/auth/login"; // как пример
        }

        InitData initData = initDataService.getInitData(wUser);
        model.addAttribute("initData", initData);
        return "settingsPage";
    }


    @PostMapping("/saveSettings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tune updated"),
            @ApiResponse(responseCode = "201", description = "Tune created"),
            @ApiResponse(responseCode = "404", description = "Tune not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Void> saveSettings(HttpSession session,
                                               @Valid @RequestBody Tune tune) {
        String userLogin = (String) session.getAttribute("WBCuser");
        String id = userLogin + "::tune";
        try {
            tuneService.upsertTune(userLogin, tune);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (DocumentExistsException e) {
            log.error(DOCUMENT_NOT_FOUND + ": " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
