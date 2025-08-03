    package application.controller;
    
    import application.model.InitData;
    import application.service.MainService;
    import com.couchbase.client.core.error.DocumentNotFoundException;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.responses.ApiResponses;
    import jakarta.servlet.ServletRequest;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import jakarta.servlet.http.HttpSession;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.core.io.FileSystemResource;
    import org.springframework.core.io.InputStreamResource;
    import org.springframework.core.io.Resource;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;

    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileNotFoundException;
    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;

    @Controller
    public class MainController {
    
        private final MainService mainService;

        private static final Logger log = LoggerFactory.getLogger(MainController.class);
        private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
        private static final String DOCUMENT_NOT_FOUND = "Document Not Found";


        @Autowired
        public MainController(MainService mainService) {
            this.mainService = mainService;
        }
    
        @GetMapping("/main")
        public String showMainPage(HttpSession session, Model model) throws IOException {
            String wUser = (String) session.getAttribute("WBCuser");
            if (wUser == null || wUser.isBlank()) {
                return "redirect:/auth/login"; // как пример
            }
    
            InitData data = mainService.getInitData(wUser);
            model.addAttribute("initData", data);
    
            return "mainPage"; // thymeleaf шаблон MainPage.html
        }
        // был Post, стал GET; не рекомендуется оставлять GET-запрос
        @GetMapping("/logout")
        public String logout(HttpServletRequest request, HttpServletResponse response) {
            mainService.clearSession(request, response);
            return "redirect:/auth/login";
        }
    
    
        @GetMapping("/file")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "File found"),
                @ApiResponse(responseCode = "404", description = "File not found"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<InputStreamResource> getFile(@RequestParam String filename, ServletRequest request) throws IOException {
            try {
                File file = mainService.getFile(filename, request);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentLength(file.length())
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(new InputStreamResource(new FileInputStream(file)));
            } catch (DocumentNotFoundException e) {
                log.error(DOCUMENT_NOT_FOUND, e);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } catch (IOException e) {
                log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

        }


        @GetMapping("/help")
        public String helpPage() {
            return "helpPage"; // thymeleaf
        }

        @GetMapping("/users")
        public ResponseEntity<org.springframework.core.io.Resource> download(@RequestParam String v, HttpSession session) throws IOException {
            String wUser = (String) session.getAttribute("WBCuser");
            Path file = Paths.get("users", wUser + "translator.txt");
            if (!Files.exists(file)) return ResponseEntity.notFound().build();
            Resource res = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(res);
        }
    }
