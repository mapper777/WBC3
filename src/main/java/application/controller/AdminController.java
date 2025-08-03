package application.controller;

import application.model.AdminInitData;
import application.model.Tune;
import application.service.AdminService;
import application.service.InitDataService;
import ch.qos.logback.core.encoder.EchoEncoder;
import com.couchbase.client.core.error.DocumentNotFoundException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.io.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {


    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String DOCUMENT_NOT_FOUND = "Document Not Found";

    @Autowired
    private final AdminService adminService;
    private final InitDataService initDataService;


    public AdminController(AdminService adminService, InitDataService initDataService) {
        this.adminService = adminService;
        this.initDataService = initDataService;
    }

    @GetMapping("")
    public String showAdminPage(HttpSession session, Model model) throws IOException {
        String wUser = (String) session.getAttribute("WBCuser");
        if (wUser == null || wUser.isBlank()) {
            return "redirect:/auth/login";
        }

        AdminInitData data = initDataService.getAdminInitData(wUser);
        model.addAttribute("data", data);

        return "adminPage"; // thymeleaf шаблон adminPage.html
    }

    @GetMapping("/job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job found"),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<List<String>> getJobLines() throws IOException {
        try {
            return ResponseEntity.ok(adminService.getJobs());
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getPlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plist found"),
            @ApiResponse(responseCode = "404", description = "Plist not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Resource> getPlistUsers(@RequestParam("file") String filename) throws IOException {
        try {
            Resource resource = adminService.getPlist(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/savePlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plist saved"),
            @ApiResponse(responseCode = "404", description = "Plist not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Void> savePlist(@RequestBody String content) throws IOException {
        try {
            adminService.savePlist(new StringReader(content));
            return ResponseEntity.ok().build();
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/dropColls")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Collections deleted"),
            @ApiResponse(responseCode = "404", description = "Collections not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<String> dropColls(@RequestParam String names) {
        try {
            return ResponseEntity.ok(adminService.dropColls(names));
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/listBase")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List base is found"),
            @ApiResponse(responseCode = "404", description = "List base is not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> listBase() throws IOException {
        try {
            return ResponseEntity.ok(adminService.listBase());
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/getContent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content found"),
            @ApiResponse(responseCode = "404", description = "Content not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> getContent() throws IOException {
        try {
        return ResponseEntity.ok(adminService.getContentOld());
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/expBase")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Export is done"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> expBase() throws IOException {
        try {
            return ResponseEntity.ok(adminService.expBase());
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/impBase")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import is done"),
            @ApiResponse(responseCode = "404", description = "Import file is not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> impBase() throws IOException {
        try {
            return ResponseEntity.ok(adminService.impBase());
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/collections")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collections found"),
            @ApiResponse(responseCode = "404", description = "Collections not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<String>> getAllCollections() {
        try {
            return ResponseEntity.ok(adminService.getCollectionNames());
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tune")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tune found"),
            @ApiResponse(responseCode = "404", description = "Tune not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Tune> getUserTune(@RequestParam String user) {
        try {
            return ResponseEntity.ok(adminService.getUserTune(user));
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND + "for: " + user);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


//    @DeleteMapping("/translator-file")
//    public ResponseEntity<String> deleteTranslatorFile(HttpSession session) {
//        String user = (String) session.getAttribute("WBCuser");
//        adminService.deleteTranslatorFile(user);
//        return ResponseEntity.noContent().build();
//    }

}
