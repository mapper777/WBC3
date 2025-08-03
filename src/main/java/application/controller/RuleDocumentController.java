package application.controller;

import java.util.List;

import application.model.RuleDeleteRequest;
import com.couchbase.client.java.json.JsonObject;
import jakarta.servlet.http.HttpSession;
import application.model.RuleDocument;
import application.service.RuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.couchbase.client.core.error.DocumentExistsException;
import com.couchbase.client.core.error.DocumentNotFoundException;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/rules")
public class RuleDocumentController {

    private static final Logger log = LoggerFactory.getLogger(RuleDocumentController.class);

    private final RuleService ruleService;

    public RuleDocumentController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String DOCUMENT_NOT_FOUND = "Document Not Found";

    @GetMapping("/samples")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Samples Found"),
            @ApiResponse(responseCode = "404", description = "Samples not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<List<String>> getSamples() {
        try {
            return ResponseEntity.ok(ruleService.getSampleDocuments());
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rules Found"),
            @ApiResponse(responseCode = "404", description = "Rules not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<List<String>> getUserRules(HttpSession session) {
        try {
            String userLogin = (String) session.getAttribute("WBCuser");
            return ResponseEntity.ok(ruleService.getUserDocuments(userLogin));
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // было id
    // /{name}/{coll}
    @GetMapping("/getRules")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule Found"),
            @ApiResponse(responseCode = "404", description = "Rule not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<RuleDocument> getRuleById(HttpSession session, @RequestParam String name, @RequestParam String coll) {
        try {
            String userLogin = (String) session.getAttribute("WBCuser");
            RuleDocument ruleDocument = ruleService.getRule(coll, name);

            if (ruleDocument != null) {
                return new ResponseEntity<>(ruleDocument, HttpStatus.OK);
            } else {
                log.error(DOCUMENT_NOT_FOUND + ": " + coll);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND + ": " + coll);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rule deleted"),
            @ApiResponse(responseCode = "404", description = "Rule not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Void> deleteRuleById(HttpSession session, @RequestBody RuleDeleteRequest request) {
        String userLogin = (String) session.getAttribute("WBCuser");
        String id = userLogin + "::" + request.getRules();
        try {
            ruleService.deleteRule(id, userLogin);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (DocumentNotFoundException e) {
            log.error(DOCUMENT_NOT_FOUND + ": " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save")
    // save or update
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule updated"),
            @ApiResponse(responseCode = "201", description = "Rule created"),
            @ApiResponse(responseCode = "404", description = "Rule not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Void> saveRule(HttpSession session,
                                         @Valid @RequestBody RuleDocument ruleDocument) {
        String userLogin = (String) session.getAttribute("WBCuser");
        String id = userLogin + "::" + ruleDocument.getRules();
        try {
            ruleService.upsertRule(id, userLogin, ruleDocument);
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