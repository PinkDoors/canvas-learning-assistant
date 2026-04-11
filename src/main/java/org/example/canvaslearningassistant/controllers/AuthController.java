package org.example.canvaslearningassistant.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.canvaslearningassistant.configuration.CanvasConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lti")
@Slf4j
public class AuthController {

    private final CanvasConfiguration canvasConfiguration;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam Map<String,String> params) {
        log.info("Login request received");

        String targetLinkUri = params.get("target_link_uri");
        String loginHint = params.get("login_hint");
        // String iss = params.get("iss");
        String ltiMessageHint = params.get("lti_message_hint"); // must include

        // Canvas OIDC endpoint
        // TODO: change to iss usage
        String oidcEndpoint = "http://canvas.docker" + "/api/lti/authorize_redirect";

        String state = java.util.UUID.randomUUID().toString();
        String nonce = java.util.UUID.randomUUID().toString();

        String redirectUrl = String.format(
                "%s?response_type=id_token" +
                        "&client_id=%s" +
                        "&login_hint=%s" +
                        "&target_link_uri=%s" +
                        "&redirect_uri=%s" +
                        "&scope=%s" +
                        "&state=%s" +
                        "&nonce=%s" +
                        "&response_mode=%s" +
                        "&prompt=%s" +
                        "&lti_message_hint=%s",
                oidcEndpoint,
                canvasConfiguration.getClientId(),
                java.net.URLEncoder.encode(loginHint, StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(targetLinkUri, StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(targetLinkUri, StandardCharsets.UTF_8), // TODO: change redirect link, current due to missing canvas verification step
                "openid",
                state,
                nonce,
                "form_post",
                "none",
                java.net.URLEncoder.encode(ltiMessageHint, StandardCharsets.UTF_8)
        );

        log.info("Sending redirect to lti");
        return ResponseEntity.status(302)
                .header("Location", redirectUrl)
                .build();
    }
}