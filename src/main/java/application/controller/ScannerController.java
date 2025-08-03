package application.controller;

import application.model.BuildRequest;
import application.model.BuildResult;
import application.service.ParserService;
import application.service.ScannerService;
import com.couchbase.client.core.error.DocumentNotFoundException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/work")
public class ScannerController {

    private final ScannerService scannerService;
    private final ParserService parserService;


    private static final Logger log = LoggerFactory.getLogger(ScannerController.class);
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String DOCUMENT_NOT_FOUND = "Document Not Found";


    public ScannerController(ScannerService scannerService, ParserService parserService) {
        this.scannerService = scannerService;
        this.parserService = parserService;
    }

    @PostMapping("/buildTranslator")
    public String buildTranslator(@RequestBody BuildRequest request, Model model, HttpSession session) throws IOException {
        BuildResult res = scannerService.buildScanner(request);
        res.setWUser((String) session.getAttribute("WBCuser"));

        if (res.isForwardToParser()) {
//            model.addAttribute("res", res);
//            return "forward:/buildParser";
            parserService.buildParser(res);
            return "buildParser";
        }

        model.addAttribute("res", res);
        return "scanner";
    }

    @PostMapping("/buildParser")
    public String buildParser(HttpServletRequest request, Model model) throws IOException {
        BuildResult result = (BuildResult) request.getAttribute("res");
        parserService.buildParser(result);
        model.addAttribute("res", result);
        return "buildParser";
    }

    @GetMapping("/getDocs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Docs found"),
            @ApiResponse(responseCode = "404", description = "Docs not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> getDocs(@RequestParam String name) {
        try {
            return ResponseEntity.ok(scannerService.getDocs(name));
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
