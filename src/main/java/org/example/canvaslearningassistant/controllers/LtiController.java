package org.example.canvaslearningassistant.controllers;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("/lti")
public class LtiController {

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam Map<String,String> params) {
        String targetLinkUri = params.get("target_link_uri");
        String loginHint = params.get("login_hint");
        // String iss = params.get("iss");
        String ltiMessageHint = params.get("lti_message_hint"); // must include

        // Canvas OIDC endpoint
        // TODO: change to iss usage
        String oidcEndpoint = "http://canvas.docker" + "/api/lti/authorize_redirect";

        // Generate unique state and nonce
        String state = java.util.UUID.randomUUID().toString();
        String nonce = java.util.UUID.randomUUID().toString();

        // Build redirect URL with all required parameters
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
                "10000000000003", // TODO: move to config
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

        // Return 302 redirect
        return ResponseEntity.status(302)
                .header("Location", redirectUrl)
                .build();
    }

    // TODO: Check canvas public key
    @PostMapping("/launch")
    public ModelAndView launchThymeleaf(@RequestParam Map<String,String> body) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(body.get("id_token"));
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        Map<String, Object> ltiClaims = claims.getClaims();
        String userId = (String) ltiClaims.get("sub");
        String courseId = "unknown";
        Object contextObj = ltiClaims.get("https://purl.imsglobal.org/spec/lti/claim/context");
        if (contextObj instanceof Map<?, ?> contextMap) {
            courseId = (String) contextMap.get("id");
        }

        ModelAndView mav = new ModelAndView("chat");
        mav.addObject("userId", userId);
        mav.addObject("courseId", courseId);

        return mav;
    }
}