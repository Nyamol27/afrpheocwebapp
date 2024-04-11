package net.pheocnetafr.africapheocnet.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Map;

@Controller
public class CsrfTokenController {

    @GetMapping(value = "/csrf-token-endpoint", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> getCsrfToken(CsrfToken csrfToken) {
        if (csrfToken != null) {
            Map<String, String> response = Collections.singletonMap("csrfToken", csrfToken.getToken());
            System.out.println("CSRF Token: " + csrfToken.getToken());
            return ResponseEntity.ok(response);
        } else {
            System.err.println("CSRF Token not found");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
